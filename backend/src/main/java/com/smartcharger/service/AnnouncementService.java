package com.smartcharger.service;

import com.smartcharger.dto.request.AnnouncementCreateRequest;
import com.smartcharger.dto.request.AnnouncementUpdateRequest;
import com.smartcharger.dto.response.AnnouncementClientResponse;
import com.smartcharger.dto.response.AnnouncementResponse;
import com.smartcharger.entity.enums.AnnouncementStatus;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {

    /**
     * 创建公告（管理端）
     */
    AnnouncementResponse createAnnouncement(Long adminId, AnnouncementCreateRequest request);

    /**
     * 更新公告（管理端）
     */
    AnnouncementResponse updateAnnouncement(Long announcementId, AnnouncementUpdateRequest request);

    /**
     * 删除公告（管理端）
     */
    void deleteAnnouncement(Long announcementId);

    /**
     * 发布公告（管理端）
     */
    AnnouncementResponse publishAnnouncement(Long announcementId);

    /**
     * 下线公告（管理端）
     */
    AnnouncementResponse unpublishAnnouncement(Long announcementId);

    /**
     * 查询公告列表（管理端）
     */
    Page<AnnouncementResponse> getAdminAnnouncementList(AnnouncementStatus status, String keyword,
                                                          Integer page, Integer size);

    /**
     * 查询公告详情（管理端）
     */
    AnnouncementResponse getAdminAnnouncementDetail(Long announcementId);

    /**
     * 查询公告列表（车主端）
     */
    Page<AnnouncementClientResponse> getClientAnnouncementList(Integer page, Integer size);

    /**
     * 查询公告详情（车主端）
     */
    AnnouncementClientResponse getClientAnnouncementDetail(Long announcementId);

    /**
     * 查询最新公告（车主端）
     */
    List<AnnouncementClientResponse> getLatestAnnouncements(Integer limit);
}
