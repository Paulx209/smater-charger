package com.smartcharger.entity;

import com.smartcharger.entity.enums.ChargingEndReason;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.entity.enums.ChargingTargetType;
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

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20)
    private ChargingTargetType targetType;

    @Column(name = "target_value", precision = 10, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "target_duration_minutes")
    private Integer targetDurationMinutes;

    @Column(name = "target_kwh", precision = 10, scale = 3)
    private BigDecimal targetKwh;

    @Column(name = "target_end_time")
    private LocalDateTime targetEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason", length = 30)
    private ChargingEndReason endReason;

    @Column(name = "pre_end_notice_sent", nullable = false)
    private Integer preEndNoticeSent = 0;

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
