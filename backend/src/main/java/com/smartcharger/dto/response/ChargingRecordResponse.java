package com.smartcharger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingRecordResponse {

    /**
     * 记录ID
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
     * 充电桩类型
     */
    private String pileType;

    /**
     * 车辆ID
     */
    private Long vehicleId;

    /**
     * 车牌号
     */
    private String vehicleLicensePlate;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 充电时长（分钟）
     */
    private Integer duration;

    /**
     * 充电量（度）
     */
    private BigDecimal electricQuantity;

    /**
     * 费用（元）
     */
    private BigDecimal fee;

    /**
     * 状态
     */
    private ChargingRecordStatus status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 每度电价格（仅详情返回）
     */
    private BigDecimal pricePerKwh;

    /**
     * 服务费单价（仅详情返回）
     */
    private BigDecimal serviceFee;

    /**
     * 费用明细（仅详情返回）
     */
    private FeeBreakdown feeBreakdown;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;

    /**
     * 费用明细内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeBreakdown {
        /**
         * 电费
         */
        private BigDecimal electricityFee;

        /**
         * 服务费总额
         */
        private BigDecimal serviceFee;
    }
}
