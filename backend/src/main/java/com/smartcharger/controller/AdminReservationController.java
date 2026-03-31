package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.response.ReservationResponse;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public Result<Page<ReservationResponse>> getAdminReservationList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long chargingPileId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Admin reservation list query: userId={}, chargingPileId={}, status={}, startDate={}, endDate={}, page={}, size={}",
                userId, chargingPileId, status, startDate, endDate, page, size);

        return Result.success(reservationService.getAdminReservations(
                userId, chargingPileId, status, startDate, endDate, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public Result<ReservationResponse> getAdminReservationDetail(@PathVariable Long id) {
        log.info("Admin reservation detail query: id={}", id);
        return Result.success(reservationService.getAdminReservationById(id));
    }

    @PutMapping("/{id:\\d+}/cancel")
    public Result<Void> cancelReservationByAdmin(@PathVariable Long id) {
        log.info("Admin reservation cancel: id={}", id);
        reservationService.cancelReservationByAdmin(id);
        return Result.success();
    }
}