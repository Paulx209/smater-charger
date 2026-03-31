package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ThresholdConfigRequest;
import com.smartcharger.dto.response.ThresholdConfigResponse;
import com.smartcharger.dto.response.UnreadCountResponse;
import com.smartcharger.dto.response.WarningNoticeResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.SystemConfig;
import com.smartcharger.entity.User;
import com.smartcharger.entity.WarningNotice;
import com.smartcharger.entity.enums.SendStatus;
import com.smartcharger.entity.enums.WarningNoticeType;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.SystemConfigRepository;
import com.smartcharger.repository.UserRepository;
import com.smartcharger.repository.WarningNoticeRepository;
import com.smartcharger.service.WarningNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarningNoticeServiceImpl implements WarningNoticeService {

    private static final String CONFIG_KEY_OVERTIME_THRESHOLD = "overtime_warning_threshold";
    private static final Integer DEFAULT_THRESHOLD = 30;

    private final WarningNoticeRepository warningNoticeRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final UserRepository userRepository;

    @Override
    public Page<WarningNoticeResponse> getWarningNoticeList(Long userId, WarningNoticeType type,
                                                            Integer isRead, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<WarningNotice> noticePage;
        if (type != null && isRead != null) {
            noticePage = warningNoticeRepository.findByUserIdAndTypeAndIsRead(userId, type, isRead, pageable);
        } else if (type != null) {
            noticePage = warningNoticeRepository.findByUserIdAndType(userId, type, pageable);
        } else if (isRead != null) {
            noticePage = warningNoticeRepository.findByUserIdAndIsRead(userId, isRead, pageable);
        } else {
            noticePage = warningNoticeRepository.findByUserId(userId, pageable);
        }

        Map<Long, ChargingPile> pileMap = batchFetchPiles(noticePage.getContent());
        User user = userRepository.findById(userId).orElse(null);

        List<WarningNoticeResponse> responses = noticePage.getContent().stream()
                .map(notice -> convertToResponse(notice, pileMap.get(notice.getChargingPileId()), user))
                .toList();

        return new PageImpl<>(responses, pageable, noticePage.getTotalElements());
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
        WarningNotice notice = warningNoticeRepository.findByIdAndUserId(noticeId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.WARNING_NOTICE_NOT_FOUND));

        notice.setIsRead(1);
        warningNoticeRepository.save(notice);
        log.info("Mark warning notice as read: userId={}, noticeId={}", userId, noticeId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        warningNoticeRepository.markAllAsRead(userId);
        log.info("Mark all warning notices as read: userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteWarningNotice(Long userId, Long noticeId) {
        WarningNotice notice = warningNoticeRepository.findByIdAndUserId(noticeId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.WARNING_NOTICE_NOT_FOUND));

        warningNoticeRepository.delete(notice);
        log.info("Delete warning notice: userId={}, noticeId={}", userId, noticeId);
    }

    @Override
    public ThresholdConfigResponse getThresholdConfig(Long userId) {
        Optional<SystemConfig> userConfig = systemConfigRepository.findByUserIdAndConfigKey(
                userId, CONFIG_KEY_OVERTIME_THRESHOLD);

        if (userConfig.isPresent()) {
            return ThresholdConfigResponse.builder()
                    .threshold(Integer.parseInt(userConfig.get().getConfigValue()))
                    .build();
        }

        return getSystemThresholdResponse();
    }

    @Override
    @Transactional
    public ThresholdConfigResponse setThresholdConfig(Long userId, ThresholdConfigRequest request) {
        Optional<SystemConfig> configOpt = systemConfigRepository.findByUserIdAndConfigKey(
                userId, CONFIG_KEY_OVERTIME_THRESHOLD);

        SystemConfig config;
        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setConfigValue(request.getThreshold().toString());
        } else {
            config = new SystemConfig();
            config.setUserId(userId);
            config.setConfigKey(CONFIG_KEY_OVERTIME_THRESHOLD);
            config.setConfigValue(request.getThreshold().toString());
            config.setDescription("用户级超时占位预警阈值（分钟）");
        }

        systemConfigRepository.save(config);
        log.info("Set user warning threshold: userId={}, threshold={}", userId, request.getThreshold());

        return ThresholdConfigResponse.builder()
                .threshold(request.getThreshold())
                .build();
    }

    @Override
    @Transactional
    public void createOvertimeWarning(Long userId, Long chargingPileId, Long chargingRecordId,
                                      String pileName, Integer duration) {
        Optional<WarningNotice> existingNotice = warningNoticeRepository.findByChargingRecordIdAndType(
                chargingRecordId, WarningNoticeType.OVERTIME_WARNING);

        if (existingNotice.isPresent()) {
            log.debug("Overtime warning already exists: recordId={}", chargingRecordId);
            return;
        }

        String content = String.format("您的充电桩 %s 已超时占位 %d 分钟，请尽快结束充电并驶离车位。",
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
        log.info("Create overtime warning: userId={}, recordId={}, duration={}min", userId, chargingRecordId, duration);
    }

    @Override
    public Page<WarningNoticeResponse> getAdminWarningNoticeList(WarningNoticeType type, Integer isRead,
                                                                 Long userId, LocalDate startDate, LocalDate endDate,
                                                                 Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Page<WarningNotice> noticePage = warningNoticeRepository.findByAdminFilters(
                type, isRead, userId, startDateTime, endDateTime, pageable);

        Map<Long, ChargingPile> pileMap = batchFetchPiles(noticePage.getContent());
        Map<Long, User> userMap = batchFetchUsers(noticePage.getContent());

        List<WarningNoticeResponse> responses = noticePage.getContent().stream()
                .map(notice -> convertToResponse(
                        notice,
                        pileMap.get(notice.getChargingPileId()),
                        userMap.get(notice.getUserId())))
                .toList();

        return new PageImpl<>(responses, pageable, noticePage.getTotalElements());
    }

    @Override
    public WarningNoticeResponse getAdminWarningNoticeById(Long id) {
        WarningNotice notice = warningNoticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.WARNING_NOTICE_NOT_FOUND));

        ChargingPile pile = notice.getChargingPileId() != null
                ? chargingPileRepository.findById(notice.getChargingPileId()).orElse(null)
                : null;
        User user = userRepository.findById(notice.getUserId()).orElse(null);

        return convertToResponse(notice, pile, user);
    }

    @Override
    @Transactional
    public void markAdminWarningsAsRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        warningNoticeRepository.markAllAsReadByIds(ids);
        log.info("Admin batch mark warning notices as read: ids={}", ids);
    }

    @Override
    @Transactional
    public void deleteAdminWarningNotice(Long id) {
        WarningNotice notice = warningNoticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.WARNING_NOTICE_NOT_FOUND));
        warningNoticeRepository.delete(notice);
        log.info("Admin delete warning notice: id={}", id);
    }

    @Override
    public ThresholdConfigResponse getAdminThresholdConfig() {
        return getSystemThresholdResponse();
    }

    @Override
    @Transactional
    public ThresholdConfigResponse setAdminThresholdConfig(ThresholdConfigRequest request) {
        Optional<SystemConfig> configOpt = systemConfigRepository.findByUserIdIsNullAndConfigKey(CONFIG_KEY_OVERTIME_THRESHOLD);

        SystemConfig config;
        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setConfigValue(request.getThreshold().toString());
        } else {
            config = new SystemConfig();
            config.setUserId(null);
            config.setConfigKey(CONFIG_KEY_OVERTIME_THRESHOLD);
            config.setConfigValue(request.getThreshold().toString());
            config.setDescription("系统级超时占位预警阈值（分钟）");
        }

        systemConfigRepository.save(config);
        log.info("Set global warning threshold: threshold={}", request.getThreshold());

        return ThresholdConfigResponse.builder()
                .threshold(request.getThreshold())
                .build();
    }

    private ThresholdConfigResponse getSystemThresholdResponse() {
        Optional<SystemConfig> systemConfig = systemConfigRepository.findByUserIdIsNullAndConfigKey(
                CONFIG_KEY_OVERTIME_THRESHOLD);

        if (systemConfig.isPresent()) {
            return ThresholdConfigResponse.builder()
                    .threshold(Integer.parseInt(systemConfig.get().getConfigValue()))
                    .build();
        }

        return ThresholdConfigResponse.builder()
                .threshold(DEFAULT_THRESHOLD)
                .build();
    }

    private Map<Long, ChargingPile> batchFetchPiles(List<WarningNotice> notices) {
        if (notices == null || notices.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> pileIds = notices.stream()
                .map(WarningNotice::getChargingPileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (pileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return chargingPileRepository.findAllById(pileIds).stream()
                .collect(Collectors.toMap(ChargingPile::getId, pile -> pile));
    }

    private Map<Long, User> batchFetchUsers(List<WarningNotice> notices) {
        if (notices == null || notices.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = notices.stream()
                .map(WarningNotice::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private WarningNoticeResponse convertToResponse(WarningNotice notice, ChargingPile pile, User user) {
        return WarningNoticeResponse.builder()
                .id(notice.getId())
                .userId(notice.getUserId())
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
                .chargingPileId(notice.getChargingPileId())
                .pileName(pile != null ? pile.getCode() : null)
                .pileLocation(pile != null ? pile.getLocation() : null)
                .chargingRecordId(notice.getChargingRecordId())
                .type(notice.getType())
                .typeDesc(notice.getType().getDescription())
                .content(notice.getContent())
                .overtimeMinutes(notice.getOvertimeMinutes())
                .isRead(notice.getIsRead())
                .sendStatus(notice.getSendStatus())
                .sendStatusDesc(notice.getSendStatus().getDescription())
                .createdTime(notice.getCreatedTime())
                .build();
    }
}