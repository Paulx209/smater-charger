package com.smartcharger.repository;

import com.smartcharger.entity.Reservation;
import com.smartcharger.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserIdOrderByCreatedTimeDesc(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatusOrderByCreatedTimeDesc(
            Long userId, ReservationStatus status, Pageable pageable);

    Optional<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    List<Reservation> findByChargingPileIdAndStatusAndEndTimeAfter(
            Long chargingPileId, ReservationStatus status, LocalDateTime time);

    List<Reservation> findByStatusAndEndTimeBefore(
            ReservationStatus status, LocalDateTime time);

    @Query("SELECT r FROM Reservation r WHERE " +
           "(:userId IS NULL OR r.userId = :userId) " +
           "AND (:chargingPileId IS NULL OR r.chargingPileId = :chargingPileId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:startDate IS NULL OR r.startTime >= :startDate) " +
           "AND (:endDate IS NULL OR r.startTime < :endDate) " +
           "ORDER BY r.startTime DESC")
    Page<Reservation> findByAdminFilters(@Param("userId") Long userId,
                                         @Param("chargingPileId") Long chargingPileId,
                                         @Param("status") ReservationStatus status,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    Long countByChargingPileId(Long chargingPileId);

    Long countByUserId(Long userId);

    Long countByUserIdAndStatus(Long userId, ReservationStatus status);
}