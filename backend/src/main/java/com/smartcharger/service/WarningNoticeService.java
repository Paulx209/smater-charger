package com.smartcharger.service;

import com.smartcharger.dto.request.ThresholdConfigRequest;
import com.smartcharger.dto.response.ThresholdConfigResponse;
import com.smartcharger.dto.response.UnreadCountResponse;
import com.smartcharger.dto.response.WarningNoticeResponse;
import com.smartcharger.entity.enums.WarningNoticeType;
import org.springframework.data.domain.Page;

/**
 * 预警通知服务接口
 */
public interface WarningNoticeService {

    /**
     * 查询预警通知列表
     */
    Page<WarningNoticeResponse> getWarningNoticeList(Long userId, WarningNoticeType type,
                                                       Integer isRead, Integer page, Integer size);

    /**
     * 查询未读通知数量
     */
    UnreadCountResponse getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long userId, Long noticeId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void deleteWarningNotice(Long userId, Long noticeId);

    /**
     * 获取预警阈值配置
     */
    ThresholdConfigResponse getThresholdConfig(Long userId);

    /**
     * 设置预警阈值配置
     */
    ThresholdConfigResponse setThresholdConfig(Long userId, ThresholdConfigRequest request);

    /**
     * 创建超时预警通知（内部方法）
     */
    void createOvertimeWarning(Long userId, Long chargingPileId, Long chargingRecordId,
                                String pileName, Integer duration);
}
