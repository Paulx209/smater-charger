package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 违规记录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationRecordResponse {

    /**
     * 预警通知ID
     */
    private Long id;

    /**
     * 充电记录ID
     */
    private Long chargingRecordId;

    /**
     * 充电桩编号
     */
    private String chargingPileCode;

    /**
     * 充电桩位置
     */
    private String chargingPileLocation;

    /**
     * 充电结束时间
     */
    private LocalDateTime chargingEndTime;

    /**
     * 超时分钟数
     */
    private Integer overtimeMinutes;

    /**
     * 预警时间
     */
    private LocalDateTime warningTime;

    /**
     * 违规类型
     */
    private String violationType;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
