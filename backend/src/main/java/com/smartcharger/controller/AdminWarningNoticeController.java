package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.AdminWarningNoticeReadRequest;
import com.smartcharger.dto.request.ThresholdConfigRequest;
import com.smartcharger.dto.response.ThresholdConfigResponse;
import com.smartcharger.dto.response.WarningNoticeResponse;
import com.smartcharger.entity.enums.WarningNoticeType;
import com.smartcharger.service.WarningNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin/warning-notices")
@RequiredArgsConstructor
public class AdminWarningNoticeController {

    private final WarningNoticeService warningNoticeService;

    @GetMapping
    public Result<Page<WarningNoticeResponse>> getAdminWarningNoticeList(
            @RequestParam(required = false) WarningNoticeType type,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Admin warning-notice list query: type={}, isRead={}, userId={}, startDate={}, endDate={}, page={}, size={}",
                type, isRead, userId, startDate, endDate, page, size);
        return Result.success(warningNoticeService.getAdminWarningNoticeList(
                type, isRead, userId, startDate, endDate, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public Result<WarningNoticeResponse> getAdminWarningNoticeDetail(@PathVariable Long id) {
        log.info("Admin warning-notice detail query: id={}", id);
        return Result.success(warningNoticeService.getAdminWarningNoticeById(id));
    }

    @PutMapping("/read")
    public Result<Void> markAdminWarningsAsRead(@Valid @RequestBody AdminWarningNoticeReadRequest request) {
        log.info("Admin batch read warning notices: ids={}", request.getIds());
        warningNoticeService.markAdminWarningsAsRead(request.getIds());
        return Result.success();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> deleteAdminWarningNotice(@PathVariable Long id) {
        log.info("Admin delete warning notice: id={}", id);
        warningNoticeService.deleteAdminWarningNotice(id);
        return Result.success();
    }

    @GetMapping("/config/threshold")
    public Result<ThresholdConfigResponse> getAdminThresholdConfig() {
        return Result.success(warningNoticeService.getAdminThresholdConfig());
    }

    @PutMapping("/config/threshold")
    public Result<ThresholdConfigResponse> setAdminThresholdConfig(@Valid @RequestBody ThresholdConfigRequest request) {
        return Result.success(warningNoticeService.setAdminThresholdConfig(request));
    }
}