package com.smartcharger.entity;

import com.smartcharger.entity.enums.AnnouncementStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统公告实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "announcement")
public class Announcement extends BaseEntity {

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;
}
