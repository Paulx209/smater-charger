package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.ReservationCreateRequest;
import com.smartcharger.dto.response.AvailabilityCheckResponse;
import com.smartcharger.dto.response.ReservationResponse;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.service.ReservationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 预约管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 创建预约
     */
    @PostMapping
    public Result<ReservationResponse> createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        Long userId = getCurrentUserId();
        ReservationResponse response = reservationService.createReservation(userId, request);
        return Result.success(response);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        reservationService.cancelReservation(userId, id);
        return Result.success();
    }

    /**
     * 查询我的预约列表
     */
    @GetMapping("/my")
    public Result<Page<ReservationResponse>> getMyReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        Page<ReservationResponse> reservations = reservationService.getMyReservations(userId, status, page, size);
        return Result.success(reservations);
    }

    /**
     * 获取当前进行中的预约
     */
    @GetMapping("/current")
    public Result<ReservationResponse> getCurrentReservation() {
        Long userId = getCurrentUserId();
        ReservationResponse response = reservationService.getCurrentReservation(userId);
        return Result.success(response);
    }

    /**
     * 检查充电桩可用性
     */
    @GetMapping("/check-availability")
    public Result<AvailabilityCheckResponse> checkAvailability(
            @RequestParam Long pileId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        AvailabilityCheckResponse response = reservationService.checkAvailability(pileId, startTime, endTime);
        return Result.success(response);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<ReservationResponse> getReservationById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ReservationResponse response = reservationService.getReservationById(userId, id);
        return Result.success(response);
    }

    /**
     * 从SecurityContext中获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
