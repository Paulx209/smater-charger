package com.smartcharger.entity;

import com.smartcharger.entity.enums.FaultReportStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 故障报修实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fault_report")
public class FaultReport extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "charging_pile_id", nullable = false)
    private Long chargingPileId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FaultReportStatus status = FaultReportStatus.PENDING;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handle_remark", columnDefinition = "TEXT")
    private String handleRemark;
}
