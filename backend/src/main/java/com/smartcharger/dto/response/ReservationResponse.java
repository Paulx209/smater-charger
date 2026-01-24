package com.smartcharger.dto.response;

import com.smartcharger.entity.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    /**
     * 预约ID
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
     * 充电桩编号
     */
    private String chargingPileCode;

    /**
     * 充电桩位置
     */
    private String chargingPileLocation;

    /**
     * 充电桩经度
     */
    private BigDecimal chargingPileLng;

    /**
     * 充电桩纬度
     */
    private BigDecimal chargingPileLat;

    /**
     * 充电桩类型
     */
    private String chargingPileType;

    /**
     * 充电桩类型描述
     */
    private String chargingPileTypeDesc;

    /**
     * 充电桩功率
     */
    private BigDecimal chargingPilePower;

    /**
     * 预约开始时间
     */
    private LocalDateTime startTime;

    /**
     * 预约结束时间
     */
    private LocalDateTime endTime;

    /**
     * 预约状态
     */
    private ReservationStatus status;

    /**
     * 预约状态描述
     */
    private String statusDesc;

    /**
     * 剩余时间（分钟），仅PENDING状态返回
     */
    private Long remainingMinutes;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
