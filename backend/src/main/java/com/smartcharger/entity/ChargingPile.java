package com.smartcharger.entity;

import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 充电桩实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "charging_pile")
public class ChargingPile extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "lng", precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "lat", precision = 10, scale = 7)
    private BigDecimal lat;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChargingPileType type;

    @Column(name = "power", nullable = false, precision = 10, scale = 2)
    private BigDecimal power;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChargingPileStatus status = ChargingPileStatus.IDLE;
}
