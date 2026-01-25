package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.BatchUserStatusUpdateRequest;
import com.smartcharger.dto.request.PasswordResetRequest;
import com.smartcharger.dto.request.UserStatusUpdateRequest;
import com.smartcharger.dto.response.*;
import com.smartcharger.service.UserManagementService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

/**
 * 用户管理控制器（管理端）
 */
@Slf4j
@RestController
@RequestMapping("/user/admin")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * 查询用户列表（管理端）
     */
    @GetMapping
    public Result<Page<UserAdminResponse>> getAdminUserList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询用户列表，参数：status={}, keyword={}, startDate={}, endDate={}, isActive={}, page={}, size={}",
                status, keyword, startDate, endDate, isActive, page, size);

        Page<UserAdminResponse> result = userManagementService.getAdminUserList(
                status, keyword, startDate, endDate, isActive, page, size);
        return Result.success(result);
    }

    /**
     * 查询用户详情（管理端）
     */
    @GetMapping("/{id:\\d+}")
    public Result<UserAdminResponse> getAdminUserDetail(@PathVariable Long id) {
        log.info("查询用户详情，ID：{}", id);

        UserAdminResponse response = userManagementService.getAdminUserDetail(id);
        return Result.success(response);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id:\\d+}/status")
    public Result<UserAdminResponse> updateUserStatus(@PathVariable Long id,
                                                        @Valid @RequestBody UserStatusUpdateRequest request) {
        log.info("更新用户状态，ID：{}，请求：{}", id, request);

        UserAdminResponse response = userManagementService.updateUserStatus(id, request);
        return Result.success(response);
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id:\\d+}/reset-password")
    public Result<PasswordResetResponse> resetUserPassword(@PathVariable Long id,
                                                             @Valid @RequestBody PasswordResetRequest request) {
        log.info("重置用户密码，ID：{}", id);

        PasswordResetResponse response = userManagementService.resetUserPassword(id, request);
        return Result.success(response);
    }

    /**
     * 查看用户充电记录
     */
    @GetMapping("/{id:\\d+}/charging-records")
    public Result<Page<ChargingRecordResponse>> getUserChargingRecords(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("查看用户充电记录，用户ID：{}，参数：status={}, startDate={}, endDate={}, page={}, size={}",
                id, status, startDate, endDate, page, size);

        Page<ChargingRecordResponse> result = userManagementService.getUserChargingRecords(
                id, status, startDate, endDate, page, size);
        return Result.success(result);
    }

    /**
     * 查看用户预约记录
     */
    @GetMapping("/{id:\\d+}/reservations")
    public Result<Page<ReservationResponse>> getUserReservations(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("查看用户预约记录，用户ID：{}，参数：status={}, startDate={}, endDate={}, page={}, size={}",
                id, status, startDate, endDate, page, size);

        Page<ReservationResponse> result = userManagementService.getUserReservations(
                id, status, startDate, endDate, page, size);
        return Result.success(result);
    }

    /**
     * 查看用户违规记录
     */
    @GetMapping("/{id:\\d+}/violations")
    public Result<Page<ViolationRecordResponse>> getUserViolations(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("查看用户违规记录，用户ID：{}，参数：startDate={}, endDate={}, page={}, size={}",
                id, startDate, endDate, page, size);

        Page<ViolationRecordResponse> result = userManagementService.getUserViolations(
                id, startDate, endDate, page, size);
        return Result.success(result);
    }

    /**
     * 导出用户列表
     */
    @GetMapping("/export")
    public void exportUsers(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean isActive,
            HttpServletResponse response) throws IOException {
        log.info("导出用户列表，参数：status={}, isActive={}", status, isActive);

        Workbook workbook = userManagementService.exportUsers(status, isActive);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=users_" + LocalDate.now() + ".xlsx");

        // 写入响应流
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 批量更新用户状态
     */
    @PutMapping("/batch-status")
    public Result<BatchDeleteResultResponse> batchUpdateUserStatus(
            @Valid @RequestBody BatchUserStatusUpdateRequest request) {
        log.info("批量更新用户状态，请求：{}", request);

        BatchDeleteResultResponse response = userManagementService.batchUpdateUserStatus(request);
        return Result.success(response);
    }
}
