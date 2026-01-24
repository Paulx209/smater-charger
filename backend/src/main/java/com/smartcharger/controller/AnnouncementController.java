package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.AnnouncementCreateRequest;
import com.smartcharger.dto.request.AnnouncementUpdateRequest;
import com.smartcharger.dto.response.AnnouncementClientResponse;
import com.smartcharger.dto.response.AnnouncementResponse;
import com.smartcharger.entity.enums.AnnouncementStatus;
import com.smartcharger.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // ==================== 管理端接口 ====================

    /**
     * 创建公告（管理端）
     */
    @PostMapping("/admin")
    public Result<AnnouncementResponse> createAnnouncement(@Valid @RequestBody AnnouncementCreateRequest request) {
        Long adminId = getCurrentUserId();
        AnnouncementResponse response = announcementService.createAnnouncement(adminId, request);
        return Result.success(response);
    }

    /**
     * 更新公告（管理端）
     */
    @PutMapping("/admin/{id:\\d+}")
    public Result<AnnouncementResponse> updateAnnouncement(@PathVariable Long id,
                                                             @Valid @RequestBody AnnouncementUpdateRequest request) {
        AnnouncementResponse response = announcementService.updateAnnouncement(id, request);
        return Result.success(response);
    }

    /**
     * 删除公告（管理端）
     */
    @DeleteMapping("/admin/{id:\\d+}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success(null);
    }

    /**
     * 发布公告（管理端）
     */
    @PutMapping("/admin/{id:\\d+}/publish")
    public Result<AnnouncementResponse> publishAnnouncement(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.publishAnnouncement(id);
        return Result.success(response);
    }

    /**
     * 下线公告（管理端）
     */
    @PutMapping("/admin/{id:\\d+}/unpublish")
    public Result<AnnouncementResponse> unpublishAnnouncement(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.unpublishAnnouncement(id);
        return Result.success(response);
    }

    /**
     * 查询公告列表（管理端）
     */
    @GetMapping("/admin")
    public Result<Page<AnnouncementResponse>> getAdminAnnouncementList(
            @RequestParam(required = false) AnnouncementStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<AnnouncementResponse> result = announcementService.getAdminAnnouncementList(
                status, keyword, page, size);
        return Result.success(result);
    }

    /**
     * 查询公告详情（管理端）
     */
    @GetMapping("/admin/{id:\\d+}")
    public Result<AnnouncementResponse> getAdminAnnouncementDetail(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.getAdminAnnouncementDetail(id);
        return Result.success(response);
    }

    // ==================== 车主端接口 ====================

    /**
     * 查询公告列表（车主端）
     */
    @GetMapping
    public Result<Page<AnnouncementClientResponse>> getClientAnnouncementList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<AnnouncementClientResponse> result = announcementService.getClientAnnouncementList(page, size);
        return Result.success(result);
    }

    /**
     * 查询公告详情（车主端）
     */
    @GetMapping("/{id:\\d+}")
    public Result<AnnouncementClientResponse> getClientAnnouncementDetail(@PathVariable Long id) {
        AnnouncementClientResponse response = announcementService.getClientAnnouncementDetail(id);
        return Result.success(response);
    }

    /**
     * 查询最新公告（车主端）
     */
    @GetMapping("/latest")
    public Result<List<AnnouncementClientResponse>> getLatestAnnouncements(
            @RequestParam(defaultValue = "3") Integer limit) {
        List<AnnouncementClientResponse> result = announcementService.getLatestAnnouncements(limit);
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
