package com.smartcharger.entity;

import com.smartcharger.entity.enums.SendStatus;
import com.smartcharger.entity.enums.WarningNoticeType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警通知实体类
 */
@Data
@Entity
@Table(name = "warning_notice")
public class WarningNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "charging_pile_id")
    private Long chargingPileId;

    @Column(name = "charging_record_id")
    private Long chargingRecordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private WarningNoticeType type;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes;

    @Column(name = "is_read", nullable = false)
    private Integer isRead = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "send_status", nullable = false, length = 20)
    private SendStatus sendStatus = SendStatus.PENDING;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
}
