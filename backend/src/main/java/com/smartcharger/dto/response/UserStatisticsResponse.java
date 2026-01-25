package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户统计数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {

    /**
     * 车辆数量
     */
    private Integer vehicleCount;

    /**
     * 充电记录数量
     */
    private Integer chargingRecordCount;

    /**
     * 总充电量（度）
     */
    private BigDecimal totalElectricQuantity;

    /**
     * 总消费金额（元）
     */
    private BigDecimal totalSpent;

    /**
     * 平均充电时长（分钟）
     */
    private Integer avgChargingDuration;

    /**
     * 超时占位次数
     */
    private Integer overtimeCount;

    /**
     * 预约次数
     */
    private Integer reservationCount;

    /**
     * 取消预约次数
     */
    private Integer cancelledReservationCount;

    /**
     * 故障报修次数
     */
    private Integer faultReportCount;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后充电时间
     */
    private LocalDateTime lastChargingTime;
}
