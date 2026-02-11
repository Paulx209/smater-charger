package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.response.ChargingPileUsageResponse;
import com.smartcharger.dto.response.RevenueStatisticsResponse;
import com.smartcharger.dto.response.StatisticsOverviewResponse;
import com.smartcharger.dto.response.UserActivityResponse;
import com.smartcharger.service.StatisticsAdminService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class StatisticsAdminController {

    private final StatisticsAdminService statisticsAdminService;

    @GetMapping("/overview")
    public Result<StatisticsOverviewResponse> getOverview(
            @RequestParam(defaultValue = "TODAY") String rangeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsAdminService.getOverview(rangeType, startDate, endDate));
    }

    @GetMapping("/charging-pile-usage")
    public Result<ChargingPileUsageResponse> getChargingPileUsage(
            @RequestParam(defaultValue = "TODAY") String rangeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsAdminService.getChargingPileUsage(rangeType, startDate, endDate));
    }

    @GetMapping("/revenue")
    public Result<RevenueStatisticsResponse> getRevenueStatistics(
            @RequestParam(defaultValue = "TODAY") String rangeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsAdminService.getRevenueStatistics(rangeType, startDate, endDate));
    }

    @GetMapping("/user-activity")
    public Result<UserActivityResponse> getUserActivity(
            @RequestParam(defaultValue = "TODAY") String rangeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsAdminService.getUserActivity(rangeType, startDate, endDate));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStatistics(
            @RequestParam(defaultValue = "TODAY") String rangeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {
        Workbook workbook = statisticsAdminService.exportStatistics(rangeType, startDate, endDate);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        String filename = "statistics_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream.toByteArray());
    }
}
