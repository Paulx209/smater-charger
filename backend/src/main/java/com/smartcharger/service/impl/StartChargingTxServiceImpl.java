package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;
import com.smartcharger.entity.*;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.repository.*;
import com.smartcharger.service.StartChargingTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (chargingPile.getStatus() == ChargingPileStatus.CHARGING) {
            throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
        }
        if (chargingPile.getStatus() == ChargingPileStatus.FAULT) {
            throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> validReservations = reservationRepository
                .findByChargingPileIdAndStatusAndEndTimeAfter(
                        request.getChargingPileId(),
                        ReservationStatus.PENDING,
                        now
                );

        Reservation userReservation = validReservations.stream()
                .filter(r -> r.getUserId().equals(userId) &&
                        r.getStartTime().isBefore(now.plusMinutes(30)))
                .findFirst()
                .orElse(null);

        boolean hasOtherUserReservation = validReservations.stream()
                .anyMatch(r -> !r.getUserId().equals(userId));

        if (hasOtherUserReservation) {
            throw new BusinessException(ResultCode.NO_VALID_RESERVATION);
        }

        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndUserId(request.getVehicleId(), userId)
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        }

        ChargingRecord chargingRecord = new ChargingRecord();
        chargingRecord.setUserId(userId);
        chargingRecord.setChargingPileId(request.getChargingPileId());
        chargingRecord.setVehicleId(request.getVehicleId());
        chargingRecord.setStartTime(LocalDateTime.now());
        chargingRecord.setStatus(ChargingRecordStatus.CHARGING);

        chargingRecord = chargingRecordRepository.save(chargingRecord);

        if (userReservation != null) {
            userReservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(userReservation);
            log.info("使用预约开始充电: userId={}, reservationId={}", userId, userReservation.getId());
        }

        chargingPile.setStatus(ChargingPileStatus.CHARGING);
        chargingPileRepository.save(chargingPile);

        log.info("开始充电成功: userId={}, recordId={}, pileId={}",
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
        response.setStatus(chargingRecord.getStatus().name());
        response.setStatusDesc(chargingRecord.getStatus().getDescription());
        response.setCreatedTime(chargingRecord.getCreatedTime());
        response.setUpdatedTime(chargingRecord.getUpdatedTime());

        return response;
    }
}
