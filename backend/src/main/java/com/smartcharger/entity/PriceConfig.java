package com.smartcharger.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "price_config")
public class PriceConfig extends BaseEntity {

    @Column(name = "charging_pile_type", nullable = false, length = 20)
    private String chargingPileType;

    @Column(name = "price_per_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKwh;

    @Column(name = "service_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;
}
