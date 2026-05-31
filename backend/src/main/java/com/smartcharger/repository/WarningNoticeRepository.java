package com.smartcharger.repository;

import com.smartcharger.entity.WarningNotice;
import com.smartcharger.entity.enums.SendStatus;
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

    Page<WarningNotice> findByUserIdAndSendStatus(Long userId, SendStatus sendStatus, Pageable pageable);

    Page<WarningNotice> findByUserIdAndTypeAndSendStatus(Long userId, WarningNoticeType type,
                                                         SendStatus sendStatus, Pageable pageable);

    Page<WarningNotice> findByUserIdAndIsReadAndSendStatus(Long userId, Integer isRead,
                                                           SendStatus sendStatus, Pageable pageable);

    Page<WarningNotice> findByUserIdAndTypeAndIsReadAndSendStatus(Long userId, WarningNoticeType type,
                                                                  Integer isRead, SendStatus sendStatus,
                                                                  Pageable pageable);

    Integer countByUserIdAndIsReadAndSendStatus(Long userId, Integer isRead, SendStatus sendStatus);

    @Modifying
    @Query("UPDATE WarningNotice wn SET wn.isRead = 1 " +
            "WHERE wn.userId = :userId AND wn.isRead = 0 AND wn.sendStatus = :sendStatus")
    void markAllAsRead(@Param("userId") Long userId, @Param("sendStatus") SendStatus sendStatus);

    @Modifying
    @Query("UPDATE WarningNotice wn SET wn.isRead = 1 WHERE wn.id IN :ids AND wn.sendStatus = :sendStatus")
    void markAllAsReadByIds(@Param("ids") List<Long> ids, @Param("sendStatus") SendStatus sendStatus);

    Optional<WarningNotice> findByChargingRecordIdAndType(Long chargingRecordId, WarningNoticeType type);

    Optional<WarningNotice> findByIdAndUserId(Long id, Long userId);

    Optional<WarningNotice> findByIdAndUserIdAndSendStatus(Long id, Long userId, SendStatus sendStatus);

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
