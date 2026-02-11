package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.response.ChargingPileUsageResponse;
import com.smartcharger.dto.response.RevenueStatisticsResponse;
import com.smartcharger.dto.response.StatisticsOverviewResponse;
import com.smartcharger.dto.response.UserActivityResponse;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.repository.UserRepository;
import com.smartcharger.service.StatisticsAdminService;
import com.smartcharger.util.ExcelExportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsAdminServiceImpl implements StatisticsAdminService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ChargingPileRepository chargingPileRepository;
    private final ChargingRecordRepository chargingRecordRepository;
    private final UserRepository userRepository;

    @Override
    public StatisticsOverviewResponse getOverview(String rangeType, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(rangeType, startDate, endDate);
        ChargingPileUsageResponse chargingPileUsage = buildChargingPileUsage(dateRange);
        RevenueStatisticsResponse revenueStatistics = buildRevenueStatistics(dateRange);
        UserActivityResponse userActivity = buildUserActivity(dateRange);
        return buildOverview(dateRange, chargingPileUsage, revenueStatistics, userActivity);
    }

    @Override
    public ChargingPileUsageResponse getChargingPileUsage(String rangeType, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(rangeType, startDate, endDate);
        return buildChargingPileUsage(dateRange);
    }

    @Override
    public RevenueStatisticsResponse getRevenueStatistics(String rangeType, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(rangeType, startDate, endDate);
        return buildRevenueStatistics(dateRange);
    }

    @Override
    public UserActivityResponse getUserActivity(String rangeType, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(rangeType, startDate, endDate);
        return buildUserActivity(dateRange);
    }

    @Override
    public Workbook exportStatistics(String rangeType, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(rangeType, startDate, endDate);
        ChargingPileUsageResponse chargingPileUsage = buildChargingPileUsage(dateRange);
        RevenueStatisticsResponse revenueStatistics = buildRevenueStatistics(dateRange);
        UserActivityResponse userActivity = buildUserActivity(dateRange);
        StatisticsOverviewResponse overview = buildOverview(dateRange, chargingPileUsage, revenueStatistics, userActivity);

        return ExcelExportUtil.buildStatisticsWorkbook(
                dateRange.getRangeType(),
                dateRange.getStartTime(),
                dateRange.getEndTime(),
                overview,
                chargingPileUsage,
                revenueStatistics,
                userActivity
        );
    }

    private StatisticsOverviewResponse buildOverview(DateRange dateRange,
                                                     ChargingPileUsageResponse chargingPileUsage,
                                                     RevenueStatisticsResponse revenueStatistics,
                                                     UserActivityResponse userActivity) {
        return StatisticsOverviewResponse.builder()
                .rangeType(dateRange.getRangeType())
                .startTime(dateRange.getStartTime())
                .endTime(dateRange.getEndTime())
                .totalChargingPileCount(chargingPileUsage.getTotalChargingPileCount())
                .usedChargingPileCount(chargingPileUsage.getUsedChargingPileCount())
                .chargingPileUsageRate(chargingPileUsage.getChargingPileUsageRate())
                .totalRevenue(revenueStatistics.getTotalRevenue())
                .averageDailyRevenue(revenueStatistics.getAverageDailyRevenue())
                .activeUserCount(userActivity.getActiveUserCount())
                .newUserCount(userActivity.getNewUserCount())
                .totalChargingCount(revenueStatistics.getTotalChargingCount())
                .build();
    }

    private ChargingPileUsageResponse buildChargingPileUsage(DateRange dateRange) {
        long totalPileCount = chargingPileRepository.count();
        long usedPileCount = defaultLong(chargingRecordRepository.countUsedChargingPilesByStartTimeRange(
                dateRange.getStartTime(), dateRange.getEndExclusive()));
        BigDecimal usageRate = calculateRate(usedPileCount, totalPileCount);

        return ChargingPileUsageResponse.builder()
                .rangeType(dateRange.getRangeType())
                .startTime(dateRange.getStartTime())
                .endTime(dateRange.getEndTime())
                .totalChargingPileCount(safeInt(totalPileCount))
                .usedChargingPileCount(safeInt(usedPileCount))
                .chargingPileUsageRate(usageRate)
                .idleCount(safeInt(chargingPileRepository.countByStatus(ChargingPileStatus.IDLE)))
                .chargingCount(safeInt(chargingPileRepository.countByStatus(ChargingPileStatus.CHARGING)))
                .faultCount(safeInt(chargingPileRepository.countByStatus(ChargingPileStatus.FAULT)))
                .reservedCount(safeInt(chargingPileRepository.countByStatus(ChargingPileStatus.RESERVED)))
                .overtimeCount(safeInt(chargingPileRepository.countByStatus(ChargingPileStatus.OVERTIME)))
                .build();
    }

    private RevenueStatisticsResponse buildRevenueStatistics(DateRange dateRange) {
        BigDecimal totalRevenue = defaultBigDecimal(chargingRecordRepository.sumCompletedFeeByStartTimeRange(
                dateRange.getStartTime(), dateRange.getEndExclusive()));
        long totalChargingCount = defaultLong(chargingRecordRepository.countCompletedRecordsByStartTimeRange(
                dateRange.getStartTime(), dateRange.getEndExclusive()));
        BigDecimal avgDailyRevenue = calculateAverageDailyRevenue(totalRevenue, dateRange.getTotalDays());

        List<Object[]> dailyRows = chargingRecordRepository.aggregateDailyRevenueByStartTimeRange(
                dateRange.getStartTime(), dateRange.getEndExclusive());
        Map<LocalDate, RevenueStatisticsResponse.DailyRevenueRecord> dailyMap = new HashMap<>();
        for (Object[] row : dailyRows) {
            LocalDate date = toLocalDate(row[0]);
            if (date == null) {
                continue;
            }
            dailyMap.put(date, RevenueStatisticsResponse.DailyRevenueRecord.builder()
                    .date(date.toString())
                    .revenue(toBigDecimal(row[1]))
                    .chargingCount(safeInt(toLong(row[2])))
                    .build());
        }

        List<RevenueStatisticsResponse.DailyRevenueRecord> dailyRecords = new ArrayList<>();
        for (LocalDate date : buildDateAxis(dateRange)) {
            RevenueStatisticsResponse.DailyRevenueRecord record = dailyMap.get(date);
            if (record == null) {
                record = RevenueStatisticsResponse.DailyRevenueRecord.builder()
                        .date(date.toString())
                        .revenue(BigDecimal.ZERO)
                        .chargingCount(0)
                        .build();
            }
            dailyRecords.add(record);
        }

        return RevenueStatisticsResponse.builder()
                .rangeType(dateRange.getRangeType())
                .startTime(dateRange.getStartTime())
                .endTime(dateRange.getEndTime())
                .totalRevenue(totalRevenue)
                .averageDailyRevenue(avgDailyRevenue)
                .totalChargingCount(safeInt(totalChargingCount))
                .dailyRecords(dailyRecords)
                .build();
    }

    private UserActivityResponse buildUserActivity(DateRange dateRange) {
        long activeUserCount = defaultLong(chargingRecordRepository.countDistinctActiveUsersByStartTimeRange(
                dateRange.getStartTime(), dateRange.getEndExclusive()));
        long newUserCount = defaultLong(userRepository.countByCreatedTimeGreaterThanEqualAndCreatedTimeLessThan(
                dateRange.getStartTime(), dateRange.getEndExclusive()));

        List<Object[]> dailyActiveRows = chargingRecordRepository.countDailyDistinctActiveUsersByStartTimeRange(
                dateRange.getStartTime(), dateRange.getEndExclusive());
        Map<LocalDate, Long> dailyActiveMap = new HashMap<>();
        for (Object[] row : dailyActiveRows) {
            LocalDate date = toLocalDate(row[0]);
            if (date == null) {
                continue;
            }
            dailyActiveMap.put(date, toLong(row[1]));
        }

        List<Object[]> dailyNewRows = userRepository.countDailyNewUsersByCreatedTimeRange(
                dateRange.getStartTime(), dateRange.getEndExclusive());
        Map<LocalDate, Long> dailyNewMap = new HashMap<>();
        for (Object[] row : dailyNewRows) {
            LocalDate date = toLocalDate(row[0]);
            if (date == null) {
                continue;
            }
            dailyNewMap.put(date, toLong(row[1]));
        }

        List<UserActivityResponse.DailyActivityRecord> dailyRecords = new ArrayList<>();
        for (LocalDate date : buildDateAxis(dateRange)) {
            long dailyActive = dailyActiveMap.getOrDefault(date, 0L);
            long dailyNew = dailyNewMap.getOrDefault(date, 0L);
            dailyRecords.add(UserActivityResponse.DailyActivityRecord.builder()
                    .date(date.toString())
                    .activeUserCount(safeInt(dailyActive))
                    .newUserCount(safeInt(dailyNew))
                    .build());
        }

        return UserActivityResponse.builder()
                .rangeType(dateRange.getRangeType())
                .startTime(dateRange.getStartTime())
                .endTime(dateRange.getEndTime())
                .activeUserCount(safeInt(activeUserCount))
                .newUserCount(safeInt(newUserCount))
                .dailyRecords(dailyRecords)
                .build();
    }

    private DateRange resolveDateRange(String rangeType, LocalDate startDate, LocalDate endDate) {
        String normalizedRangeType = rangeType == null ? "TODAY" : rangeType.trim().toUpperCase(Locale.ROOT);

        LocalDate today = LocalDate.now();
        LocalDateTime startTime;
        LocalDateTime endExclusive;

        switch (normalizedRangeType) {
            case "TODAY":
                startTime = today.atStartOfDay();
                endExclusive = today.plusDays(1).atStartOfDay();
                break;
            case "WEEK":
                startTime = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                endExclusive = today.plusDays(1).atStartOfDay();
                break;
            case "MONTH":
                startTime = today.withDayOfMonth(1).atStartOfDay();
                endExclusive = today.plusDays(1).atStartOfDay();
                break;
            case "CUSTOM":
                if (startDate == null || endDate == null) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "startDate and endDate are required for CUSTOM range.");
                }
                if (endDate.isBefore(startDate)) {
                    throw new BusinessException(ResultCode.INVALID_TIME_RANGE);
                }
                startTime = startDate.atStartOfDay();
                endExclusive = endDate.plusDays(1).atStartOfDay();
                break;
            default:
                throw new BusinessException(ResultCode.BAD_REQUEST, "rangeType must be TODAY, WEEK, MONTH or CUSTOM.");
        }

        if (!endExclusive.isAfter(startTime)) {
            throw new BusinessException(ResultCode.INVALID_TIME_RANGE);
        }

        return new DateRange(normalizedRangeType, startTime, endExclusive);
    }

    private List<LocalDate> buildDateAxis(DateRange dateRange) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = dateRange.getStartTime().toLocalDate();
             date.isBefore(dateRange.getEndExclusive().toLocalDate());
             date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }

    private BigDecimal calculateRate(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(part)
                .multiply(HUNDRED)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageDailyRevenue(BigDecimal totalRevenue, long totalDays) {
        if (totalDays <= 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private int safeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static class DateRange {
        private final String rangeType;
        private final LocalDateTime startTime;
        private final LocalDateTime endExclusive;

        private DateRange(String rangeType, LocalDateTime startTime, LocalDateTime endExclusive) {
            this.rangeType = rangeType;
            this.startTime = startTime;
            this.endExclusive = endExclusive;
        }

        private String getRangeType() {
            return rangeType;
        }

        private LocalDateTime getStartTime() {
            return startTime;
        }

        private LocalDateTime getEndExclusive() {
            return endExclusive;
        }

        private LocalDateTime getEndTime() {
            return endExclusive.minusSeconds(1);
        }

        private long getTotalDays() {
            return ChronoUnit.DAYS.between(startTime.toLocalDate(), endExclusive.toLocalDate());
        }
    }
}
