package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ChargingPileBatchDeleteRequest;
import com.smartcharger.dto.request.ChargingPileCreateRequest;
import com.smartcharger.dto.request.ChargingPileStatusUpdateRequest;
import com.smartcharger.dto.request.ChargingPileUpdateRequest;
import com.smartcharger.dto.response.BatchDeleteResultResponse;
import com.smartcharger.dto.response.ChargingPileResponse;
import com.smartcharger.dto.response.ChargingPileStatisticsResponse;
import com.smartcharger.dto.response.ImportResultResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.repository.FaultReportRepository;
import com.smartcharger.repository.ReservationRepository;
import com.smartcharger.service.ChargingPileAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 充电桩管理服务实现类（管理端）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingPileAdminServiceImpl implements ChargingPileAdminService {

    private final ChargingPileRepository chargingPileRepository;
    private final ChargingRecordRepository chargingRecordRepository;
    private final ReservationRepository reservationRepository;
    private final FaultReportRepository faultReportRepository;

    @Override
    @Transactional
    public ChargingPileResponse createChargingPile(ChargingPileCreateRequest request) {
        // 验证充电桩编号是否唯一
        ChargingPile existingPile = chargingPileRepository.findByCode(request.getCode());
        if (existingPile != null) {
            throw new BusinessException(ResultCode.CHARGING_PILE_CODE_EXISTS);
        }

        // 创建充电桩
        ChargingPile chargingPile = new ChargingPile();
        chargingPile.setCode(request.getCode());
        chargingPile.setLocation(request.getLocation());
        chargingPile.setLng(request.getLng());
        chargingPile.setLat(request.getLat());
        chargingPile.setType(request.getType());
        chargingPile.setPower(request.getPower());
        chargingPile.setStatus(ChargingPileStatus.IDLE);

        chargingPileRepository.save(chargingPile);

        log.info("添加充电桩成功: code={}, id={}", request.getCode(), chargingPile.getId());

        return convertToResponse(chargingPile);
    }

    @Override
    @Transactional
    public ChargingPileResponse updateChargingPile(Long id, ChargingPileUpdateRequest request) {
        // 查询充电桩
        ChargingPile chargingPile = chargingPileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        // 如果更新编号，验证新编号是否唯一
        if (request.getCode() != null && !request.getCode().equals(chargingPile.getCode())) {
            ChargingPile existingPile = chargingPileRepository.findByCodeAndIdNot(request.getCode(), id);
            if (existingPile != null) {
                throw new BusinessException(ResultCode.CHARGING_PILE_CODE_EXISTS);
            }
            chargingPile.setCode(request.getCode());
        }

        // 更新其他字段
        if (request.getLocation() != null) {
            chargingPile.setLocation(request.getLocation());
        }
        if (request.getLng() != null) {
            chargingPile.setLng(request.getLng());
        }
        if (request.getLat() != null) {
            chargingPile.setLat(request.getLat());
        }
        if (request.getType() != null) {
            chargingPile.setType(request.getType());
        }
        if (request.getPower() != null) {
            chargingPile.setPower(request.getPower());
        }

        chargingPileRepository.save(chargingPile);

        log.info("更新充电桩成功: id={}", id);

        return convertToResponse(chargingPile);
    }

    @Override
    @Transactional
    public void deleteChargingPile(Long id) {
        // 查询充电桩
        ChargingPile chargingPile = chargingPileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        // 验证充电桩状态
        if (chargingPile.getStatus() != ChargingPileStatus.IDLE &&
                chargingPile.getStatus() != ChargingPileStatus.FAULT) {
            throw new BusinessException(ResultCode.CHARGING_PILE_CANNOT_DELETE);
        }

        // 检查是否有关联的充电记录
        Long recordCount = chargingRecordRepository.countByChargingPileId(id);
        if (recordCount > 0) {
            throw new BusinessException(ResultCode.CHARGING_PILE_HAS_RECORDS);
        }

        // 检查是否有关联的预约记录
        Long reservationCount = reservationRepository.countByChargingPileId(id);
        if (reservationCount > 0) {
            throw new BusinessException(ResultCode.CHARGING_PILE_HAS_RESERVATIONS);
        }

        // 删除充电桩
        chargingPileRepository.delete(chargingPile);

        log.info("删除充电桩成功: id={}, code={}", id, chargingPile.getCode());
    }

    @Override
    public Page<ChargingPileResponse> getAdminChargingPileList(ChargingPileType type, ChargingPileStatus status,
                                                                 String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<ChargingPile> chargingPilePage = chargingPileRepository.findByAdminConditions(
                type, status, keyword, pageable);

        return chargingPilePage.map(this::convertToResponseWithStats);
    }

    @Override
    public ChargingPileResponse getAdminChargingPileDetail(Long id) {
        ChargingPile chargingPile = chargingPileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        ChargingPileResponse response = convertToResponse(chargingPile);

        // 添加统计数据
        ChargingPileStatisticsResponse statistics = getChargingPileStatistics(id);
        response.setStatistics(statistics);

        return response;
    }

    @Override
    @Transactional
    public ChargingPileResponse updateChargingPileStatus(Long id, ChargingPileStatusUpdateRequest request) {
        // 查询充电桩
        ChargingPile chargingPile = chargingPileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        // 验证状态转换的合法性
        ChargingPileStatus newStatus = request.getStatus();

        // 只允许手动设置为"空闲"或"故障"
        if (newStatus != ChargingPileStatus.IDLE && newStatus != ChargingPileStatus.FAULT) {
            throw new BusinessException(ResultCode.CHARGING_PILE_STATUS_INVALID);
        }

        // 如果当前状态为"充电中"，不允许手动更改
        if (chargingPile.getStatus() == ChargingPileStatus.CHARGING) {
            throw new BusinessException(ResultCode.CHARGING_PILE_IS_CHARGING);
        }

        // 如果当前状态为"已预约"，需要先取消预约
        if (chargingPile.getStatus() == ChargingPileStatus.RESERVED) {
            throw new BusinessException(ResultCode.CHARGING_PILE_IS_RESERVED);
        }

        // 更新状态
        chargingPile.setStatus(newStatus);
        chargingPileRepository.save(chargingPile);

        log.info("更新充电桩状态成功: id={}, newStatus={}", id, newStatus);

        return convertToResponse(chargingPile);
    }

    @Override
    @Transactional
    public BatchDeleteResultResponse batchDeleteChargingPiles(ChargingPileBatchDeleteRequest request) {
        List<Long> ids = request.getIds();
        int totalCount = ids.size();
        int successCount = 0;
        List<BatchDeleteResultResponse.FailedRecord> failedRecords = new ArrayList<>();

        for (Long id : ids) {
            try {
                deleteChargingPile(id);
                successCount++;
            } catch (BusinessException e) {
                ChargingPile pile = chargingPileRepository.findById(id).orElse(null);
                String code = pile != null ? pile.getCode() : "未知";
                failedRecords.add(BatchDeleteResultResponse.FailedRecord.builder()
                        .id(id)
                        .code(code)
                        .reason(e.getMessage())
                        .build());
            }
        }

        log.info("批量删除充电桩完成: totalCount={}, successCount={}, failCount={}",
                totalCount, successCount, failedRecords.size());

        return BatchDeleteResultResponse.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failedRecords.size())
                .failedRecords(failedRecords)
                .build();
    }

    @Override
    public ChargingPileStatisticsResponse getChargingPileStatistics(Long id) {
        // 统计充电记录数量
        Long totalRecords = chargingRecordRepository.countByChargingPileId(id);

        // 统计总充电量
        BigDecimal totalElectricQuantity = chargingRecordRepository.sumElectricQuantityByChargingPileId(id);
        if (totalElectricQuantity == null) {
            totalElectricQuantity = BigDecimal.ZERO;
        }

        // 统计总营收
        BigDecimal totalRevenue = chargingRecordRepository.sumFeeByChargingPileId(id);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // 计算平均充电时长
        Double avgDuration = chargingRecordRepository.avgDurationByChargingPileId(id);
        Integer avgChargingDuration = avgDuration != null ? avgDuration.intValue() : 0;

        // 统计故障次数
        Long faultCount = faultReportRepository.countByChargingPileId(id);

        // 查询最后充电时间
        LocalDateTime lastChargingTime = chargingRecordRepository.findLastChargingTimeByChargingPileId(id);
        String lastChargingTimeStr = null;
        if (lastChargingTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            lastChargingTimeStr = lastChargingTime.format(formatter);
        }

        // 计算使用率（简化计算：充电记录数 / 总天数 * 100）
        BigDecimal utilizationRate = BigDecimal.ZERO;
        if (totalRecords > 0) {
            // 这里简化处理，实际应该根据充电桩创建时间到现在的天数计算
            utilizationRate = BigDecimal.valueOf(totalRecords * 2.5); // 假设每天最多40次充电
            if (utilizationRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                utilizationRate = BigDecimal.valueOf(100);
            }
        }

        return ChargingPileStatisticsResponse.builder()
                .totalChargingRecords(totalRecords.intValue())
                .totalElectricQuantity(totalElectricQuantity)
                .totalRevenue(totalRevenue)
                .avgChargingDuration(avgChargingDuration)
                .utilizationRate(utilizationRate)
                .faultCount(faultCount.intValue())
                .lastChargingTime(lastChargingTimeStr)
                .build();
    }

    /**
     * 转换为响应DTO
     */
    private ChargingPileResponse convertToResponse(ChargingPile chargingPile) {
        return ChargingPileResponse.builder()
                .id(chargingPile.getId())
                .code(chargingPile.getCode())
                .location(chargingPile.getLocation())
                .lng(chargingPile.getLng())
                .lat(chargingPile.getLat())
                .type(chargingPile.getType())
                .typeDesc(chargingPile.getType().getDescription())
                .power(chargingPile.getPower())
                .status(chargingPile.getStatus())
                .statusDesc(chargingPile.getStatus().getDescription())
                .createdTime(chargingPile.getCreatedTime())
                .updatedTime(chargingPile.getUpdatedTime())
                .build();
    }

    /**
     * 转换为响应DTO（包含统计数据）
     */
    private ChargingPileResponse convertToResponseWithStats(ChargingPile chargingPile) {
        ChargingPileResponse response = convertToResponse(chargingPile);

        // 添加简单统计数据
        Long recordCount = chargingRecordRepository.countByChargingPileId(chargingPile.getId());
        BigDecimal totalRevenue = chargingRecordRepository.sumFeeByChargingPileId(chargingPile.getId());

        response.setChargingRecordCount(recordCount != null ? recordCount.intValue() : 0);
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        return response;
    }

    @Override
    @Transactional
    public ImportResultResponse importChargingPiles(MultipartFile file) throws IOException {
        // 验证文件格式
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件格式不正确，仅支持.xlsx或.xls格式");
        }

        // 解析Excel文件
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        int totalCount = 0;
        int successCount = 0;
        List<ImportResultResponse.FailedRecord> failedRecords = new ArrayList<>();

        // 从第2行开始读取（第1行是表头）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            totalCount++;

            try {
                // 读取单元格数据
                String code = getCellValue(row.getCell(0));
                String location = getCellValue(row.getCell(1));
                String lngStr = getCellValue(row.getCell(2));
                String latStr = getCellValue(row.getCell(3));
                String typeStr = getCellValue(row.getCell(4));
                String powerStr = getCellValue(row.getCell(5));

                // 验证必填字段
                if (code == null || code.trim().isEmpty()) {
                    failedRecords.add(ImportResultResponse.FailedRecord.builder()
                            .row(i + 1)
                            .code(code)
                            .reason("充电桩编号不能为空")
                            .build());
                    continue;
                }

                if (location == null || location.trim().isEmpty()) {
                    failedRecords.add(ImportResultResponse.FailedRecord.builder()
                            .row(i + 1)
                            .code(code)
                            .reason("位置描述不能为空")
                            .build());
                    continue;
                }

                if (typeStr == null || typeStr.trim().isEmpty()) {
                    failedRecords.add(ImportResultResponse.FailedRecord.builder()
                            .row(i + 1)
                            .code(code)
                            .reason("充电桩类型不能为空")
                            .build());
                    continue;
                }

                if (powerStr == null || powerStr.trim().isEmpty()) {
                    failedRecords.add(ImportResultResponse.FailedRecord.builder()
                            .row(i + 1)
                            .code(code)
                            .reason("功率不能为空")
                            .build());
                    continue;
                }

                // 验证充电桩编号是否已存在
                ChargingPile existingPile = chargingPileRepository.findByCode(code);
                if (existingPile != null) {
                    failedRecords.add(ImportResultResponse.FailedRecord.builder()
                            .row(i + 1)
                            .code(code)
                            .reason("充电桩编号已存在")
                            .build());
                    continue;
                }

                // 解析类型
                ChargingPileType type;
                try {
                    type = ChargingPileType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    failedRecords.add(ImportResultResponse.FailedRecord.builder()
                            .row(i + 1)
                            .code(code)
                            .reason("充电桩类型无效，必须是AC或DC")
                            .build());
                    continue;
                }

                // 解析功率
                BigDecimal power;
                try {
                    power = new BigDecimal(powerStr);
                    if (power.compareTo(BigDecimal.ZERO) <= 0) {
                        failedRecords.add(ImportResultResponse.FailedRecord.builder()
                                .row(i + 1)
                                .code(code)
                                .reason("功率必须大于0")
                                .build());
                        continue;
                    }
                } catch (NumberFormatException e) {
                    failedRecords.add(ImportResultResponse.FailedRecord.builder()
                            .row(i + 1)
                            .code(code)
                            .reason("功率格式不正确")
                            .build());
                    continue;
                }

                // 解析经纬度（可选）
                BigDecimal lng = null;
                BigDecimal lat = null;
                if (lngStr != null && !lngStr.trim().isEmpty()) {
                    try {
                        lng = new BigDecimal(lngStr);
                    } catch (NumberFormatException e) {
                        failedRecords.add(ImportResultResponse.FailedRecord.builder()
                                .row(i + 1)
                                .code(code)
                                .reason("经度格式不正确")
                                .build());
                        continue;
                    }
                }
                if (latStr != null && !latStr.trim().isEmpty()) {
                    try {
                        lat = new BigDecimal(latStr);
                    } catch (NumberFormatException e) {
                        failedRecords.add(ImportResultResponse.FailedRecord.builder()
                                .row(i + 1)
                                .code(code)
                                .reason("纬度格式不正确")
                                .build());
                        continue;
                    }
                }

                // 创建充电桩
                ChargingPile chargingPile = new ChargingPile();
                chargingPile.setCode(code);
                chargingPile.setLocation(location);
                chargingPile.setLng(lng);
                chargingPile.setLat(lat);
                chargingPile.setType(type);
                chargingPile.setPower(power);
                chargingPile.setStatus(ChargingPileStatus.IDLE);

                chargingPileRepository.save(chargingPile);
                successCount++;

            } catch (Exception e) {
                String code = getCellValue(row.getCell(0));
                failedRecords.add(ImportResultResponse.FailedRecord.builder()
                        .row(i + 1)
                        .code(code)
                        .reason("导入失败：" + e.getMessage())
                        .build());
            }
        }

        workbook.close();

        log.info("批量导入充电桩完成: totalCount={}, successCount={}, failCount={}",
                totalCount, successCount, failedRecords.size());

        return ImportResultResponse.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failedRecords.size())
                .failedRecords(failedRecords)
                .build();
    }

    @Override
    public Workbook exportChargingPiles(ChargingPileType type, ChargingPileStatus status) {
        // 查询充电桩列表
        List<ChargingPile> chargingPiles;
        if (type != null || status != null) {
            Pageable pageable = PageRequest.of(0, 10000); // 最多导出10000条
            Page<ChargingPile> page = chargingPileRepository.findByAdminConditions(type, status, null, pageable);
            chargingPiles = page.getContent();
        } else {
            chargingPiles = chargingPileRepository.findAll();
        }

        // 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("充电桩列表");

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"充电桩编号", "位置描述", "经度", "纬度", "类型", "功率(kW)", "状态", "创建时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (ChargingPile pile : chargingPiles) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(pile.getCode());
            row.createCell(1).setCellValue(pile.getLocation());
            row.createCell(2).setCellValue(pile.getLng() != null ? pile.getLng().toString() : "");
            row.createCell(3).setCellValue(pile.getLat() != null ? pile.getLat().toString() : "");
            row.createCell(4).setCellValue(pile.getType().name());
            row.createCell(5).setCellValue(pile.getPower().toString());
            row.createCell(6).setCellValue(pile.getStatus().getDescription());
            row.createCell(7).setCellValue(pile.getCreatedTime().format(formatter));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        log.info("导出充电桩数据: count={}", chargingPiles.size());

        return workbook;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // 避免科学计数法
                    return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
