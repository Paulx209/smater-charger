package com.smartcharger.service;

import com.smartcharger.dto.response.ChargingPileUsageResponse;
import com.smartcharger.dto.response.RevenueStatisticsResponse;
import com.smartcharger.dto.response.StatisticsOverviewResponse;
import com.smartcharger.dto.response.UserActivityResponse;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;

public interface StatisticsAdminService {

    StatisticsOverviewResponse getOverview(String rangeType, LocalDate startDate, LocalDate endDate);

    ChargingPileUsageResponse getChargingPileUsage(String rangeType, LocalDate startDate, LocalDate endDate);

    RevenueStatisticsResponse getRevenueStatistics(String rangeType, LocalDate startDate, LocalDate endDate);

    UserActivityResponse getUserActivity(String rangeType, LocalDate startDate, LocalDate endDate);

    Workbook exportStatistics(String rangeType, LocalDate startDate, LocalDate endDate);
}
