package com.smartcharger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcharger.entity.enums.SendStatus;
import com.smartcharger.entity.enums.WarningNoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预警通知响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningNoticeResponse {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 充电桩ID
     */
    private Long chargingPileId;

    /**
     * 充电桩名称
     */
    private String pileName;

    /**
     * 充电桩位置
     */
    private String pileLocation;

    /**
     * 充电记录ID
     */
    private Long chargingRecordId;

    /**
     * 通知类型
     */
    private WarningNoticeType type;

    /**
     * 通知类型描述
     */
    private String typeDesc;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读（0=未读，1=已读）
     */
    private Integer isRead;

    /**
     * 发送状态
     */
    private SendStatus sendStatus;

    /**
     * 发送状态描述
     */
    private String sendStatusDesc;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;
}
