package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ReservationCreateRequest;
import com.smartcharger.dto.response.AvailabilityCheckResponse;
import com.smartcharger.dto.response.ReservationResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.Reservation;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ReservationRepository;
import com.smartcharger.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final RedissonClient redissonClient;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ChargingPileRepository chargingPileRepository,
                                  RedissonClient redissonClient) {
        this.reservationRepository = reservationRepository;
        this.chargingPileRepository = chargingPileRepository;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationCreateRequest request) {
        log.info("创建预约: userId={}, chargingPileId=", userId, request.getChargingPileId());

        // 1. 检查用户是否已有进行中的预约
        Optional<Reservation> existingReservation =
                reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.PENDING);
        if (existingReservation.isPresent()) {
            throw new BusinessException(ResultCode.USER_HAS_PENDING_RESERVATION);
        }

        // 2. 检查充电桩是否存在
        ChargingPile chargingPile = chargingPileRepository.findById(request.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        // 3. 使用分布式锁防止并发预约
        String lockKey = "reservation:pile:" + request.getChargingPileId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待5秒，锁定10秒
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // 4. 检查充电桩状态是否为空闲
                if (chargingPile.getStatus() != ChargingPileStatus.IDLE) {
                    throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
                }

                // 5. 计算预约时间
                LocalDateTime startTime = request.getStartTime() != null ?
                        request.getStartTime() : LocalDateTime.now();
                LocalDateTime endTime = startTime.plusHours(2);

                // 6. 检查时间段是否冲突
                List<Reservation> conflictReservations =
                        reservationRepository.findByChargingPileIdAndStatusAndEndTimeAfter(
                                request.getChargingPileId(), ReservationStatus.PENDING, startTime);

                if (!conflictReservations.isEmpty()) {
                    throw new BusinessException(ResultCode.TIME_CONFLICT);
                }

                // 7. 创建预约
                Reservation reservation = new Reservation();
                reservation.setUserId(userId);
                reservation.setChargingPileId(request.getChargingPileId());
                reservation.setStartTime(startTime);
                reservation.setEndTime(endTime);
                reservation.setStatus(ReservationStatus.PENDING);
                Reservation savedReservation = reservationRepository.save(reservation);

                // 8. 更新充电桩状态为已预约
                chargingPile.setStatus(ChargingPileStatus.RESERVED);
                chargingPileRepository.save(chargingPile);

                log.info("创建预约成功: reservationId={}, chargingPileId={}",
                        savedReservation.getId(), request.getChargingPileId());

                // 9. 返回预约信息
                return buildReservationResponse(savedReservation, chargingPile);
            } else {
                throw new BusinessException(ResultCode.SYSTEM_BUSY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, Long id) {
        log.info("取消预约: userId={}, reservationId={}", userId, id);

        // 1. 查询预约（验证权限）
        Reservation reservation = reservationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        // 2. 检查预约状态
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ResultCode.RESERVATION_CANNOT_CANCEL);
        }

        // 3. 更新预约状态
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // 4. 更新充电桩状态为空闲
        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));
        chargingPile.setStatus(ChargingPileStatus.IDLE);
        chargingPileRepository.save(chargingPile);

        log.info("取消预约成功: reservationId={}", id);
    }

    @Override
    public Page<ReservationResponse> getMyReservations(Long userId, ReservationStatus status,
                                                        Integer page, Integer size) {
        log.info("查询我的预约列表: userId={}, status={}, page={}, size={}", userId, status, page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Reservation> reservationPage;

        if (status != null) {
            reservationPage = reservationRepository.findByUserIdAndStatusOrderByCreatedTimeDesc(
                    userId, status, pageable);
        } else {
            reservationPage = reservationRepository.findByUserIdOrderByCreatedTimeDesc(
                    userId, pageable);
        }

        return reservationPage.map(reservation -> {
            ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId())
                    .orElse(null);
            return buildReservationResponse(reservation, chargingPile);
        });
    }

    @Override
    public ReservationResponse getReservationById(Long userId, Long id) {
        log.info("获取预约详情: userId={}, reservationId={}", userId, id);

        Reservation reservation = reservationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId())
                .orElse(null);

        return buildReservationResponse(reservation, chargingPile);
    }

    @Override
    public ReservationResponse getCurrentReservation(Long userId) {
        log.info("获取当前进行中的预约: userId={}", userId);

        Optional<Reservation> reservationOpt =
                reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.PENDING);

        if (reservationOpt.isEmpty()) {
            return null;
        }

        Reservation reservation = reservationOpt.get();
        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId())
                .orElse(null);

        return buildReservationResponse(reservation, chargingPile);
    }

    @Override
    @Transactional
    public void handleExpiredReservations() {
        // 查询所有过期的预约
        List<Reservation> expiredReservations =
                reservationRepository.findByStatusAndEndTimeBefore(
                        ReservationStatus.PENDING, LocalDateTime.now());

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("开始处理过期预约，数量: {}", expiredReservations.size());

        // 批量处理
        for (Reservation reservation : expiredReservations) {
            try {
                // 更新预约状态
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                // 更新充电桩状态
                ChargingPile chargingPile =
                        chargingPileRepository.findById(reservation.getChargingPileId())
                                .orElse(null);
                if (chargingPile != null &&
                        chargingPile.getStatus() == ChargingPileStatus.RESERVED) {
                    chargingPile.setStatus(ChargingPileStatus.IDLE);
                    chargingPileRepository.save(chargingPile);
                }

                log.info("处理过期预约成功: reservationId={}, chargingPileId={}",
                        reservation.getId(), reservation.getChargingPileId());
            } catch (Exception e) {
                log.error("处理过期预约失败: reservationId={}, error={}",
                        reservation.getId(), e.getMessage());
            }
        }

        log.info("处理过期预约完成，成功处理: {} 条", expiredReservations.size());
    }

    /**
     * 构建预约响应对象
     */
    private ReservationResponse buildReservationResponse(Reservation reservation, ChargingPile chargingPile) {
        ReservationResponse.ReservationResponseBuilder builder = ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .chargingPileId(reservation.getChargingPileId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .statusDesc(getStatusDesc(reservation.getStatus()))
                .createdTime(reservation.getCreatedTime());

        // 添加充电桩信息
        if (chargingPile != null) {
            builder.chargingPileCode(chargingPile.getCode())
                    .chargingPileLocation(chargingPile.getLocation())
                    .chargingPileLng(chargingPile.getLng())
                    .chargingPileLat(chargingPile.getLat())
                    .chargingPileType(chargingPile.getType().name())
                    .chargingPileTypeDesc(getTypeDesc(chargingPile.getType().name()))
                    .chargingPilePower(chargingPile.getPower());
        }

        // 计算剩余时间（仅PENDING状态）
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            long remainingMinutes = Duration.between(LocalDateTime.now(), reservation.getEndTime()).toMinutes();
            builder.remainingMinutes(Math.max(0, remainingMinutes));
        }

        return builder.build();
    }

    /**
     * 获取预约状态描述
     */
    private String getStatusDesc(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "待使用";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case EXPIRED -> "已过期";
        };
    }

    /**
     * 获取充电桩类型描述
     */
    private String getTypeDesc(String type) {
        return switch (type) {
            case "AC" -> "交流慢充";
            case "DC" -> "直流快充";
            default -> type;
        };
    }

    @Override
    public AvailabilityCheckResponse checkAvailability(Long pileId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("检查充电桩可用性: pileId={}, startTime={}, endTime=", pileId, startTime, endTime);

        // 1. 检查充电桩是否存在
        ChargingPile chargingPile = chargingPileRepository.findById(pileId)
                .orElse(null);

        if (chargingPile == null) {
            return AvailabilityCheckResponse.builder()
                    .available(false)
                    .reason("充电桩不存在")
                    .build();
        }

        // 2. 检查充电桩状态
        if (chargingPile.getStatus() != ChargingPileStatus.IDLE) {
            return AvailabilityCheckResponse.builder()
                    .available(false)
                    .reason("充电桩当前状态为：" + getChargingPileStatusDesc(chargingPile.getStatus()))
                    .build();
        }

        // 3. 检查时间段是否有冲突的预约
        List<Reservation> conflictReservations = reservationRepository
                .findByChargingPileIdAndStatusAndEndTimeAfter(pileId, ReservationStatus.PENDING, startTime);

        // 过滤出真正冲突的预约（时间段有重叠）
        List<Reservation> actualConflicts = conflictReservations.stream()
                .filter(r -> isTimeOverlap(startTime, endTime, r.getStartTime(), r.getEndTime()))
                .toList();

        if (!actualConflicts.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            List<AvailabilityCheckResponse.ConflictReservation> conflicts = actualConflicts.stream()
                    .map(r -> AvailabilityCheckResponse.ConflictReservation.builder()
                            .reservationId(r.getId())
                            .startTime(r.getStartTime().format(formatter))
                            .endTime(r.getEndTime().format(formatter))
                            .build())
                    .collect(Collectors.toList());

            return AvailabilityCheckResponse.builder()
                    .available(false)
                    .reason("该时间段已有预约")
                    .conflictReservations(conflicts)
                    .build();
        }

        // 4. 可用
        return AvailabilityCheckResponse.builder()
                .available(true)
                .reason("充电桩可用")
                .build();
    }

    /**
     * 判断两个时间段是否重叠
     */
    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1,
                                   LocalDateTime start2, LocalDateTime end2) {
        // 时间段1: [start1, end1]
        // 时间段2: [start2, end2]
        // 重叠条件: start1 < end2 && start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * 获取充电桩状态描述
     */
    private String getChargingPileStatusDesc(ChargingPileStatus status) {
        return switch (status) {
            case IDLE -> "空闲";
            case CHARGING -> "充电中";
            case FAULT -> "故障";
            case RESERVED -> "已预约";
            case OVERTIME -> "超时占位";
        };
    }
}
