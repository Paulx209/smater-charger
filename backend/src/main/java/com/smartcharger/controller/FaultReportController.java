package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.FaultReportCreateRequest;
import com.smartcharger.dto.request.FaultReportHandleRequest;
import com.smartcharger.dto.response.FaultReportResponse;
import com.smartcharger.dto.response.FaultStatisticsResponse;
import com.smartcharger.entity.enums.FaultReportStatus;
import com.smartcharger.service.FaultReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 故障报修控制器
 */
@Slf4j
@RestController
@RequestMapping("/fault-report")
@RequiredArgsConstructor
public class FaultReportController {

    private final FaultReportService faultReportService;

    // ==================== 车主端接口 ====================

    /**
     * 提交故障报修
     */
    @PostMapping
    public Result<FaultReportResponse> createFaultReport(@Valid @RequestBody FaultReportCreateRequest request) {
        Long userId = getCurrentUserId();
        FaultReportResponse response = faultReportService.createFaultReport(userId, request);
        return Result.success(response);
    }

    /**
     * 查询报修列表（车主端）
     */
    @GetMapping
    public Result<Page<FaultReportResponse>> getFaultReportList(
            @RequestParam(required = false) FaultReportStatus status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        Page<FaultReportResponse> result = faultReportService.getFaultReportList(
                userId, status, page, size);
        return Result.success(result);
    }

    /**
     * 查询报修详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<FaultReportResponse> getFaultReportDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        FaultReportResponse response = faultReportService.getFaultReportDetail(userId, id, false);
        return Result.success(response);
    }

    /**
     * 取消报修
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> cancelFaultReport(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        faultReportService.cancelFaultReport(userId, id);
        return Result.success(null);
    }

    // ==================== 管理端接口 ====================

    /**
     * 查询所有报修列表（管理端）
     */
    @GetMapping("/admin/all")
    public Result<Page<FaultReportResponse>> getAllFaultReports(
            @RequestParam(required = false) Long chargingPileId,
            @RequestParam(required = false) FaultReportStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FaultReportResponse> result = faultReportService.getAllFaultReports(
                chargingPileId, status, startDate, endDate, page, size);
        return Result.success(result);
    }

    /**
     * 查询报修详情（管理端）
     */
    @GetMapping("/admin/{id:\\d+}")
    public Result<FaultReportResponse> getAdminFaultReportDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        FaultReportResponse response = faultReportService.getFaultReportDetail(userId, id, true);
        return Result.success(response);
    }

    /**
     * 处理故障报修（管理端）
     */
    @PutMapping("/admin/{id:\\d+}/handle")
    public Result<FaultReportResponse> handleFaultReport(@PathVariable Long id,
                                                           @Valid @RequestBody FaultReportHandleRequest request) {
        Long handlerId = getCurrentUserId();
        FaultReportResponse response = faultReportService.handleFaultReport(handlerId, id, request);
        return Result.success(response);
    }

    /**
     * 故障统计（管理端）
     */
    @GetMapping("/admin/statistics")
    public Result<FaultStatisticsResponse> getFaultStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        FaultStatisticsResponse response = faultReportService.getFaultStatistics(startDate, endDate);
        return Result.success(response);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }
}
