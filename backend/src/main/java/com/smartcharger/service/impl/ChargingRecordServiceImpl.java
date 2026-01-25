package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ChargingRecordEndRequest;
import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;
import com.smartcharger.dto.response.ChargingStatisticsMonthlyResponse;
import com.smartcharger.dto.response.ChargingStatisticsYearlyResponse;
import com.smartcharger.entity.*;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.repository.*;
import com.smartcharger.service.ChargingRecordService;
import com.smartcharger.service.PriceConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 充电记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingRecordServiceImpl implements ChargingRecordService {

    private final ChargingRecordRepository chargingRecordRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final PriceConfigService priceConfigService;

    @Override
    @Transactional
    public ChargingRecordResponse startCharging(Long userId, ChargingRecordStartRequest request) {
        // 1. 验证用户是否已有充电中的记录
        Optional<ChargingRecord> existingRecord = chargingRecordRepository.findByUserIdAndStatus(
                userId, ChargingRecordStatus.CHARGING);
        if (existingRecord.isPresent()) {
            throw new BusinessException(ResultCode.USER_ALREADY_CHARGING);
        }

        // 2. 验证充电桩是否存在
        ChargingPile chargingPile = chargingPileRepository.findById(request.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        // 3. 验证充电桩状态（不能是充电中或故障）
        if (chargingPile.getStatus() == ChargingPileStatus.CHARGING) {
            throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
        }
        if (chargingPile.getStatus() == ChargingPileStatus.FAULT) {
            throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
        }

        // 4. 检查充电桩是否有有效预约（无论充电桩状态如何，都要检查）
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> validReservations = reservationRepository
                .findByChargingPileIdAndStatusAndEndTimeAfter(
                        request.getChargingPileId(),
                        ReservationStatus.PENDING,
                        now
                );

        // 查找当前用户的有效预约（预约开始时间前30分钟内可用）
        Reservation userReservation = validReservations.stream()
                .filter(r -> r.getUserId().equals(userId) &&
                        r.getStartTime().isBefore(now.plusMinutes(30)))
                .findFirst()
                .orElse(null);

        // 查找其他用户的有效预约
        boolean hasOtherUserReservation = validReservations.stream()
                .anyMatch(r -> !r.getUserId().equals(userId));

        // 如果有其他用户的预约，拒绝充电
        if (hasOtherUserReservation) {
            throw new BusinessException(ResultCode.NO_VALID_RESERVATION);
        }

        // 5. 如果提供了车辆ID，验证车辆是否存在且属于当前用户
        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndUserId(request.getVehicleId(), userId)
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        }

        // 6. 创建充电记录
        ChargingRecord chargingRecord = new ChargingRecord();
        chargingRecord.setUserId(userId);
        chargingRecord.setChargingPileId(request.getChargingPileId());
        chargingRecord.setVehicleId(request.getVehicleId());
        chargingRecord.setStartTime(LocalDateTime.now());
        chargingRecord.setStatus(ChargingRecordStatus.CHARGING);

        chargingRecord = chargingRecordRepository.save(chargingRecord);

        // 7. 如果使用了预约，更新预约状态为已完成
        if (userReservation != null) {
            userReservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(userReservation);
            log.info("使用预约开始充电: userId={}, reservationId={}", userId, userReservation.getId());
        }

        // 8. 更新充电桩状态为"充电中"
        chargingPile.setStatus(ChargingPileStatus.CHARGING);
        chargingPileRepository.save(chargingPile);

        log.info("开始充电成功: userId={}, recordId={}, pileId={}",
                userId, chargingRecord.getId(), request.getChargingPileId());

        return convertToResponse(chargingRecord, chargingPile, null);
    }

    @Override
    @Transactional
    public ChargingRecordResponse endCharging(Long userId, Long recordId, ChargingRecordEndRequest request) {
        // 1. 验证充电记录是否存在
        ChargingRecord chargingRecord = chargingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_RECORD_NOT_FOUND));

        // 2. 验证充电记录是否属于当前用户
        if (!chargingRecord.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 验证充电记录状态是否为"充电中"
        if (chargingRecord.getStatus() != ChargingRecordStatus.CHARGING) {
            throw new BusinessException(ResultCode.CHARGING_RECORD_NOT_CHARGING);
        }

        // 4. 记录结束时间
        LocalDateTime endTime = LocalDateTime.now();
        chargingRecord.setEndTime(endTime);

        // 5. 计算充电时长（分钟）
        Duration duration = Duration.between(chargingRecord.getStartTime(), endTime);
        int durationMinutes = (int) duration.toMinutes();
        chargingRecord.setDuration(durationMinutes);

        // 6. 记录充电量
        chargingRecord.setElectricQuantity(request.getElectricQuantity());

        // 7. 获取充电桩信息并计算费用
        ChargingPile chargingPile = chargingPileRepository.findById(chargingRecord.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        BigDecimal fee = priceConfigService.calculateFee(
                chargingPile.getType().name(),
                request.getElectricQuantity()
        );
        chargingRecord.setFee(fee);

        // 8. 更新充电记录状态为"已完成"
        chargingRecord.setStatus(ChargingRecordStatus.COMPLETED);
        chargingRecord = chargingRecordRepository.save(chargingRecord);

        // 9. 更新充电桩状态为"空闲"
        chargingPile.setStatus(ChargingPileStatus.IDLE);
        chargingPileRepository.save(chargingPile);

        log.info("结束充电成功: userId={}, recordId={}, duration={}min, quantity={}, fee={}",
                userId, recordId, durationMinutes, request.getElectricQuantity(), fee);

        return convertToResponse(chargingRecord, chargingPile, null);
    }

    @Override
    public Page<ChargingRecordResponse> getChargingRecordList(Long userId, ChargingRecordStatus status,
                                                                LocalDate startDate, LocalDate endDate,
                                                                Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startTime"));

        Page<ChargingRecord> recordPage;

        // 根据查询条件查询
        if (status != null && startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            recordPage = chargingRecordRepository.findByUserIdAndStatusAndDateRange(
                    userId, status, startDateTime, endDateTime, pageable);
        } else if (status != null) {
            recordPage = chargingRecordRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            recordPage = chargingRecordRepository.findByUserIdAndDateRange(
                    userId, startDateTime, endDateTime, pageable);
        } else {
            recordPage = chargingRecordRepository.findByUserId(userId, pageable);
        }

        return recordPage.map(record -> {
            ChargingPile pile = chargingPileRepository.findById(record.getChargingPileId()).orElse(null);
            Vehicle vehicle = record.getVehicleId() != null ?
                    vehicleRepository.findById(record.getVehicleId()).orElse(null) : null;
            return convertToResponse(record, pile, vehicle);
        });
    }

    @Override
    public ChargingRecordResponse getChargingRecordDetail(Long userId, Long recordId) {
        // 1. 查询充电记录
        ChargingRecord chargingRecord = chargingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_RECORD_NOT_FOUND));

        // 2. 验证记录是否属于当前用户
        if (!chargingRecord.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 关联查询充电桩信息、车辆信息
        ChargingPile pile = chargingPileRepository.findById(chargingRecord.getChargingPileId()).orElse(null);
        Vehicle vehicle = chargingRecord.getVehicleId() != null ?
                vehicleRepository.findById(chargingRecord.getVehicleId()).orElse(null) : null;

        // 4. 构建响应（包含费用明细）
        ChargingRecordResponse response = convertToResponse(chargingRecord, pile, vehicle);

        // 5. 如果是已完成状态，添加费用明细
        if (chargingRecord.getStatus() == ChargingRecordStatus.COMPLETED && pile != null) {
            try {
                var priceConfig = priceConfigService.getCurrentPriceConfig(pile.getType().name());
                response.setPricePerKwh(priceConfig.getPricePerKwh());
                response.setServiceFee(priceConfig.getServiceFee());

                // 计算费用明细
                BigDecimal electricityFee = chargingRecord.getElectricQuantity()
                        .multiply(priceConfig.getPricePerKwh())
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal serviceFeeTotal = chargingRecord.getElectricQuantity()
                        .multiply(priceConfig.getServiceFee())
                        .setScale(2, RoundingMode.HALF_UP);

                ChargingRecordResponse.FeeBreakdown breakdown = ChargingRecordResponse.FeeBreakdown.builder()
                        .electricityFee(electricityFee)
                        .serviceFee(serviceFeeTotal)
                        .build();
                response.setFeeBreakdown(breakdown);
            } catch (Exception e) {
                log.warn("获取费用配置失败: {}", e.getMessage());
            }
        }

        return response;
    }

    @Override
    public ChargingRecordResponse getCurrentChargingRecord(Long userId) {
        Optional<ChargingRecord> recordOpt = chargingRecordRepository.findByUserIdAndStatus(
                userId, ChargingRecordStatus.CHARGING);

        if (recordOpt.isEmpty()) {
            return null;
        }

        ChargingRecord record = recordOpt.get();
        ChargingPile pile = chargingPileRepository.findById(record.getChargingPileId()).orElse(null);
        Vehicle vehicle = record.getVehicleId() != null ?
                vehicleRepository.findById(record.getVehicleId()).orElse(null) : null;

        return convertToResponse(record, pile, vehicle);
    }

    @Override
    public ChargingStatisticsMonthlyResponse getMonthlyStatistics(Long userId, Integer year, Integer month) {
        // 查询指定年月的已完成充电记录
        List<ChargingRecord> records = chargingRecordRepository.findCompletedRecordsByMonth(userId, year, month);

        // 统计总数据
        int totalCount = records.size();
        BigDecimal totalElectricQuantity = records.stream()
                .map(ChargingRecord::getElectricQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFee = records.stream()
                .map(ChargingRecord::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按日期分组统计
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, List<ChargingRecord>> groupedByDate = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStartTime().format(dateFormatter)));

        List<ChargingStatisticsMonthlyResponse.DailyRecord> dailyRecords = groupedByDate.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<ChargingRecord> dayRecords = entry.getValue();

                    int count = dayRecords.size();
                    BigDecimal electricQuantity = dayRecords.stream()
                            .map(ChargingRecord::getElectricQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal fee = dayRecords.stream()
                            .map(ChargingRecord::getFee)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ChargingStatisticsMonthlyResponse.DailyRecord.builder()
                            .date(date)
                            .count(count)
                            .electricQuantity(electricQuantity)
                            .fee(fee)
                            .build();
                })
                .sorted(Comparator.comparing(ChargingStatisticsMonthlyResponse.DailyRecord::getDate))
                .collect(Collectors.toList());

        return ChargingStatisticsMonthlyResponse.builder()
                .year(year)
                .month(month)
                .totalCount(totalCount)
                .totalElectricQuantity(totalElectricQuantity)
                .totalFee(totalFee)
                .records(dailyRecords)
                .build();
    }

    @Override
    public ChargingStatisticsYearlyResponse getYearlyStatistics(Long userId, Integer year) {
        // 查询指定年份的已完成充电记录
        List<ChargingRecord> records = chargingRecordRepository.findCompletedRecordsByYear(userId, year);

        // 统计总数据
        int totalCount = records.size();
        BigDecimal totalElectricQuantity = records.stream()
                .map(ChargingRecord::getElectricQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFee = records.stream()
                .map(ChargingRecord::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按月份分组统计
        Map<Integer, List<ChargingRecord>> groupedByMonth = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStartTime().getMonthValue()));

        List<ChargingStatisticsYearlyResponse.MonthlyRecord> monthlyRecords = groupedByMonth.entrySet().stream()
                .map(entry -> {
                    Integer month = entry.getKey();
                    List<ChargingRecord> monthRecords = entry.getValue();

                    int count = monthRecords.size();
                    BigDecimal electricQuantity = monthRecords.stream()
                            .map(ChargingRecord::getElectricQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal fee = monthRecords.stream()
                            .map(ChargingRecord::getFee)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ChargingStatisticsYearlyResponse.MonthlyRecord.builder()
                            .month(month)
                            .count(count)
                            .electricQuantity(electricQuantity)
                            .fee(fee)
                            .build();
                })
                .sorted(Comparator.comparing(ChargingStatisticsYearlyResponse.MonthlyRecord::getMonth))
                .collect(Collectors.toList());

        return ChargingStatisticsYearlyResponse.builder()
                .year(year)
                .totalCount(totalCount)
                .totalElectricQuantity(totalElectricQuantity)
                .totalFee(totalFee)
                .records(monthlyRecords)
                .build();
    }

    @Override
    public Page<ChargingRecordResponse> getAllChargingRecords(Long userId, Long chargingPileId,
                                                                ChargingRecordStatus status,
                                                                LocalDate startDate, LocalDate endDate,
                                                                Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startTime"));

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Page<ChargingRecord> recordPage = chargingRecordRepository.findByAdminFilters(
                userId, chargingPileId, status, startDateTime, endDateTime, pageable);

        return recordPage.map(record -> {
            ChargingPile pile = chargingPileRepository.findById(record.getChargingPileId()).orElse(null);
            Vehicle vehicle = record.getVehicleId() != null ?
                    vehicleRepository.findById(record.getVehicleId()).orElse(null) : null;
            return convertToResponse(record, pile, vehicle);
        });
    }

    /**
     * 转换为响应DTO
     */
    private ChargingRecordResponse convertToResponse(ChargingRecord record, ChargingPile pile, Vehicle vehicle) {
        return ChargingRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .chargingPileId(record.getChargingPileId())
                .pileName(pile != null ? pile.getCode() : null)
                .pileLocation(pile != null ? pile.getLocation() : null)
                .pileType(pile != null ? pile.getType().name() : null)
                .vehicleId(record.getVehicleId())
                .vehicleLicensePlate(vehicle != null ? vehicle.getLicensePlate() : null)
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .duration(record.getDuration())
                .electricQuantity(record.getElectricQuantity())
                .fee(record.getFee())
                .status(record.getStatus())
                .statusDesc(record.getStatus().getDescription())
                .createdTime(record.getCreatedTime())
                .updatedTime(record.getUpdatedTime())
                .build();
    }
}
