package com.smartcharger.repository;

import com.smartcharger.entity.WarningNotice;
import com.smartcharger.entity.enums.WarningNoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 预警通知数据访问接口
 */
@Repository
public interface WarningNoticeRepository extends JpaRepository<WarningNotice, Long> {

    /**
     * 查询用户的预警通知（分页）
     */
    Page<WarningNotice> findByUserId(Long userId, Pageable pageable);

    /**
     * 查询用户指定类型的预警通知（分页）
     */
    Page<WarningNotice> findByUserIdAndType(Long userId, WarningNoticeType type, Pageable pageable);

    /**
     * 查询用户指定已读状态的预警通知（分页）
     */
    Page<WarningNotice> findByUserIdAndIsRead(Long userId, Integer isRead, Pageable pageable);

    /**
     * 查询用户指定类型和已读状态的预警通知（分页）
     */
    Page<WarningNotice> findByUserIdAndTypeAndIsRead(Long userId, WarningNoticeType type,
                                                       Integer isRead, Pageable pageable);

    /**
     * 查询用户未读通知数量
     */
    Integer countByUserIdAndIsRead(Long userId, Integer isRead);

    /**
     * 标记所有未读通知为已读
     */
    @Modifying
    @Query("UPDATE WarningNotice wn SET wn.isRead = 1 WHERE wn.userId = :userId AND wn.isRead = 0")
    void markAllAsRead(@Param("userId") Long userId);

    /**
     * 根据充电记录ID查询超时预警通知
     */
    Optional<WarningNotice> findByChargingRecordIdAndType(Long chargingRecordId, WarningNoticeType type);

    /**
     * 查询用户的通知（根据ID和用户ID）
     */
    Optional<WarningNotice> findByIdAndUserId(Long id, Long userId);

    /**
     * 统计用户的超时占位次数
     */
    Long countByUserIdAndType(Long userId, WarningNoticeType type);

    /**
     * 管理端：查询用户的违规记录（分页）
     */
    @Query("SELECT wn FROM WarningNotice wn WHERE wn.userId = :userId AND wn.type = 'OVERTIME_WARNING' AND " +
            "(:startDate IS NULL OR wn.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR wn.createdTime < :endDate) " +
            "ORDER BY wn.createdTime DESC")
    Page<WarningNotice> findViolationsByUserId(@Param("userId") Long userId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 Pageable pageable);

    /**
     * 统计用户的总超时时长（分钟）
     */
    @Query("SELECT SUM(wn.overtimeMinutes) FROM WarningNotice wn WHERE wn.userId = :userId AND wn.type = 'OVERTIME_WARNING'")
    Integer sumOvertimeMinutesByUserId(@Param("userId") Long userId);
}
