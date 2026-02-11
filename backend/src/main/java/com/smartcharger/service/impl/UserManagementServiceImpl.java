package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.BatchUserStatusUpdateRequest;
import com.smartcharger.dto.request.PasswordResetRequest;
import com.smartcharger.dto.request.UserStatusUpdateRequest;
import com.smartcharger.dto.response.*;
import com.smartcharger.entity.*;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.entity.enums.ReservationStatus;
import com.smartcharger.entity.enums.WarningNoticeType;
import com.smartcharger.repository.*;
import com.smartcharger.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类（管理端）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ChargingRecordRepository chargingRecordRepository;
    private final ReservationRepository reservationRepository;
    private final WarningNoticeRepository warningNoticeRepository;
    private final FaultReportRepository faultReportRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserAdminResponse> getAdminUserList(Integer status, String keyword,
                                                      LocalDate startDate, LocalDate endDate,
                                                      Boolean isActive, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Page<User> userPage;

        if (Boolean.TRUE.equals(isActive)) {
            // 查询活跃用户（最近30天有充电记录）
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            userPage = userRepository.findActiveUsersByAdminFilters(
                    status, keyword, startDateTime, endDateTime, thirtyDaysAgo, pageable);
        } else {
            // 查询所有用户
            userPage = userRepository.findByAdminFilters(
                    status, keyword, startDateTime, endDateTime, pageable);
        }

        return userPage.map(this::buildUserAdminResponse);
    }

    @Override
    public UserAdminResponse getAdminUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserAdminResponse response = buildUserAdminResponse(user);

        // 查询用户车辆列表
        List<Vehicle> vehicles = vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedTimeDesc(id);
        response.setVehicles(vehicles.stream()
                .map(this::buildVehicleResponse)
                .collect(Collectors.toList()));

        // 查询最近5条充电记录
        Pageable pageable = PageRequest.of(0, 5);
        List<ChargingRecord> recentRecords = chargingRecordRepository.findRecentRecordsByUserId(id, pageable);
        response.setRecentChargingRecords(recentRecords.stream()
                .map(this::buildChargingRecordResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserStatus(Long id, UserStatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 验证状态值
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(ResultCode.INVALID_USER_STATUS);
        }

        // 如果禁用用户，检查是否有正在进行的充电记录
        if (request.getStatus() == 0) {
            ChargingRecord activeCharging = chargingRecordRepository
                    .findByUserIdAndStatus(id, ChargingRecordStatus.CHARGING)
                    .orElse(null);
            if (activeCharging != null) {
                log.warn("用户 {} 有正在进行的充电记录，建议先结束充电再禁用", id);
                // 这里只是警告，不阻止禁用操作
            }
        }

        user.setStatus(request.getStatus());
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("更新用户状态成功，用户ID：{}，新状态：{}，原因：{}", id, request.getStatus(), request.getReason());

        return buildUserAdminResponse(user);
    }

    @Override
    @Transactional
    public PasswordResetResponse resetUserPassword(Long id, PasswordResetRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        String newPassword;

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // 手动输入密码
            newPassword = request.getNewPassword();
            // 验证密码强度
            if (!isPasswordValid(newPassword)) {
                throw new BusinessException(ResultCode.PASSWORD_TOO_WEAK);
            }
        } else {
            // 系统生成随机密码
            newPassword = generateRandomPassword();
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("重置用户密码成功，用户ID：{}，用户名：{}", id, user.getUsername());

        return PasswordResetResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .newPassword(newPassword)
                .message("请将新密码告知用户，用户首次登录后建议修改密码")
                .build();
    }

    @Override
    public Page<ChargingRecordResponse> getUserChargingRecords(Long id, String status,
                                                                 LocalDate startDate, LocalDate endDate,
                                                                 Integer page, Integer size) {
        // 验证用户是否存在
        userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page - 1, size);

        ChargingRecordStatus recordStatus = status != null ? ChargingRecordStatus.valueOf(status) : null;
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Page<ChargingRecord> recordPage;

        if (recordStatus != null && startDateTime != null && endDateTime != null) {
            recordPage = chargingRecordRepository.findByUserIdAndStatusAndDateRange(
                    id, recordStatus, startDateTime, endDateTime, pageable);
        } else if (recordStatus != null) {
            recordPage = chargingRecordRepository.findByUserIdAndStatus(id, recordStatus, pageable);
        } else if (startDateTime != null && endDateTime != null) {
            recordPage = chargingRecordRepository.findByUserIdAndDateRange(
                    id, startDateTime, endDateTime, pageable);
        } else {
            recordPage = chargingRecordRepository.findByUserId(id, pageable);
        }

        return recordPage.map(this::buildChargingRecordResponse);
    }

    @Override
    public Page<ReservationResponse> getUserReservations(Long id, String status,
                                                           LocalDate startDate, LocalDate endDate,
                                                           Integer page, Integer size) {
        // 验证用户是否存在
        userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page - 1, size);

        ReservationStatus reservationStatus = status != null ? ReservationStatus.valueOf(status) : null;

        Page<Reservation> reservationPage;

        if (reservationStatus != null) {
            reservationPage = reservationRepository.findByUserIdAndStatusOrderByCreatedTimeDesc(
                    id, reservationStatus, pageable);
        } else {
            reservationPage = reservationRepository.findByUserIdOrderByCreatedTimeDesc(id, pageable);
        }

        return reservationPage.map(this::buildReservationResponse);
    }

    @Override
    public Page<ViolationRecordResponse> getUserViolations(Long id, LocalDate startDate, LocalDate endDate,
                                                             Integer page, Integer size) {
        // 验证用户是否存在
        userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page - 1, size);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Page<WarningNotice> violationPage = warningNoticeRepository.findViolationsByUserId(
                id, startDateTime, endDateTime, pageable);

        return violationPage.map(this::buildViolationRecordResponse);
    }

    @Override
    public Workbook exportUsers(Integer status, Boolean isActive) {
        List<User> users;

        if (Boolean.TRUE.equals(isActive)) {
            // 查询活跃用户
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            users = userRepository.findActiveUsersByStatus(status, thirtyDaysAgo);
        } else {
            // 查询所有用户
            users = userRepository.findByStatus(status);
        }

        // 创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("用户列表");

        // 创建标题行样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"用户ID", "用户名", "手机号", "昵称", "真实姓名", "状态",
                "车辆数量", "充电次数", "总消费", "超时次数", "注册时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);

            UserStatisticsResponse statistics = buildUserStatistics(user.getId());

            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(3).setCellValue(user.getNickname() != null ? user.getNickname() : "");
            row.createCell(4).setCellValue(user.getName() != null ? user.getName() : "");
            row.createCell(5).setCellValue(user.getStatus() == 1 ? "启用" : "禁用");
            row.createCell(6).setCellValue(statistics.getVehicleCount());
            row.createCell(7).setCellValue(statistics.getChargingRecordCount());
            row.createCell(8).setCellValue(statistics.getTotalSpent() != null ?
                    statistics.getTotalSpent().doubleValue() : 0.0);
            row.createCell(9).setCellValue(statistics.getOvertimeCount());
            row.createCell(10).setCellValue(user.getCreatedTime().toString());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    @Override
    @Transactional
    public BatchDeleteResultResponse batchUpdateUserStatus(BatchUserStatusUpdateRequest request) {
        int totalCount = request.getUserIds().size();
        int successCount = 0;
        int failCount = 0;
        List<BatchDeleteResultResponse.FailedRecord> failedRecords = new ArrayList<>();

        for (Long userId : request.getUserIds()) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    failCount++;
                    failedRecords.add(BatchDeleteResultResponse.FailedRecord.builder()
                            .id(userId)
                            .reason("用户不存在")
                            .build());
                    continue;
                }

                user.setStatus(request.getStatus());
                user.setUpdatedTime(LocalDateTime.now());
                userRepository.save(user);

                successCount++;
                log.info("批量更新用户状态成功，用户ID：{}，新状态：{}", userId, request.getStatus());
            } catch (Exception e) {
                failCount++;
                failedRecords.add(BatchDeleteResultResponse.FailedRecord.builder()
                        .id(userId)
                        .reason(e.getMessage())
                        .build());
                log.error("批量更新用户状态失败，用户ID：{}", userId, e);
            }
        }

        return BatchDeleteResultResponse.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .failedRecords(failedRecords)
                .build();
    }

    /**
     * 构建用户管理响应对象
     */
    private UserAdminResponse buildUserAdminResponse(User user) {
        UserStatisticsResponse statistics = buildUserStatistics(user.getId());

        return UserAdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .name(user.getName())
                .avatar(user.getAvatar())
                .warningThreshold(user.getWarningThreshold())
                .status(user.getStatus())
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .statistics(statistics)
                .build();
    }

    /**
     * 构建用户统计数据
     */
    private UserStatisticsResponse buildUserStatistics(Long userId) {
        // 车辆数量
        Integer vehicleCount = (int) vehicleRepository.countByUserId(userId);

        // 充电记录数量
        Integer chargingRecordCount = chargingRecordRepository.countByUserId(userId).intValue();

        // 总充电量
        BigDecimal totalElectricQuantity = chargingRecordRepository.sumElectricQuantityByUserId(userId);
        if (totalElectricQuantity == null) {
            totalElectricQuantity = BigDecimal.ZERO;
        }

        // 总消费金额
        BigDecimal totalSpent = chargingRecordRepository.sumFeeByUserId(userId);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        // 平均充电时长
        Double avgDuration = chargingRecordRepository.avgDurationByUserId(userId);
        Integer avgChargingDuration = avgDuration != null ? avgDuration.intValue() : 0;

        // 超时占位次数
        Integer overtimeCount = warningNoticeRepository.countByUserIdAndType(
                userId, WarningNoticeType.OVERTIME_WARNING).intValue();

        // 预约次数
        Integer reservationCount = reservationRepository.countByUserId(userId).intValue();

        // 取消预约次数
        Integer cancelledReservationCount = reservationRepository.countByUserIdAndStatus(
                userId, ReservationStatus.CANCELLED).intValue();

        // 故障报修次数
        Integer faultReportCount = faultReportRepository.countByUserId(userId).intValue();

        // 最后登录时间（暂时为空，需要登录日志功能）
        LocalDateTime lastLoginTime = null;

        // 最后充电时间
        LocalDateTime lastChargingTime = chargingRecordRepository.findLastChargingTimeByUserId(userId);

        return UserStatisticsResponse.builder()
                .vehicleCount(vehicleCount)
                .chargingRecordCount(chargingRecordCount)
                .totalElectricQuantity(totalElectricQuantity)
                .totalSpent(totalSpent)
                .avgChargingDuration(avgChargingDuration)
                .overtimeCount(overtimeCount)
                .reservationCount(reservationCount)
                .cancelledReservationCount(cancelledReservationCount)
                .faultReportCount(faultReportCount)
                .lastLoginTime(lastLoginTime)
                .lastChargingTime(lastChargingTime)
                .build();
    }

    /**
     * 构建车辆响应对象
     */
    private VehicleResponse buildVehicleResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .batteryCapacity(vehicle.getBatteryCapacity())
                .isDefault(vehicle.getIsDefault())
                .createdTime(vehicle.getCreatedTime())
                .updatedTime(vehicle.getUpdatedTime())
                .build();
    }

    /**
     * 构建充电记录响应对象
     */
    private ChargingRecordResponse buildChargingRecordResponse(ChargingRecord record) {
        // 查询充电桩信息
        ChargingPile chargingPile = chargingPileRepository.findById(record.getChargingPileId()).orElse(null);
        String chargingPileCode = chargingPile != null ? chargingPile.getCode() : "";
        String chargingPileLocation = chargingPile != null ? chargingPile.getLocation() : "";

        // 查询车辆信息
        Vehicle vehicle = vehicleRepository.findById(record.getVehicleId()).orElse(null);
        String vehicleLicensePlate = vehicle != null ? vehicle.getLicensePlate() : "";

        // 计算充电时长
        Integer duration = null;
        if (record.getEndTime() != null) {
            duration = (int) java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
        }

        return ChargingRecordResponse.builder()
                .id(record.getId())
                .chargingPileId(record.getChargingPileId())
                .chargingPileCode(chargingPileCode)
                .chargingPileLocation(chargingPileLocation)
                .vehicleLicensePlate(vehicleLicensePlate)
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .duration(duration)
                .electricQuantity(record.getElectricQuantity())
                .fee(record.getFee())
                .status(record.getStatus().name())
                .createdTime(record.getCreatedTime())
                .build();
    }

    /**
     * 构建预约记录响应对象
     */
    private ReservationResponse buildReservationResponse(Reservation reservation) {
        // 查询充电桩信息
        ChargingPile chargingPile = chargingPileRepository.findById(reservation.getChargingPileId()).orElse(null);
        String chargingPileCode = chargingPile != null ? chargingPile.getCode() : "";
        String chargingPileLocation = chargingPile != null ? chargingPile.getLocation() : "";

        return ReservationResponse.builder()
                .id(reservation.getId())
                .chargingPileId(reservation.getChargingPileId())
                .chargingPileCode(chargingPileCode)
                .chargingPileLocation(chargingPileLocation)
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .createdTime(reservation.getCreatedTime())
                .build();
    }

    /**
     * 构建违规记录响应对象
     */
    private ViolationRecordResponse buildViolationRecordResponse(WarningNotice warningNotice) {
        // 查询充电记录信息
        ChargingRecord chargingRecord = chargingRecordRepository.findById(warningNotice.getChargingRecordId()).orElse(null);
        LocalDateTime chargingEndTime = chargingRecord != null ? chargingRecord.getEndTime() : null;

        // 查询充电桩信息
        ChargingPile chargingPile = null;
        if (chargingRecord != null) {
            chargingPile = chargingPileRepository.findById(chargingRecord.getChargingPileId()).orElse(null);
        }
        String chargingPileCode = chargingPile != null ? chargingPile.getCode() : "";
        String chargingPileLocation = chargingPile != null ? chargingPile.getLocation() : "";

        return ViolationRecordResponse.builder()
                .id(warningNotice.getId())
                .chargingRecordId(warningNotice.getChargingRecordId())
                .chargingPileCode(chargingPileCode)
                .chargingPileLocation(chargingPileLocation)
                .chargingEndTime(chargingEndTime)
                .overtimeMinutes(warningNotice.getOvertimeMinutes())
                .warningTime(warningNotice.getCreatedTime())
                .violationType("OVERTIME")
                .createdTime(warningNotice.getCreatedTime())
                .build();
    }

    /**
     * 生成随机密码
     * 规则：8位，包含大写字母、小写字母、数字
     */
    private String generateRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String allChars = upperCase + lowerCase + digits;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);

        // 确保至少包含一个大写字母、一个小写字母、一个数字
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));

        // 填充剩余字符
        for (int i = 3; i < 8; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱顺序
        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        return chars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    /**
     * 验证密码强度
     * 规则：至少8位，包含字母和数字
     */
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasLetter && hasDigit;
    }
}
