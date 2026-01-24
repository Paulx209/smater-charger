package com.smartcharger.repository;

import com.smartcharger.entity.Reservation;
import com.smartcharger.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 预约数据访问接口
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 查询用户的预约列表（按创建时间倒序）
     */
    Page<Reservation> findByUserIdOrderByCreatedTimeDesc(Long userId, Pageable pageable);

    /**
     * 查询用户的预约列表（按状态筛选）
     */
    Page<Reservation> findByUserIdAndStatusOrderByCreatedTimeDesc(
            Long userId, ReservationStatus status, Pageable pageable);

    /**
     * 查询用户当前进行中的预约
     */
    Optional<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    /**
     * 查询用户的预约（带ID，用于权限校验）
     */
    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询充电桩在指定时间段内的预约（检查时间冲突）
     * 查找结束时间在指定时间之后的预约
     */
    List<Reservation> findByChargingPileIdAndStatusAndEndTimeAfter(
            Long chargingPileId, ReservationStatus status, LocalDateTime time);

    /**
     * 查询过期的预约（定时任务使用）
     * 查找结束时间在指定时间之前的预约
     */
    List<Reservation> findByStatusAndEndTimeBefore(
            ReservationStatus status, LocalDateTime time);
}
