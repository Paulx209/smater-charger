package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.ThresholdConfigRequest;
import com.smartcharger.dto.response.ThresholdConfigResponse;
import com.smartcharger.dto.response.UnreadCountResponse;
import com.smartcharger.dto.response.WarningNoticeResponse;
import com.smartcharger.entity.enums.WarningNoticeType;
import com.smartcharger.service.WarningNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 预警通知管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/warning-notice")
@RequiredArgsConstructor
public class WarningNoticeController {

    private final WarningNoticeService warningNoticeService;

    /**
     * 查询预警通知列表
     */
    @GetMapping
    public Result<Page<WarningNoticeResponse>> getWarningNoticeList(
            @RequestParam(required = false) WarningNoticeType type,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        Page<WarningNoticeResponse> result = warningNoticeService.getWarningNoticeList(
                userId, type, isRead, page, size);
        return Result.success(result);
    }

    /**
     * 查询未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> getUnreadCount() {
        Long userId = getCurrentUserId();
        UnreadCountResponse response = warningNoticeService.getUnreadCount(userId);
        return Result.success(response);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id:\\d+}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        warningNoticeService.markAsRead(userId, id);
        return Result.success(null);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        warningNoticeService.markAllAsRead(userId);
        return Result.success(null);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> deleteWarningNotice(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        warningNoticeService.deleteWarningNotice(userId, id);
        return Result.success(null);
    }

    /**
     * 获取预警阈值配置
     */
    @GetMapping("/config/threshold")
    public Result<ThresholdConfigResponse> getThresholdConfig() {
        Long userId = getCurrentUserId();
        ThresholdConfigResponse response = warningNoticeService.getThresholdConfig(userId);
        return Result.success(response);
    }

    /**
     * 设置预警阈值配置
     */
    @PutMapping("/config/threshold")
    public Result<ThresholdConfigResponse> setThresholdConfig(@Valid @RequestBody ThresholdConfigRequest request) {
        Long userId = getCurrentUserId();
        ThresholdConfigResponse response = warningNoticeService.setThresholdConfig(userId, request);
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
