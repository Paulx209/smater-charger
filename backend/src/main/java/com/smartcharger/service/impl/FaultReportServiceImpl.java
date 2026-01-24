package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.FaultReportCreateRequest;
import com.smartcharger.dto.request.FaultReportHandleRequest;
import com.smartcharger.dto.response.FaultReportResponse;
import com.smartcharger.dto.response.FaultStatisticsResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.FaultReport;
import com.smartcharger.entity.User;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.FaultReportStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.FaultReportRepository;
import com.smartcharger.repository.UserRepository;
import com.smartcharger.service.FaultReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 故障报修服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaultReportServiceImpl implements FaultReportService {

    private final FaultReportRepository faultReportRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FaultReportResponse createFaultReport(Long userId, FaultReportCreateRequest request) {
        // 验证充电桩是否存在
        ChargingPile chargingPile = chargingPileRepository.findById(request.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        // 创建故障报修记录
        FaultReport faultReport = new FaultReport();
        faultReport.setUserId(userId);
        faultReport.setChargingPileId(request.getChargingPileId());
        faultReport.setDescription(request.getDescription());
        faultReport.setStatus(FaultReportStatus.PENDING);

        faultReportRepository.save(faultReport);

        // 更新充电桩状态为"故障"
        chargingPile.setStatus(ChargingPileStatus.FAULT);
        chargingPileRepository.save(chargingPile);

        log.info("提交故障报修成功: userId={}, chargingPileId={}, faultReportId={}",
                userId, request.getChargingPileId(), faultReport.getId());

        return convertToResponse(faultReport);
    }

    @Override
    public Page<FaultReportResponse> getFaultReportList(Long userId, FaultReportStatus status,
                                                          Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<FaultReport> faultReportPage = faultReportRepository.findByUserIdAndStatus(
                userId, status, pageable);

        return faultReportPage.map(this::convertToResponse);
    }

    @Override
    public FaultReportResponse getFaultReportDetail(Long userId, Long faultReportId, boolean isAdmin) {
        FaultReport faultReport = faultReportRepository.findById(faultReportId)
                .orElseThrow(() -> new BusinessException(ResultCode.FAULT_REPORT_NOT_FOUND));

        // 如果不是管理员，验证记录是否属于当前用户
        if (!isAdmin && !faultReport.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FAULT_REPORT_NOT_OWNER);
        }

        return convertToResponse(faultReport);
    }

    @Override
    @Transactional
    public void cancelFaultReport(Long userId, Long faultReportId) {
        // 验证报修记录是否存在
        FaultReport faultReport = faultReportRepository.findById(faultReportId)
                .orElseThrow(() -> new BusinessException(ResultCode.FAULT_REPORT_NOT_FOUND));

        // 验证记录是否属于当前用户
        if (!faultReport.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FAULT_REPORT_NOT_OWNER);
        }

        // 验证状态是否为"待处理"
        if (faultReport.getStatus() != FaultReportStatus.PENDING) {
            throw new BusinessException(ResultCode.FAULT_REPORT_CANNOT_CANCEL);
        }

        // 删除报修记录
        faultReportRepository.delete(faultReport);

        // 检查该充电桩是否还有其他待处理的故障报修
        Integer pendingCount = faultReportRepository.countPendingOrProcessingByChargingPileId(
                faultReport.getChargingPileId());

        // 如果没有其他待处理的故障报修，恢复充电桩状态为"空闲"
        if (pendingCount == 0) {
            ChargingPile chargingPile = chargingPileRepository.findById(faultReport.getChargingPileId())
                    .orElse(null);
            if (chargingPile != null && chargingPile.getStatus() == ChargingPileStatus.FAULT) {
                chargingPile.setStatus(ChargingPileStatus.IDLE);
                chargingPileRepository.save(chargingPile);
            }
        }

        log.info("取消故障报修成功: userId={}, faultReportId={}", userId, faultReportId);
    }

    @Override
    public Page<FaultReportResponse> getAllFaultReports(Long chargingPileId, FaultReportStatus status,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<FaultReport> faultReportPage = faultReportRepository.findByConditions(
                chargingPileId, status, startDate, endDate, pageable);

        return faultReportPage.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public FaultReportResponse handleFaultReport(Long handlerId, Long faultReportId,
                                                   FaultReportHandleRequest request) {
        // 验证报修记录是否存在
        FaultReport faultReport = faultReportRepository.findById(faultReportId)
                .orElseThrow(() -> new BusinessException(ResultCode.FAULT_REPORT_NOT_FOUND));

        // 更新状态和处理备注
        faultReport.setStatus(request.getStatus());
        faultReport.setHandleRemark(request.getHandleRemark());
        faultReport.setHandlerId(handlerId);

        faultReportRepository.save(faultReport);

        // 如果状态更新为"已修复"，检查是否需要恢复充电桩状态
        if (request.getStatus() == FaultReportStatus.RESOLVED) {
            Integer pendingCount = faultReportRepository.countPendingOrProcessingByChargingPileId(
                    faultReport.getChargingPileId());

            // 如果没有其他待处理的故障报修，恢复充电桩状态为"空闲"
            if (pendingCount == 0) {
                ChargingPile chargingPile = chargingPileRepository.findById(faultReport.getChargingPileId())
                        .orElse(null);
                if (chargingPile != null && chargingPile.getStatus() == ChargingPileStatus.FAULT) {
                    chargingPile.setStatus(ChargingPileStatus.IDLE);
                    chargingPileRepository.save(chargingPile);
                }
            }
        }

        log.info("处理故障报修成功: handlerId={}, faultReportId={}, status={}",
                handlerId, faultReportId, request.getStatus());

        return convertToResponse(faultReport);
    }

    @Override
    public FaultStatisticsResponse getFaultStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // 统计各状态的数量
        List<Object[]> statusCounts = faultReportRepository.countByStatusInDateRange(startDate, endDate);

        Map<FaultReportStatus, Integer> statusCountMap = new HashMap<>();
        int totalCount = 0;

        for (Object[] row : statusCounts) {
            FaultReportStatus status = (FaultReportStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status, count.intValue());
            totalCount += count.intValue();
        }

        // 计算平均处理时长
        Double avgHandleTime = faultReportRepository.calculateAvgHandleTime(startDate, endDate);

        // 统计故障最多的充电桩（Top 5）
        Pageable topPagable = PageRequest.of(0, 5);
        List<Object[]> topFaultPilesData = faultReportRepository.findTopFaultPiles(
                startDate, endDate, topPagable);

        List<FaultStatisticsResponse.TopFaultPile> topFaultPiles = new ArrayList<>();
        for (Object[] row : topFaultPilesData) {
            Long chargingPileId = (Long) row[0];
            Long faultCount = (Long) row[1];

            ChargingPile pile = chargingPileRepository.findById(chargingPileId).orElse(null);
            String pileName = pile != null ? pile.getCode() : "未知";

            topFaultPiles.add(FaultStatisticsResponse.TopFaultPile.builder()
                    .chargingPileId(chargingPileId)
                    .pileName(pileName)
                    .faultCount(faultCount.intValue())
                    .build());
        }

        return FaultStatisticsResponse.builder()
                .totalCount(totalCount)
                .pendingCount(statusCountMap.getOrDefault(FaultReportStatus.PENDING, 0))
                .processingCount(statusCountMap.getOrDefault(FaultReportStatus.PROCESSING, 0))
                .resolvedCount(statusCountMap.getOrDefault(FaultReportStatus.RESOLVED, 0))
                .avgHandleTime(avgHandleTime != null ? avgHandleTime.intValue() : 0)
                .topFaultPiles(topFaultPiles)
                .build();
    }

    /**
     * 转换为响应DTO
     */
    private FaultReportResponse convertToResponse(FaultReport faultReport) {
        // 查询充电桩信息
        ChargingPile pile = null;
        if (faultReport.getChargingPileId() != null) {
            pile = chargingPileRepository.findById(faultReport.getChargingPileId()).orElse(null);
        }

        // 查询用户信息
        User user = null;
        if (faultReport.getUserId() != null) {
            user = userRepository.findById(faultReport.getUserId()).orElse(null);
        }

        // 查询处理人信息
        User handler = null;
        if (faultReport.getHandlerId() != null) {
            handler = userRepository.findById(faultReport.getHandlerId()).orElse(null);
        }

        return FaultReportResponse.builder()
                .id(faultReport.getId())
                .userId(faultReport.getUserId())
                .userName(user != null ? user.getUsername() : null)
                .userPhone(user != null ? user.getPhone() : null)
                .chargingPileId(faultReport.getChargingPileId())
                .pileName(pile != null ? pile.getCode() : null)
                .pileLocation(pile != null ? pile.getLocation() : null)
                .pileType(pile != null ? pile.getType() : null)
                .description(faultReport.getDescription())
                .status(faultReport.getStatus())
                .statusDesc(faultReport.getStatus().getDescription())
                .handlerId(faultReport.getHandlerId())
                .handlerName(handler != null ? handler.getUsername() : null)
                .handleRemark(faultReport.getHandleRemark())
                .createdTime(faultReport.getCreatedTime())
                .updatedTime(faultReport.getUpdatedTime())
                .build();
    }
}
