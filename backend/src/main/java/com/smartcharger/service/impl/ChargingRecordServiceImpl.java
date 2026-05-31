package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ChargingRecordEndRequest;
import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;
import com.smartcharger.dto.response.ChargingStatisticsMonthlyResponse;
import com.smartcharger.dto.response.ChargingStatisticsYearlyResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.ChargingRecord;
import com.smartcharger.entity.Vehicle;
import com.smartcharger.entity.enums.ChargingEndReason;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.repository.VehicleRepository;
import com.smartcharger.service.ChargingRecordService;
import com.smartcharger.service.PriceConfigService;
import com.smartcharger.service.StartChargingTxService;
import com.smartcharger.service.WarningNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingRecordServiceImpl implements ChargingRecordService {

    private static final int PRE_END_NOTICE_MINUTES = 5;

    private final ChargingRecordRepository chargingRecordRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final VehicleRepository vehicleRepository;
    private final PriceConfigService priceConfigService;
    private final RedissonClient redissonClient;
    private final StartChargingTxService startChargingTxService;
    private final WarningNoticeService warningNoticeService;

    @Override
    public ChargingRecordResponse startCharging(Long userId, ChargingRecordStartRequest request) {
        String userLockKey = "charging:start:user:" + userId;
        String pileLockKey = "charging:start:pile:" + request.getChargingPileId();

        RLock userLock = null;
        RLock pileLock = null;

        try {
            userLock = redissonClient.getLock(userLockKey);
            if (!userLock.tryLock(5, 30, TimeUnit.SECONDS)) {
                log.warn("Failed to acquire lock for user {}: timeout", userId);
                Optional<ChargingRecord> existingRecord = chargingRecordRepository.findByUserIdAndStatus(
                        userId, ChargingRecordStatus.CHARGING);
                if (existingRecord.isPresent()) {
                    throw new BusinessException(ResultCode.USER_ALREADY_CHARGING);
                }
                throw new BusinessException(ResultCode.SYSTEM_BUSY);
            }
            log.info("Acquired lock for user {}", userId);

            pileLock = redissonClient.getLock(pileLockKey);
            if (!pileLock.tryLock(5, 30, TimeUnit.SECONDS)) {
                log.warn("Failed to acquire lock for pile {}: timeout", request.getChargingPileId());
                throw new BusinessException(ResultCode.CHARGING_PILE_BUSY);
            }
            log.info("Acquired lock for user {} and pile {}", userId, request.getChargingPileId());

            return startChargingTxService.startChargingInTx(userId, request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted for user {}", userId, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis connection error or unexpected exception for user {}", userId, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (pileLock != null && pileLock.isHeldByCurrentThread()) {
                try {
                    pileLock.unlock();
                    log.info("Released lock for pile {}", request.getChargingPileId());
                } catch (Exception e) {
                    log.error("Failed to release pile lock", e);
                }
            }
            if (userLock != null && userLock.isHeldByCurrentThread()) {
                try {
                    userLock.unlock();
                    log.info("Released lock for user {}", userId);
                } catch (Exception e) {
                    log.error("Failed to release user lock", e);
                }
            }
        }
    }

    @Override
    @Transactional
    public ChargingRecordResponse endCharging(Long userId, Long recordId, ChargingRecordEndRequest request) {
        ChargingRecord chargingRecord = chargingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_RECORD_NOT_FOUND));

        if (!chargingRecord.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        ChargingPile chargingPile = chargingPileRepository.findById(chargingRecord.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        chargingRecord = completeCharging(chargingRecord, chargingPile, ChargingEndReason.USER_MANUAL);
        return convertToResponse(chargingRecord, chargingPile, null);
    }

    @Override
    @Transactional
    public ChargingRecordResponse confirmLeave(Long userId, Long recordId) {
        ChargingRecord chargingRecord = chargingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_RECORD_NOT_FOUND));

        if (!chargingRecord.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (chargingRecord.getStatus() != ChargingRecordStatus.COMPLETED || chargingRecord.getEndTime() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "充电记录尚未完成");
        }

        ChargingPile chargingPile = chargingPileRepository.findById(chargingRecord.getChargingPileId())
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));

        Optional<ChargingRecord> currentOccupancy = chargingRecordRepository
                .findFirstByChargingPileIdAndStatusAndLeaveTimeIsNullOrderByEndTimeDesc(
                        chargingRecord.getChargingPileId(), ChargingRecordStatus.COMPLETED);
        if (currentOccupancy.isPresent() && !currentOccupancy.get().getId().equals(chargingRecord.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (chargingRecord.getLeaveTime() == null) {
            chargingRecord.setLeaveTime(LocalDateTime.now());
            chargingRecord = chargingRecordRepository.save(chargingRecord);
        }

        if (chargingPile.getStatus() == ChargingPileStatus.WAITING_LEAVE
                || chargingPile.getStatus() == ChargingPileStatus.OVERTIME) {
            chargingPile.setStatus(ChargingPileStatus.IDLE);
            chargingPileRepository.save(chargingPile);
        }

        log.info("Owner confirmed leaving: userId={}, recordId={}, pileId={}",
                userId, recordId, chargingPile.getId());
        return convertToResponse(chargingRecord, chargingPile, null);
    }

    @Override
    @Transactional
    public void autoCompleteDueChargingRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<ChargingRecord> dueRecords = chargingRecordRepository
                .findByStatusAndTargetEndTimeLessThanEqual(ChargingRecordStatus.CHARGING, now);

        for (ChargingRecord record : dueRecords) {
            try {
                ChargingPile chargingPile = chargingPileRepository.findById(record.getChargingPileId())
                        .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));
                completeCharging(record, chargingPile, ChargingEndReason.AUTO_TARGET_REACHED);
            } catch (Exception e) {
                log.error("Auto complete charging failed: recordId={}", record.getId(), e);
            }
        }
    }

    @Override
    @Transactional
    public void sendPreEndChargingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderEnd = now.plusMinutes(PRE_END_NOTICE_MINUTES);
        List<ChargingRecord> records = chargingRecordRepository
                .findByStatusAndPreEndNoticeSentAndTargetEndTimeBetween(
                        ChargingRecordStatus.CHARGING, 0, now, reminderEnd);

        for (ChargingRecord record : records) {
            try {
                ChargingPile pile = chargingPileRepository.findById(record.getChargingPileId())
                        .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));
                warningNoticeService.createChargingEndingSoonNotice(
                        record.getUserId(),
                        record.getChargingPileId(),
                        record.getId(),
                        pile.getCode(),
                        record.getTargetEndTime()
                );
                record.setPreEndNoticeSent(1);
                chargingRecordRepository.save(record);
            } catch (Exception e) {
                log.error("Create pre-end charging reminder failed: recordId={}", record.getId(), e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChargingRecordResponse> getChargingRecordList(Long userId, ChargingRecordStatus status,
                                                              LocalDate startDate, LocalDate endDate,
                                                              Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startTime"));

        Page<ChargingRecord> recordPage;

        if (status != null && startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            recordPage = chargingRecordRepository.findByUserIdAndStatusAndDateRange(
                    userId, status, startDateTime, endDateTime, pageable);
        } else if (status != null) {
            recordPage = chargingRecordRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            recordPage = chargingRecordRepository.findByUserIdAndDateRange(
                    userId, startDateTime, endDateTime, pageable);
        } else {
            recordPage = chargingRecordRepository.findByUserId(userId, pageable);
        }

        List<ChargingRecord> records = recordPage.getContent();
        Map<Long, ChargingPile> pileMap = batchFetchPiles(records);
        Map<Long, Vehicle> vehicleMap = batchFetchVehicles(records);

        List<ChargingRecordResponse> responses = records.stream()
                .map(record -> convertToResponse(
                        record,
                        pileMap.get(record.getChargingPileId()),
                        vehicleMap.get(record.getVehicleId())))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(responses, pageable, recordPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ChargingRecordResponse getChargingRecordDetail(Long userId, Long recordId) {
        ChargingRecord chargingRecord = chargingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_RECORD_NOT_FOUND));

        if (!chargingRecord.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        ChargingPile pile = chargingPileRepository.findById(chargingRecord.getChargingPileId()).orElse(null);
        Vehicle vehicle = chargingRecord.getVehicleId() != null
                ? vehicleRepository.findById(chargingRecord.getVehicleId()).orElse(null)
                : null;

        ChargingRecordResponse response = convertToResponse(chargingRecord, pile, vehicle);
        enrichFeeBreakdown(chargingRecord, pile, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ChargingRecordResponse getAdminChargingRecordDetail(Long recordId) {
        ChargingRecord chargingRecord = chargingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_RECORD_NOT_FOUND));

        ChargingPile pile = chargingPileRepository.findById(chargingRecord.getChargingPileId()).orElse(null);
        Vehicle vehicle = chargingRecord.getVehicleId() != null
                ? vehicleRepository.findById(chargingRecord.getVehicleId()).orElse(null)
                : null;

        ChargingRecordResponse response = convertToResponse(chargingRecord, pile, vehicle);
        enrichFeeBreakdown(chargingRecord, pile, response);
        return response;
    }

    @Override
    public ChargingRecordResponse getCurrentChargingRecord(Long userId) {
        Optional<ChargingRecord> recordOpt = chargingRecordRepository.findByUserIdAndStatus(
                userId, ChargingRecordStatus.CHARGING);

        if (recordOpt.isEmpty()) {
            return null;
        }

        ChargingRecord record = recordOpt.get();
        ChargingPile pile = chargingPileRepository.findById(record.getChargingPileId()).orElse(null);
        Vehicle vehicle = record.getVehicleId() != null
                ? vehicleRepository.findById(record.getVehicleId()).orElse(null)
                : null;

        return convertToResponse(record, pile, vehicle);
    }

    @Override
    public ChargingStatisticsMonthlyResponse getMonthlyStatistics(Long userId, Integer year, Integer month) {
        List<ChargingRecord> records = chargingRecordRepository.findSettledRecordsByMonth(userId, year, month);

        int totalCount = records.size();
        BigDecimal totalElectricQuantity = records.stream()
                .map(ChargingRecord::getElectricQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFee = records.stream()
                .map(ChargingRecord::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, List<ChargingRecord>> groupedByDate = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStartTime().format(dateFormatter)));

        List<ChargingStatisticsMonthlyResponse.DailyRecord> dailyRecords = groupedByDate.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<ChargingRecord> dayRecords = entry.getValue();

                    int count = dayRecords.size();
                    BigDecimal electricQuantity = dayRecords.stream()
                            .map(ChargingRecord::getElectricQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal fee = dayRecords.stream()
                            .map(ChargingRecord::getFee)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ChargingStatisticsMonthlyResponse.DailyRecord.builder()
                            .date(date)
                            .count(count)
                            .electricQuantity(electricQuantity)
                            .fee(fee)
                            .build();
                })
                .sorted(Comparator.comparing(ChargingStatisticsMonthlyResponse.DailyRecord::getDate))
                .collect(Collectors.toList());

        return ChargingStatisticsMonthlyResponse.builder()
                .year(year)
                .month(month)
                .totalCount(totalCount)
                .totalElectricQuantity(totalElectricQuantity)
                .totalFee(totalFee)
                .records(dailyRecords)
                .build();
    }

    @Override
    public ChargingStatisticsYearlyResponse getYearlyStatistics(Long userId, Integer year) {
        List<ChargingRecord> records = chargingRecordRepository.findSettledRecordsByYear(userId, year);

        int totalCount = records.size();
        BigDecimal totalElectricQuantity = records.stream()
                .map(ChargingRecord::getElectricQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFee = records.stream()
                .map(ChargingRecord::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Integer, List<ChargingRecord>> groupedByMonth = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStartTime().getMonthValue()));

        List<ChargingStatisticsYearlyResponse.MonthlyRecord> monthlyRecords = groupedByMonth.entrySet().stream()
                .map(entry -> {
                    Integer month = entry.getKey();
                    List<ChargingRecord> monthRecords = entry.getValue();

                    int count = monthRecords.size();
                    BigDecimal electricQuantity = monthRecords.stream()
                            .map(ChargingRecord::getElectricQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal fee = monthRecords.stream()
                            .map(ChargingRecord::getFee)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ChargingStatisticsYearlyResponse.MonthlyRecord.builder()
                            .month(month)
                            .count(count)
                            .electricQuantity(electricQuantity)
                            .fee(fee)
                            .build();
                })
                .sorted(Comparator.comparing(ChargingStatisticsYearlyResponse.MonthlyRecord::getMonth))
                .collect(Collectors.toList());

        return ChargingStatisticsYearlyResponse.builder()
                .year(year)
                .totalCount(totalCount)
                .totalElectricQuantity(totalElectricQuantity)
                .totalFee(totalFee)
                .records(monthlyRecords)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChargingRecordResponse> getAllChargingRecords(Long userId, Long chargingPileId,
                                                              ChargingRecordStatus status,
                                                              LocalDate startDate, LocalDate endDate,
                                                              Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startTime"));

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Page<ChargingRecord> recordPage = chargingRecordRepository.findByAdminFilters(
                userId, chargingPileId, status, startDateTime, endDateTime, pageable);

        List<ChargingRecord> records = recordPage.getContent();
        Map<Long, ChargingPile> pileMap = batchFetchPiles(records);
        Map<Long, Vehicle> vehicleMap = batchFetchVehicles(records);

        List<ChargingRecordResponse> responses = records.stream()
                .map(record -> convertToResponse(
                        record,
                        pileMap.get(record.getChargingPileId()),
                        vehicleMap.get(record.getVehicleId())))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(responses, pageable, recordPage.getTotalElements());
    }

    private ChargingRecord completeCharging(ChargingRecord chargingRecord, ChargingPile chargingPile,
                                            ChargingEndReason endReason) {
        if (chargingRecord.getStatus() != ChargingRecordStatus.CHARGING) {
            throw new BusinessException(ResultCode.CHARGING_RECORD_NOT_CHARGING);
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(chargingRecord.getStartTime(), endTime);
        int durationMinutes = Math.max(0, (int) duration.toMinutes());
        BigDecimal durationHours = BigDecimal.valueOf(duration.getSeconds())
                .divide(BigDecimal.valueOf(3600), 6, RoundingMode.DOWN);

        BigDecimal electricQuantity = chargingPile.getPower()
                .multiply(durationHours)
                .setScale(3, RoundingMode.DOWN);
        BigDecimal fee = priceConfigService.calculateFee(chargingPile.getType().name(), electricQuantity);

        chargingRecord.setEndTime(endTime);
        chargingRecord.setDuration(durationMinutes);
        chargingRecord.setElectricQuantity(electricQuantity);
        chargingRecord.setFee(fee);
        chargingRecord.setStatus(ChargingRecordStatus.COMPLETED);
        chargingRecord.setEndReason(endReason);
        chargingRecord = chargingRecordRepository.save(chargingRecord);

        chargingPile.setStatus(ChargingPileStatus.WAITING_LEAVE);
        chargingPileRepository.save(chargingPile);

        warningNoticeService.createChargingCompletedNotice(
                chargingRecord.getUserId(),
                chargingRecord.getChargingPileId(),
                chargingRecord.getId(),
                chargingPile.getCode()
        );

        log.info("Charging completed: userId={}, recordId={}, reason={}, duration={}min, quantity={}, fee={}",
                chargingRecord.getUserId(), chargingRecord.getId(), endReason,
                durationMinutes, electricQuantity, fee);
        return chargingRecord;
    }

    private void enrichFeeBreakdown(ChargingRecord chargingRecord, ChargingPile pile, ChargingRecordResponse response) {
        if (chargingRecord.getStatus() != ChargingRecordStatus.COMPLETED
                || pile == null
                || chargingRecord.getElectricQuantity() == null) {
            return;
        }

        try {
            var priceConfig = priceConfigService.getCurrentPriceConfig(pile.getType().name());
            response.setPricePerKwh(priceConfig.getPricePerKwh());
            response.setServiceFee(priceConfig.getServiceFee());

            BigDecimal electricityFee = chargingRecord.getElectricQuantity()
                    .multiply(priceConfig.getPricePerKwh())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal serviceFeeTotal = chargingRecord.getElectricQuantity()
                    .multiply(priceConfig.getServiceFee())
                    .setScale(2, RoundingMode.HALF_UP);

            ChargingRecordResponse.FeeBreakdown breakdown = ChargingRecordResponse.FeeBreakdown.builder()
                    .electricityFee(electricityFee)
                    .serviceFee(serviceFeeTotal)
                    .build();
            response.setFeeBreakdown(breakdown);
        } catch (Exception e) {
            log.warn("Failed to calculate charging record fee breakdown: {}", e.getMessage());
        }
    }

    private Map<Long, ChargingPile> batchFetchPiles(List<ChargingRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> pileIds = records.stream()
                .map(ChargingRecord::getChargingPileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (pileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return chargingPileRepository.findAllById(pileIds).stream()
                .collect(Collectors.toMap(ChargingPile::getId, pile -> pile));
    }

    private Map<Long, Vehicle> batchFetchVehicles(List<ChargingRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> vehicleIds = records.stream()
                .map(ChargingRecord::getVehicleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (vehicleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return vehicleRepository.findAllById(vehicleIds).stream()
                .collect(Collectors.toMap(Vehicle::getId, vehicle -> vehicle));
    }

    private ChargingRecordResponse convertToResponse(ChargingRecord record, ChargingPile pile, Vehicle vehicle) {
        return ChargingRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .chargingPileId(record.getChargingPileId())
                .chargingPileCode(pile != null ? pile.getCode() : null)
                .chargingPileLocation(pile != null ? pile.getLocation() : null)
                .pileName(pile != null ? pile.getCode() : null)
                .pileLocation(pile != null ? pile.getLocation() : null)
                .pileType(pile != null ? pile.getType().name() : null)
                .vehicleId(record.getVehicleId())
                .vehicleLicensePlate(vehicle != null ? vehicle.getLicensePlate() : null)
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .leaveTime(record.getLeaveTime())
                .targetType(record.getTargetType())
                .targetValue(record.getTargetValue())
                .targetDurationMinutes(record.getTargetDurationMinutes())
                .targetKwh(record.getTargetKwh())
                .targetEndTime(record.getTargetEndTime())
                .endReason(record.getEndReason())
                .duration(record.getDuration())
                .electricQuantity(record.getElectricQuantity())
                .fee(record.getFee())
                .status(record.getStatus().name())
                .statusDesc(record.getStatus().getDescription())
                .createdTime(record.getCreatedTime())
                .updatedTime(record.getUpdatedTime())
                .build();
    }
}
