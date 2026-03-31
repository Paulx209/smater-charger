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
import java.util.List;
import java.util.Optional;

@Repository
public interface WarningNoticeRepository extends JpaRepository<WarningNotice, Long> {

    Page<WarningNotice> findByUserId(Long userId, Pageable pageable);

    Page<WarningNotice> findByUserIdAndType(Long userId, WarningNoticeType type, Pageable pageable);

    Page<WarningNotice> findByUserIdAndIsRead(Long userId, Integer isRead, Pageable pageable);

    Page<WarningNotice> findByUserIdAndTypeAndIsRead(Long userId, WarningNoticeType type,
                                                     Integer isRead, Pageable pageable);

    Integer countByUserIdAndIsRead(Long userId, Integer isRead);

    @Modifying
    @Query("UPDATE WarningNotice wn SET wn.isRead = 1 WHERE wn.userId = :userId AND wn.isRead = 0")
    void markAllAsRead(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE WarningNotice wn SET wn.isRead = 1 WHERE wn.id IN :ids")
    void markAllAsReadByIds(@Param("ids") List<Long> ids);

    Optional<WarningNotice> findByChargingRecordIdAndType(Long chargingRecordId, WarningNoticeType type);

    Optional<WarningNotice> findByIdAndUserId(Long id, Long userId);

    Long countByUserIdAndType(Long userId, WarningNoticeType type);

    @Query("SELECT wn FROM WarningNotice wn WHERE wn.userId = :userId AND wn.type = 'OVERTIME_WARNING' AND " +
            "(:startDate IS NULL OR wn.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR wn.createdTime < :endDate) " +
            "ORDER BY wn.createdTime DESC")
    Page<WarningNotice> findViolationsByUserId(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    @Query("SELECT SUM(wn.overtimeMinutes) FROM WarningNotice wn WHERE wn.userId = :userId AND wn.type = 'OVERTIME_WARNING'")
    Integer sumOvertimeMinutesByUserId(@Param("userId") Long userId);

    @Query("SELECT wn FROM WarningNotice wn WHERE " +
            "(:type IS NULL OR wn.type = :type) " +
            "AND (:isRead IS NULL OR wn.isRead = :isRead) " +
            "AND (:userId IS NULL OR wn.userId = :userId) " +
            "AND (:startDate IS NULL OR wn.createdTime >= :startDate) " +
            "AND (:endDate IS NULL OR wn.createdTime < :endDate) " +
            "ORDER BY wn.createdTime DESC")
    Page<WarningNotice> findByAdminFilters(@Param("type") WarningNoticeType type,
                                           @Param("isRead") Integer isRead,
                                           @Param("userId") Long userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);
}