package com.smartcharger.service;

import com.smartcharger.dto.request.ReservationCreateRequest;
import com.smartcharger.dto.response.AvailabilityCheckResponse;
import com.smartcharger.dto.response.ReservationResponse;
import com.smartcharger.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReservationService {

    ReservationResponse createReservation(Long userId, ReservationCreateRequest request);

    void cancelReservation(Long userId, Long id);

    Page<ReservationResponse> getMyReservations(Long userId, ReservationStatus status,
                                                Integer page, Integer size);

    ReservationResponse getReservationById(Long userId, Long id);

    ReservationResponse getCurrentReservation(Long userId);

    void handleExpiredReservations();

    AvailabilityCheckResponse checkAvailability(Long pileId, LocalDateTime startTime, LocalDateTime endTime);

    Page<ReservationResponse> getAdminReservations(Long userId, Long chargingPileId,
                                                   ReservationStatus status,
                                                   LocalDate startDate, LocalDate endDate,
                                                   Integer page, Integer size);

    ReservationResponse getAdminReservationById(Long id);

    void cancelReservationByAdmin(Long id);
}