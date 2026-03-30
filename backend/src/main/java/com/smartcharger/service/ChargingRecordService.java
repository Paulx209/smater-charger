package com.smartcharger.service;

import com.smartcharger.dto.request.ChargingRecordEndRequest;
import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;
import com.smartcharger.dto.response.ChargingStatisticsMonthlyResponse;
import com.smartcharger.dto.response.ChargingStatisticsYearlyResponse;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

/**
 * 鍏呯數璁板綍鏈嶅姟鎺ュ彛
 */
public interface ChargingRecordService {

    /**
     * 寮€濮嬪厖鐢?
     */
    ChargingRecordResponse startCharging(Long userId, ChargingRecordStartRequest request);

    /**
     * 缁撴潫鍏呯數
     */
    ChargingRecordResponse endCharging(Long userId, Long recordId, ChargingRecordEndRequest request);

    /**
     * 鏌ヨ鍏呯數璁板綍鍒楄〃
     */
    Page<ChargingRecordResponse> getChargingRecordList(Long userId, ChargingRecordStatus status,
                                                         LocalDate startDate, LocalDate endDate,
                                                         Integer page, Integer size);

    /**
     * 鏌ヨ鍏呯數璁板綍璇︽儏
     */
    ChargingRecordResponse getChargingRecordDetail(Long userId, Long recordId);

    /**
     * 鏌ヨ褰撳墠鍏呯數璁板綍
     */
    /**
     * Admin detail lookup for a charging record.
     */
    ChargingRecordResponse getAdminChargingRecordDetail(Long recordId);

    /**
     * éŒãƒ¨î‡—è¤°æ’³å¢ éå‘¯æ•¸ç’æ¿ç¶
     */
    ChargingRecordResponse getCurrentChargingRecord(Long userId);

    /**
     * 鏈堝害缁熻
     */
    ChargingStatisticsMonthlyResponse getMonthlyStatistics(Long userId, Integer year, Integer month);

    /**
     * 骞村害缁熻
     */
    ChargingStatisticsYearlyResponse getYearlyStatistics(Long userId, Integer year);

    /**
     * 绠＄悊绔細鏌ヨ鎵€鏈夊厖鐢佃褰?
     */
    Page<ChargingRecordResponse> getAllChargingRecords(Long userId, Long chargingPileId,
                                                         ChargingRecordStatus status,
                                                         LocalDate startDate, LocalDate endDate,
                                                         Integer page, Integer size);
}
