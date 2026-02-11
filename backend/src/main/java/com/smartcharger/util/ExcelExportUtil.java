package com.smartcharger.util;

import com.smartcharger.dto.response.ChargingPileUsageResponse;
import com.smartcharger.dto.response.RevenueStatisticsResponse;
import com.smartcharger.dto.response.StatisticsOverviewResponse;
import com.smartcharger.dto.response.UserActivityResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public final class ExcelExportUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExcelExportUtil() {
    }

    public static Workbook buildStatisticsWorkbook(String rangeType,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime,
                                                   StatisticsOverviewResponse overview,
                                                   ChargingPileUsageResponse chargingPileUsage,
                                                   RevenueStatisticsResponse revenueStatistics,
                                                   UserActivityResponse userActivity) {
        Workbook workbook = new XSSFWorkbook();
        CellStyle headerStyle = createHeaderStyle(workbook);

        createOverviewSheet(workbook, headerStyle, rangeType, startTime, endTime, overview);
        createChargingPileUsageSheet(workbook, headerStyle, chargingPileUsage);
        createRevenueSheet(workbook, headerStyle, revenueStatistics);
        createUserActivitySheet(workbook, headerStyle, userActivity);

        return workbook;
    }

    private static void createOverviewSheet(Workbook workbook,
                                            CellStyle headerStyle,
                                            String rangeType,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime,
                                            StatisticsOverviewResponse overview) {
        Sheet sheet = workbook.createSheet("Overview");
        createHeaderRow(sheet, headerStyle, "Metric", "Value");

        int rowIndex = 1;
        writeMetricRow(sheet, rowIndex++, "Range Type", safe(rangeType));
        writeMetricRow(sheet, rowIndex++, "Start Time", formatDateTime(startTime));
        writeMetricRow(sheet, rowIndex++, "End Time", formatDateTime(endTime));
        writeMetricRow(sheet, rowIndex++, "Total Charging Piles", String.valueOf(safeInt(overview.getTotalChargingPileCount())));
        writeMetricRow(sheet, rowIndex++, "Used Charging Piles", String.valueOf(safeInt(overview.getUsedChargingPileCount())));
        writeMetricRow(sheet, rowIndex++, "Charging Pile Usage Rate", safeDecimal(overview.getChargingPileUsageRate()) + "%");
        writeMetricRow(sheet, rowIndex++, "Total Revenue", safeDecimal(overview.getTotalRevenue()));
        writeMetricRow(sheet, rowIndex++, "Average Daily Revenue", safeDecimal(overview.getAverageDailyRevenue()));
        writeMetricRow(sheet, rowIndex++, "Active Users", String.valueOf(safeInt(overview.getActiveUserCount())));
        writeMetricRow(sheet, rowIndex++, "New Users", String.valueOf(safeInt(overview.getNewUserCount())));
        writeMetricRow(sheet, rowIndex++, "Total Charging Count", String.valueOf(safeInt(overview.getTotalChargingCount())));

        autoSizeColumns(sheet, 2);
    }

    private static void createChargingPileUsageSheet(Workbook workbook, CellStyle headerStyle, ChargingPileUsageResponse usage) {
        Sheet sheet = workbook.createSheet("ChargingPileUsage");
        createHeaderRow(sheet, headerStyle, "Metric", "Value");

        int rowIndex = 1;
        writeMetricRow(sheet, rowIndex++, "Total Charging Piles", String.valueOf(safeInt(usage.getTotalChargingPileCount())));
        writeMetricRow(sheet, rowIndex++, "Used Charging Piles", String.valueOf(safeInt(usage.getUsedChargingPileCount())));
        writeMetricRow(sheet, rowIndex++, "Usage Rate", safeDecimal(usage.getChargingPileUsageRate()) + "%");
        writeMetricRow(sheet, rowIndex++, "Idle Count", String.valueOf(safeInt(usage.getIdleCount())));
        writeMetricRow(sheet, rowIndex++, "Charging Count", String.valueOf(safeInt(usage.getChargingCount())));
        writeMetricRow(sheet, rowIndex++, "Fault Count", String.valueOf(safeInt(usage.getFaultCount())));
        writeMetricRow(sheet, rowIndex++, "Reserved Count", String.valueOf(safeInt(usage.getReservedCount())));
        writeMetricRow(sheet, rowIndex++, "Overtime Count", String.valueOf(safeInt(usage.getOvertimeCount())));

        autoSizeColumns(sheet, 2);
    }

    private static void createRevenueSheet(Workbook workbook, CellStyle headerStyle, RevenueStatisticsResponse revenue) {
        Sheet sheet = workbook.createSheet("Revenue");
        createHeaderRow(sheet, headerStyle, "Date", "Revenue", "Charging Count");

        int rowIndex = 1;
        List<RevenueStatisticsResponse.DailyRevenueRecord> records =
                revenue.getDailyRecords() == null ? Collections.emptyList() : revenue.getDailyRecords();
        for (RevenueStatisticsResponse.DailyRevenueRecord record : records) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(safe(record.getDate()));
            row.createCell(1).setCellValue(safeDecimal(record.getRevenue()));
            row.createCell(2).setCellValue(safeInt(record.getChargingCount()));
        }

        autoSizeColumns(sheet, 3);
    }

    private static void createUserActivitySheet(Workbook workbook, CellStyle headerStyle, UserActivityResponse userActivity) {
        Sheet sheet = workbook.createSheet("UserActivity");
        createHeaderRow(sheet, headerStyle, "Date", "Active Users", "New Users");

        int rowIndex = 1;
        List<UserActivityResponse.DailyActivityRecord> records =
                userActivity.getDailyRecords() == null ? Collections.emptyList() : userActivity.getDailyRecords();
        for (UserActivityResponse.DailyActivityRecord record : records) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(safe(record.getDate()));
            row.createCell(1).setCellValue(safeInt(record.getActiveUserCount()));
            row.createCell(2).setCellValue(safeInt(record.getNewUserCount()));
        }

        autoSizeColumns(sheet, 3);
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    private static void createHeaderRow(Sheet sheet, CellStyle headerStyle, String... headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void writeMetricRow(Sheet sheet, int rowIndex, String metric, String value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(metric);
        row.createCell(1).setCellValue(value);
    }

    private static void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static String formatDateTime(LocalDateTime time) {
        return time == null ? "" : time.format(DATE_TIME_FORMATTER);
    }

    private static String safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.toPlainString() : value.toPlainString();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
