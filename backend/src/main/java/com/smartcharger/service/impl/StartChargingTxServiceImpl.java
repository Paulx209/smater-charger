package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.ChargingRecord;
import com.smartcharger.entity.Reservation;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.entity.enums.ChargingTargetType;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.repository.ReservationRepository;
import com.smartcharger.repository.VehicleRepository;
import com.smartcharger.service.StartChargingTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartChargingTxServiceImpl implements StartChargingTxService {

    private final ChargingRecordRepository chargingRecordRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public ChargingRecordResponse startChargingInTx(Long userId, ChargingRecordStartRequest request) {
        Optional<ChargingRecord> existingRecord = chargingRecordRepository.findByUserIdAndStatus(
                userId, ChargingRecordStatus.CHARGING);
        if (existingRecord.isPresent()) {
            throw new BusinessException(ResultCode.USER_ALREADY_CHARGING);
        }

        ChargingPile chargingPile = chargingPileRepository.findById(request.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        if (chargingPile.getStatus() != ChargingPileStatus.IDLE) {
            throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> validReservations = reservationRepository
                .findByChargingPileIdAndStatusAndEndTimeAfter(
                        request.getChargingPileId(),
                        ReservationStatus.PENDING,
                        now
                )
                .stream()
                .filter(reservation -> !now.isBefore(reservation.getStartTime()))
                .toList();

        Reservation userReservation = validReservations.stream()
                .filter(r -> r.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        boolean hasOtherUserReservation = validReservations.stream()
                .anyMatch(r -> !r.getUserId().equals(userId));

        if (hasOtherUserReservation) {
            throw new BusinessException(ResultCode.NO_VALID_RESERVATION);
        }

        if (request.getVehicleId() != null) {
            vehicleRepository.findByIdAndUserId(request.getVehicleId(), userId)
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        }

        ChargingRecord chargingRecord = new ChargingRecord();
        chargingRecord.setUserId(userId);
        chargingRecord.setChargingPileId(request.getChargingPileId());
        chargingRecord.setVehicleId(request.getVehicleId());
        chargingRecord.setStartTime(now);
        chargingRecord.setStatus(ChargingRecordStatus.CHARGING);
        applyChargingTarget(chargingRecord, chargingPile, request, now);

        chargingRecord = chargingRecordRepository.save(chargingRecord);

        if (userReservation != null) {
            userReservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(userReservation);
            log.info("Start charging with reservation: userId={}, reservationId={}",
                    userId, userReservation.getId());
        }

        chargingPile.setStatus(ChargingPileStatus.CHARGING);
        chargingPileRepository.save(chargingPile);

        log.info("Start charging succeeded: userId={}, recordId={}, pileId={}",
                userId, chargingRecord.getId(), request.getChargingPileId());

        ChargingRecordResponse response = new ChargingRecordResponse();
        response.setId(chargingRecord.getId());
        response.setUserId(chargingRecord.getUserId());
        response.setChargingPileId(chargingRecord.getChargingPileId());
        response.setPileName(chargingPile.getCode());
        response.setPileLocation(chargingPile.getLocation());
        response.setPileType(chargingPile.getType().name());
        response.setVehicleId(chargingRecord.getVehicleId());
        response.setStartTime(chargingRecord.getStartTime());
        response.setTargetType(chargingRecord.getTargetType());
        response.setTargetValue(chargingRecord.getTargetValue());
        response.setTargetDurationMinutes(chargingRecord.getTargetDurationMinutes());
        response.setTargetKwh(chargingRecord.getTargetKwh());
        response.setTargetEndTime(chargingRecord.getTargetEndTime());
        response.setStatus(chargingRecord.getStatus().name());
        response.setStatusDesc(chargingRecord.getStatus().getDescription());
        response.setCreatedTime(chargingRecord.getCreatedTime());
        response.setUpdatedTime(chargingRecord.getUpdatedTime());

        return response;
    }

    private void applyChargingTarget(ChargingRecord chargingRecord, ChargingPile chargingPile,
                                     ChargingRecordStartRequest request, LocalDateTime startTime) {
        BigDecimal targetValue = request.getTargetValue();
        if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        if (chargingPile.getPower() == null || chargingPile.getPower().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "充电桩功率配置无效");
        }

        Integer durationMinutes;
        BigDecimal targetKwh;
        if (request.getTargetType() == ChargingTargetType.DURATION) {
            durationMinutes = targetValue.setScale(0, RoundingMode.CEILING).intValue();
            targetKwh = chargingPile.getPower()
                    .multiply(BigDecimal.valueOf(durationMinutes))
                    .divide(BigDecimal.valueOf(60), 3, RoundingMode.HALF_UP);
        } else if (request.getTargetType() == ChargingTargetType.KWH) {
            targetKwh = targetValue.setScale(3, RoundingMode.HALF_UP);
            durationMinutes = targetKwh
                    .multiply(BigDecimal.valueOf(60))
                    .divide(chargingPile.getPower(), 0, RoundingMode.CEILING)
                    .intValue();
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        if (durationMinutes <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        chargingRecord.setTargetType(request.getTargetType());
        chargingRecord.setTargetValue(targetValue);
        chargingRecord.setTargetDurationMinutes(durationMinutes);
        chargingRecord.setTargetKwh(targetKwh);
        chargingRecord.setTargetEndTime(startTime.plusMinutes(durationMinutes));
        chargingRecord.setPreEndNoticeSent(0);
    }
}
