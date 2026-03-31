package com.smartcharger.service;

import com.smartcharger.dto.request.ThresholdConfigRequest;
import com.smartcharger.dto.response.ThresholdConfigResponse;
import com.smartcharger.dto.response.UnreadCountResponse;
import com.smartcharger.dto.response.WarningNoticeResponse;
import com.smartcharger.entity.enums.WarningNoticeType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface WarningNoticeService {

    Page<WarningNoticeResponse> getWarningNoticeList(Long userId, WarningNoticeType type,
                                                     Integer isRead, Integer page, Integer size);

    UnreadCountResponse getUnreadCount(Long userId);

    void markAsRead(Long userId, Long noticeId);

    void markAllAsRead(Long userId);

    void deleteWarningNotice(Long userId, Long noticeId);

    ThresholdConfigResponse getThresholdConfig(Long userId);

    ThresholdConfigResponse setThresholdConfig(Long userId, ThresholdConfigRequest request);

    void createOvertimeWarning(Long userId, Long chargingPileId, Long chargingRecordId,
                               String pileName, Integer duration);

    Page<WarningNoticeResponse> getAdminWarningNoticeList(WarningNoticeType type, Integer isRead,
                                                          Long userId, LocalDate startDate, LocalDate endDate,
                                                          Integer page, Integer size);

    WarningNoticeResponse getAdminWarningNoticeById(Long id);

    void markAdminWarningsAsRead(List<Long> ids);

    void deleteAdminWarningNotice(Long id);

    ThresholdConfigResponse getAdminThresholdConfig();

    ThresholdConfigResponse setAdminThresholdConfig(ThresholdConfigRequest request);
}