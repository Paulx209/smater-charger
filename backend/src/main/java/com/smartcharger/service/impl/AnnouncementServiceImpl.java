package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.AnnouncementCreateRequest;
import com.smartcharger.dto.request.AnnouncementUpdateRequest;
import com.smartcharger.dto.response.AnnouncementClientResponse;
import com.smartcharger.dto.response.AnnouncementResponse;
import com.smartcharger.entity.Announcement;
import com.smartcharger.entity.User;
import com.smartcharger.entity.enums.AnnouncementStatus;
import com.smartcharger.repository.AnnouncementRepository;
import com.smartcharger.repository.UserRepository;
import com.smartcharger.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(Long adminId, AnnouncementCreateRequest request) {
        // 验证时间范围
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new BusinessException(ResultCode.INVALID_TIME_RANGE);
            }
        }

        // 创建公告
        Announcement announcement = new Announcement();
        announcement.setAdminId(adminId);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setStatus(request.getStatus() != null ? request.getStatus() : AnnouncementStatus.DRAFT);
        announcement.setStartTime(request.getStartTime());
        announcement.setEndTime(request.getEndTime());

        announcementRepository.save(announcement);

        log.info("创建公告成功: adminId={}, announcementId={}, status={}",
                adminId, announcement.getId(), announcement.getStatus());

        return convertToResponse(announcement);
    }

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long announcementId, AnnouncementUpdateRequest request) {
        // 查询公告
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND));

        // 验证时间范围
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : announcement.getStartTime();
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : announcement.getEndTime();
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.INVALID_TIME_RANGE);
        }

        // 更新公告
        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            announcement.setContent(request.getContent());
        }
        if (request.getStartTime() != null) {
            announcement.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            announcement.setEndTime(request.getEndTime());
        }

        announcementRepository.save(announcement);

        log.info("更新公告成功: announcementId={}", announcementId);

        return convertToResponse(announcement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        // 查询公告
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND));

        announcementRepository.delete(announcement);

        log.info("删除公告成功: announcementId={}", announcementId);
    }

    @Override
    @Transactional
    public AnnouncementResponse publishAnnouncement(Long announcementId) {
        // 查询公告
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND));

        // 验证状态
        if (announcement.getStatus() != AnnouncementStatus.DRAFT) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_DRAFT);
        }

        // 更新状态
        announcement.setStatus(AnnouncementStatus.PUBLISHED);
        announcementRepository.save(announcement);

        log.info("发布公告成功: announcementId={}", announcementId);

        return convertToResponse(announcement);
    }

    @Override
    @Transactional
    public AnnouncementResponse unpublishAnnouncement(Long announcementId) {
        // 查询公告
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND));

        // 验证状态
        if (announcement.getStatus() != AnnouncementStatus.PUBLISHED) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_PUBLISHED);
        }

        // 更新状态
        announcement.setStatus(AnnouncementStatus.DRAFT);
        announcementRepository.save(announcement);

        log.info("下线公告成功: announcementId={}", announcementId);

        return convertToResponse(announcement);
    }

    @Override
    public Page<AnnouncementResponse> getAdminAnnouncementList(AnnouncementStatus status, String keyword,
                                                                  Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<Announcement> announcementPage = announcementRepository.findByStatusAndKeyword(
                status, keyword, pageable);

        return announcementPage.map(this::convertToResponse);
    }

    @Override
    public AnnouncementResponse getAdminAnnouncementDetail(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND));

        return convertToResponse(announcement);
    }

    @Override
    public Page<AnnouncementClientResponse> getClientAnnouncementList(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        LocalDateTime currentTime = LocalDateTime.now();
        Page<Announcement> announcementPage = announcementRepository.findPublishedAndValid(
                currentTime, pageable);

        return announcementPage.map(this::convertToClientResponse);
    }

    @Override
    public AnnouncementClientResponse getClientAnnouncementDetail(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND));

        // 验证公告状态和有效期
        if (announcement.getStatus() != AnnouncementStatus.PUBLISHED) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_AVAILABLE);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        if (announcement.getStartTime() != null && currentTime.isBefore(announcement.getStartTime())) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_AVAILABLE);
        }
        if (announcement.getEndTime() != null && currentTime.isAfter(announcement.getEndTime())) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_AVAILABLE);
        }

        return convertToClientResponse(announcement);
    }

    @Override
    public List<AnnouncementClientResponse> getLatestAnnouncements(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);

        LocalDateTime currentTime = LocalDateTime.now();
        List<Announcement> announcements = announcementRepository.findLatestPublishedAndValid(
                currentTime, pageable);

        return announcements.stream()
                .map(this::convertToClientResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为管理端响应DTO
     */
    private AnnouncementResponse convertToResponse(Announcement announcement) {
        // 查询管理员信息
        String adminName = null;
        if (announcement.getAdminId() != null) {
            User admin = userRepository.findById(announcement.getAdminId()).orElse(null);
            if (admin != null) {
                adminName = admin.getUsername();
            }
        }

        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .adminId(announcement.getAdminId())
                .adminName(adminName)
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .status(announcement.getStatus())
                .statusDesc(announcement.getStatus().getDescription())
                .startTime(announcement.getStartTime())
                .endTime(announcement.getEndTime())
                .createdTime(announcement.getCreatedTime())
                .updatedTime(announcement.getUpdatedTime())
                .build();
    }

    /**
     * 转换为车主端响应DTO
     */
    private AnnouncementClientResponse convertToClientResponse(Announcement announcement) {
        return AnnouncementClientResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .startTime(announcement.getStartTime())
                .endTime(announcement.getEndTime())
                .createdTime(announcement.getCreatedTime())
                .build();
    }
}
