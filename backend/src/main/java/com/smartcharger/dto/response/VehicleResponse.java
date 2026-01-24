package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    /**
     * 车辆ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 车型
     */
    private String model;

    /**
     * 电池容量（kWh）
     */
    private BigDecimal batteryCapacity;

    /**
     * 是否为默认车辆（0=否，1=是）
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
