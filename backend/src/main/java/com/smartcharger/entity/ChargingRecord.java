package com.smartcharger.entity;

import com.smartcharger.entity.enums.ChargingRecordStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "charging_record")
public class ChargingRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "charging_pile_id", nullable = false)
    private Long chargingPileId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "electric_quantity", precision = 10, scale = 2)
    private BigDecimal electricQuantity;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChargingRecordStatus status = ChargingRecordStatus.CHARGING;
}
