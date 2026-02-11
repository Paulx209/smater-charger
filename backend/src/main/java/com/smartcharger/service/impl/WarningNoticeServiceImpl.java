package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ThresholdConfigRequest;
import com.smartcharger.dto.response.ThresholdConfigResponse;
import com.smartcharger.dto.response.UnreadCountResponse;
import com.smartcharger.dto.response.WarningNoticeResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.SystemConfig;
import com.smartcharger.entity.WarningNotice;
import com.smartcharger.entity.enums.SendStatus;
import com.smartcharger.entity.enums.WarningNoticeType;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.SystemConfigRepository;
import com.smartcharger.repository.WarningNoticeRepository;
import com.smartcharger.service.WarningNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 预警通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarningNoticeServiceImpl implements WarningNoticeService {

    private final WarningNoticeRepository warningNoticeRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final ChargingPileRepository chargingPileRepository;

    private static final String CONFIG_KEY_OVERTIME_THRESHOLD = "overtime_warning_threshold";
    private static final Integer DEFAULT_THRESHOLD = 30;

    @Override
    public Page<WarningNoticeResponse> getWarningNoticeList(Long userId, WarningNoticeType type,
                                                              Integer isRead, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<WarningNotice> noticePage;

        // 根据查询条件查询
        if (type != null && isRead != null) {
            noticePage = warningNoticeRepository.findByUserIdAndTypeAndIsRead(userId, type, isRead, pageable);
        } else if (type != null) {
            noticePage = warningNoticeRepository.findByUserIdAndType(userId, type, pageable);
        } else if (isRead != null) {
            noticePage = warningNoticeRepository.findByUserIdAndIsRead(userId, isRead, pageable);
        } else {
            noticePage = warningNoticeRepository.findByUserId(userId, pageable);
        }

        return noticePage.map(this::convertToResponse);
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        Integer count = warningNoticeRepository.countByUserIdAndIsRead(userId, 0);
        return UnreadCountResponse.builder()
                .unreadCount(count)
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long noticeId) {
        // 验证通知是否存在且属于当前用户
        WarningNotice notice = warningNoticeRepository.findByIdAndUserId(noticeId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.WARNING_NOTICE_NOT_FOUND));

        // 更新为已读
        notice.setIsRead(1);
        warningNoticeRepository.save(notice);

        log.info("标记通知为已读: userId={}, noticeId={}", userId, noticeId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        warningNoticeRepository.markAllAsRead(userId);
        log.info("标记所有通知为已读: userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteWarningNotice(Long userId, Long noticeId) {
        // 验证通知是否存在且属于当前用户
        WarningNotice notice = warningNoticeRepository.findByIdAndUserId(noticeId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.WARNING_NOTICE_NOT_FOUND));

        warningNoticeRepository.delete(notice);
        log.info("删除通知: userId={}, noticeId={}", userId, noticeId);
    }

    @Override
    public ThresholdConfigResponse getThresholdConfig(Long userId) {
        // 先查询用户级配置
        Optional<SystemConfig> userConfig = systemConfigRepository.findByUserIdAndConfigKey(
                userId, CONFIG_KEY_OVERTIME_THRESHOLD);

        if (userConfig.isPresent()) {
            Integer threshold = Integer.parseInt(userConfig.get().getConfigValue());
            return ThresholdConfigResponse.builder()
                    .threshold(threshold)
                    .build();
        }

        // 如果用户未设置，查询系统级配置
        Optional<SystemConfig> systemConfig = systemConfigRepository.findByUserIdIsNullAndConfigKey(
                CONFIG_KEY_OVERTIME_THRESHOLD);

        if (systemConfig.isPresent()) {
            Integer threshold = Integer.parseInt(systemConfig.get().getConfigValue());
            return ThresholdConfigResponse.builder()
                    .threshold(threshold)
                    .build();
        }

        // 如果系统级配置也不存在，返回默认值
        return ThresholdConfigResponse.builder()
                .threshold(DEFAULT_THRESHOLD)
                .build();
    }

    @Override
    @Transactional
    public ThresholdConfigResponse setThresholdConfig(Long userId, ThresholdConfigRequest request) {
        // 查询用户级配置是否存在
        Optional<SystemConfig> configOpt = systemConfigRepository.findByUserIdAndConfigKey(
                userId, CONFIG_KEY_OVERTIME_THRESHOLD);

        SystemConfig config;
        if (configOpt.isPresent()) {
            // 更新现有配置
            config = configOpt.get();
            config.setConfigValue(request.getThreshold().toString());
        } else {
            // 创建新配置
            config = new SystemConfig();
            config.setUserId(userId);
            config.setConfigKey(CONFIG_KEY_OVERTIME_THRESHOLD);
            config.setConfigValue(request.getThreshold().toString());
            config.setDescription("超时占位预警阈值（分钟）");
        }

        systemConfigRepository.save(config);
        log.info("设置预警阈值: userId={}, threshold={}", userId, request.getThreshold());

        return ThresholdConfigResponse.builder()
                .threshold(request.getThreshold())
                .build();
    }

    @Override
    @Transactional
    public void createOvertimeWarning(Long userId, Long chargingPileId, Long chargingRecordId,
                                       String pileName, Integer duration) {
        // 检查是否已经发送过该充电记录的超时预警
        Optional<WarningNotice> existingNotice = warningNoticeRepository.findByChargingRecordIdAndType(
                chargingRecordId, WarningNoticeType.OVERTIME_WARNING);

        if (existingNotice.isPresent()) {
            log.debug("该充电记录已发送过超时预警: recordId={}", chargingRecordId);
            return;
        }

        // 创建预警通知
        String content = String.format("您在充电桩%s的充电已完成%d分钟，请尽快移车，避免占用充电资源。",
                pileName, duration);

        WarningNotice notice = new WarningNotice();
        notice.setUserId(userId);
        notice.setChargingPileId(chargingPileId);
        notice.setChargingRecordId(chargingRecordId);
        notice.setType(WarningNoticeType.OVERTIME_WARNING);
        notice.setContent(content);
        notice.setOvertimeMinutes(duration);
        notice.setIsRead(0);
        notice.setSendStatus(SendStatus.PENDING);
        notice.setCreatedTime(LocalDateTime.now());

        warningNoticeRepository.save(notice);

        log.info("创建超时预警通知: userId={}, recordId={}, duration={}min", userId, chargingRecordId, duration);

        // TODO: 异步发送短信通知
        // sendSmsNotification(userId, content);
    }

    /**
     * 转换为响应DTO
     */
    private WarningNoticeResponse convertToResponse(WarningNotice notice) {
        ChargingPile pile = null;
        if (notice.getChargingPileId() != null) {
            pile = chargingPileRepository.findById(notice.getChargingPileId()).orElse(null);
        }

        return WarningNoticeResponse.builder()
                .id(notice.getId())
                .userId(notice.getUserId())
                .chargingPileId(notice.getChargingPileId())
                .pileName(pile != null ? pile.getCode() : null)
                .pileLocation(pile != null ? pile.getLocation() : null)
                .chargingRecordId(notice.getChargingRecordId())
                .type(notice.getType())
                .typeDesc(notice.getType().getDescription())
                .content(notice.getContent())
                .isRead(notice.getIsRead())
                .sendStatus(notice.getSendStatus())
                .sendStatusDesc(notice.getSendStatus().getDescription())
                .createdTime(notice.getCreatedTime())
                .build();
    }
}
