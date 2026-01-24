package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.ChargingRecordEndRequest;
import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;
import com.smartcharger.dto.response.ChargingStatisticsMonthlyResponse;
import com.smartcharger.dto.response.ChargingStatisticsYearlyResponse;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.service.ChargingRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 充电记录管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/charging-record")
@RequiredArgsConstructor
public class ChargingRecordController {

    private final ChargingRecordService chargingRecordService;

    /**
     * 开始充电
     */
    @PostMapping("/start")
    public Result<ChargingRecordResponse> startCharging(@Valid @RequestBody ChargingRecordStartRequest request) {
        Long userId = getCurrentUserId();
        ChargingRecordResponse response = chargingRecordService.startCharging(userId, request);
        return Result.success(response);
    }

    /**
     * 结束充电
     */
    @PostMapping("/end/{id:\\d+}")
    public Result<ChargingRecordResponse> endCharging(@PathVariable Long id,
                                                        @Valid @RequestBody ChargingRecordEndRequest request) {
        Long userId = getCurrentUserId();
        ChargingRecordResponse response = chargingRecordService.endCharging(userId, id, request);
        return Result.success(response);
    }

    /**
     * 查询充电记录列表
     */
    @GetMapping
    public Result<Page<ChargingRecordResponse>> getChargingRecordList(
            @RequestParam(required = false) ChargingRecordStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        Page<ChargingRecordResponse> result = chargingRecordService.getChargingRecordList(
                userId, status, startDate, endDate, page, size);
        return Result.success(result);
    }

    /**
     * 查询充电记录详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<ChargingRecordResponse> getChargingRecordDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ChargingRecordResponse response = chargingRecordService.getChargingRecordDetail(userId, id);
        return Result.success(response);
    }

    /**
     * 查询当前充电记录
     */
    @GetMapping("/current")
    public Result<ChargingRecordResponse> getCurrentChargingRecord() {
        Long userId = getCurrentUserId();
        ChargingRecordResponse response = chargingRecordService.getCurrentChargingRecord(userId);
        return Result.success(response);
    }

    /**
     * 月度统计
     */
    @GetMapping("/statistics/monthly")
    public Result<ChargingStatisticsMonthlyResponse> getMonthlyStatistics(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long userId = getCurrentUserId();
        ChargingStatisticsMonthlyResponse response = chargingRecordService.getMonthlyStatistics(userId, year, month);
        return Result.success(response);
    }

    /**
     * 年度统计
     */
    @GetMapping("/statistics/yearly")
    public Result<ChargingStatisticsYearlyResponse> getYearlyStatistics(@RequestParam Integer year) {
        Long userId = getCurrentUserId();
        ChargingStatisticsYearlyResponse response = chargingRecordService.getYearlyStatistics(userId, year);
        return Result.success(response);
    }

    /**
     * 管理端：查询所有充电记录
     */
    @GetMapping("/admin/all")
    public Result<Page<ChargingRecordResponse>> getAllChargingRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long chargingPileId,
            @RequestParam(required = false) ChargingRecordStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ChargingRecordResponse> result = chargingRecordService.getAllChargingRecords(
                userId, chargingPileId, status, startDate, endDate, page, size);
        return Result.success(result);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }
}
