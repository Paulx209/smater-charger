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
 * 充电记录服务接口
 */
public interface ChargingRecordService {

    /**
     * 开始充电
     */
    ChargingRecordResponse startCharging(Long userId, ChargingRecordStartRequest request);

    /**
     * 结束充电
     */
    ChargingRecordResponse endCharging(Long userId, Long recordId, ChargingRecordEndRequest request);

    /**
     * 查询充电记录列表
     */
    Page<ChargingRecordResponse> getChargingRecordList(Long userId, ChargingRecordStatus status,
                                                         LocalDate startDate, LocalDate endDate,
                                                         Integer page, Integer size);

    /**
     * 查询充电记录详情
     */
    ChargingRecordResponse getChargingRecordDetail(Long userId, Long recordId);

    /**
     * 查询当前充电记录
     */
    ChargingRecordResponse getCurrentChargingRecord(Long userId);

    /**
     * 月度统计
     */
    ChargingStatisticsMonthlyResponse getMonthlyStatistics(Long userId, Integer year, Integer month);

    /**
     * 年度统计
     */
    ChargingStatisticsYearlyResponse getYearlyStatistics(Long userId, Integer year);

    /**
     * 管理端：查询所有充电记录
     */
    Page<ChargingRecordResponse> getAllChargingRecords(Long userId, Long chargingPileId,
                                                         ChargingRecordStatus status,
                                                         LocalDate startDate, LocalDate endDate,
                                                         Integer page, Integer size);
}
