package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ReservationCreateRequest;
import com.smartcharger.dto.response.AvailabilityCheckResponse;
import com.smartcharger.dto.response.ReservationResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.Reservation;
import com.smartcharger.entity.User;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ReservationRepository;
import com.smartcharger.repository.UserRepository;
import com.smartcharger.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ChargingPileRepository chargingPileRepository,
                                  UserRepository userRepository,
                                  RedissonClient redissonClient) {
        this.reservationRepository = reservationRepository;
        this.chargingPileRepository = chargingPileRepository;
        this.userRepository = userRepository;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationCreateRequest request) {
        log.info("Create reservation: userId={}, chargingPileId={}", userId, request.getChargingPileId());

        Optional<Reservation> existingReservation =
                reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.PENDING);
        if (existingReservation.isPresent()) {
            throw new BusinessException(ResultCode.USER_HAS_PENDING_RESERVATION);
        }

        ChargingPile chargingPile = chargingPileRepository.findById(request.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        String lockKey = "reservation:pile:" + request.getChargingPileId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                chargingPile = chargingPileRepository.findById(request.getChargingPileId())
                        .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

                if (chargingPile.getStatus() != ChargingPileStatus.IDLE) {
                    throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
                }

                LocalDateTime startTime = request.getStartTime() != null
                        ? request.getStartTime()
                        : LocalDateTime.now();
                LocalDateTime endTime = startTime.plusHours(2);

                List<Reservation> conflictReservations = reservationRepository
                        .findByChargingPileIdAndStatusAndEndTimeAfter(
                                request.getChargingPileId(), ReservationStatus.PENDING, startTime);

                if (!conflictReservations.isEmpty()) {
                    throw new BusinessException(ResultCode.TIME_CONFLICT);
                }

                Reservation reservation = new Reservation();
                reservation.setUserId(userId);
                reservation.setChargingPileId(request.getChargingPileId());
                reservation.setStartTime(startTime);
                reservation.setEndTime(endTime);
                reservation.setStatus(ReservationStatus.PENDING);
                Reservation savedReservation = reservationRepository.save(reservation);

                chargingPile.setStatus(ChargingPileStatus.RESERVED);
                chargingPileRepository.save(chargingPile);

                log.info("Reservation created: reservationId={}, chargingPileId={}",
                        savedReservation.getId(), request.getChargingPileId());

                User user = userRepository.findById(userId).orElse(null);
                return buildReservationResponse(savedReservation, chargingPile, user);
            }

            throw new BusinessException(ResultCode.SYSTEM_BUSY);
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
        log.info("Cancel reservation by owner: userId={}, reservationId={}", userId, id);

        Reservation reservation = reservationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        cancelPendingReservation(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getMyReservations(Long userId, ReservationStatus status,
                                                       Integer page, Integer size) {
        log.info("Get my reservations: userId={}, status={}, page={}, size={}", userId, status, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<Reservation> reservationPage = status != null
                ? reservationRepository.findByUserIdAndStatusOrderByCreatedTimeDesc(userId, status, pageable)
                : reservationRepository.findByUserIdOrderByCreatedTimeDesc(userId, pageable);

        Map<Long, ChargingPile> pileMap = batchFetchPiles(reservationPage.getContent());
        User user = userRepository.findById(userId).orElse(null);

        List<ReservationResponse> responses = reservationPage.getContent().stream()
                .map(reservation -> buildReservationResponse(
                        reservation,
                        pileMap.get(reservation.getChargingPileId()),
                        user))
                .toList();

        return new PageImpl<>(responses, pageable, reservationPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long userId, Long id) {
        log.info("Get reservation by owner: userId={}, reservationId={}", userId, id);

        Reservation reservation = reservationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId()).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        return buildReservationResponse(reservation, chargingPile, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getCurrentReservation(Long userId) {
        log.info("Get current reservation: userId={}", userId);

        Optional<Reservation> reservationOpt =
                reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.PENDING);
        if (reservationOpt.isEmpty()) {
            return null;
        }

        Reservation reservation = reservationOpt.get();
        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId()).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        return buildReservationResponse(reservation, chargingPile, user);
    }

    @Override
    @Transactional
    public void handleExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndEndTimeBefore(
                ReservationStatus.PENDING, LocalDateTime.now());

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("Handle expired reservations: count={}", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            try {
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId()).orElse(null);
                if (chargingPile != null && chargingPile.getStatus() == ChargingPileStatus.RESERVED) {
                    chargingPile.setStatus(ChargingPileStatus.IDLE);
                    chargingPileRepository.save(chargingPile);
                }

                log.info("Expired reservation handled: reservationId={}, chargingPileId={}",
                        reservation.getId(), reservation.getChargingPileId());
            } catch (Exception e) {
                log.error("Failed to handle expired reservation: reservationId={}", reservation.getId(), e);
            }
        }
    }

    @Override
    public AvailabilityCheckResponse checkAvailability(Long pileId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Check reservation availability: pileId={}, startTime={}, endTime={}", pileId, startTime, endTime);

        ChargingPile chargingPile = chargingPileRepository.findById(pileId).orElse(null);
        if (chargingPile == null) {
            return AvailabilityCheckResponse.builder()
                    .available(false)
                    .reason("充电桩不存在")
                    .build();
        }

        if (chargingPile.getStatus() != ChargingPileStatus.IDLE) {
            return AvailabilityCheckResponse.builder()
                    .available(false)
                    .reason("充电桩当前状态不可预约：" + getChargingPileStatusDesc(chargingPile.getStatus()))
                    .build();
        }

        List<Reservation> conflictReservations = reservationRepository
                .findByChargingPileIdAndStatusAndEndTimeAfter(pileId, ReservationStatus.PENDING, startTime);

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

        return AvailabilityCheckResponse.builder()
                .available(true)
                .reason("充电桩可预约")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getAdminReservations(Long userId, Long chargingPileId,
                                                          ReservationStatus status,
                                                          LocalDate startDate, LocalDate endDate,
                                                          Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startTime"));
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Page<Reservation> reservationPage = reservationRepository.findByAdminFilters(
                userId, chargingPileId, status, startDateTime, endDateTime, pageable);

        Map<Long, ChargingPile> pileMap = batchFetchPiles(reservationPage.getContent());
        Map<Long, User> userMap = batchFetchUsers(reservationPage.getContent());

        List<ReservationResponse> responses = reservationPage.getContent().stream()
                .map(reservation -> buildReservationResponse(
                        reservation,
                        pileMap.get(reservation.getChargingPileId()),
                        userMap.get(reservation.getUserId())))
                .toList();

        return new PageImpl<>(responses, pageable, reservationPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getAdminReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId()).orElse(null);
        User user = userRepository.findById(reservation.getUserId()).orElse(null);

        return buildReservationResponse(reservation, chargingPile, user);
    }

    @Override
    @Transactional
    public void cancelReservationByAdmin(Long id) {
        log.info("Cancel reservation by admin: reservationId={}", id);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        cancelPendingReservation(reservation);
    }

    private void cancelPendingReservation(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ResultCode.RESERVATION_CANNOT_CANCEL);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));
        if (chargingPile.getStatus() == ChargingPileStatus.RESERVED) {
            chargingPile.setStatus(ChargingPileStatus.IDLE);
            chargingPileRepository.save(chargingPile);
        }
    }

    private Map<Long, ChargingPile> batchFetchPiles(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> pileIds = reservations.stream()
                .map(Reservation::getChargingPileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (pileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return chargingPileRepository.findAllById(pileIds).stream()
                .collect(Collectors.toMap(ChargingPile::getId, pile -> pile));
    }

    private Map<Long, User> batchFetchUsers(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = reservations.stream()
                .map(Reservation::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private ReservationResponse buildReservationResponse(Reservation reservation, ChargingPile chargingPile, User user) {
        ReservationResponse.ReservationResponseBuilder builder = ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
                .chargingPileId(reservation.getChargingPileId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .statusDesc(getStatusDesc(reservation.getStatus()))
                .createdTime(reservation.getCreatedTime())
                .updatedTime(reservation.getUpdatedTime());

        if (chargingPile != null) {
            builder.chargingPileCode(chargingPile.getCode())
                    .chargingPileLocation(chargingPile.getLocation())
                    .chargingPileLng(chargingPile.getLng())
                    .chargingPileLat(chargingPile.getLat())
                    .chargingPileType(chargingPile.getType().name())
                    .chargingPileTypeDesc(getTypeDesc(chargingPile.getType().name()))
                    .chargingPilePower(chargingPile.getPower());
        }

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            long remainingMinutes = Duration.between(LocalDateTime.now(), reservation.getEndTime()).toMinutes();
            builder.remainingMinutes(Math.max(0, remainingMinutes));
        }

        return builder.build();
    }

    private String getStatusDesc(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "待使用";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case EXPIRED -> "已过期";
        };
    }

    private String getTypeDesc(String type) {
        return switch (type) {
            case "AC" -> "交流桩";
            case "DC" -> "直流桩";
            default -> type;
        };
    }

    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1,
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

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