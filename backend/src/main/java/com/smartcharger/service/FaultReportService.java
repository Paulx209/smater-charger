package com.smartcharger.service;

import com.smartcharger.dto.request.FaultReportCreateRequest;
import com.smartcharger.dto.request.FaultReportHandleRequest;
import com.smartcharger.dto.response.FaultReportResponse;
import com.smartcharger.dto.response.FaultStatisticsResponse;
import com.smartcharger.entity.enums.FaultReportStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

/**
 * 故障报修服务接口
 */
public interface FaultReportService {

    /**
     * 提交故障报修
     */
    FaultReportResponse createFaultReport(Long userId, FaultReportCreateRequest request);

    /**
     * 查询报修列表（车主端）
     */
    Page<FaultReportResponse> getFaultReportList(Long userId, FaultReportStatus status,
                                                   Integer page, Integer size);

    /**
     * 查询报修详情
     */
    FaultReportResponse getFaultReportDetail(Long userId, Long faultReportId, boolean isAdmin);

    /**
     * 取消报修
     */
    void cancelFaultReport(Long userId, Long faultReportId);

    /**
     * 查询所有报修列表（管理端）
     */
    Page<FaultReportResponse> getAllFaultReports(Long chargingPileId, FaultReportStatus status,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   Integer page, Integer size);

    /**
     * 处理故障报修（管理端）
     */
    FaultReportResponse handleFaultReport(Long handlerId, Long faultReportId,
                                           FaultReportHandleRequest request);

    /**
     * 故障统计（管理端）
     */
    FaultStatisticsResponse getFaultStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
