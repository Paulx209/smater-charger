package com.smartcharger.service;

import com.smartcharger.dto.request.ChargingPileBatchDeleteRequest;
import com.smartcharger.dto.request.ChargingPileCreateRequest;
import com.smartcharger.dto.request.ChargingPileStatusUpdateRequest;
import com.smartcharger.dto.request.ChargingPileUpdateRequest;
import com.smartcharger.dto.response.BatchDeleteResultResponse;
import com.smartcharger.dto.response.ChargingPileResponse;
import com.smartcharger.dto.response.ChargingPileStatisticsResponse;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import org.springframework.data.domain.Page;

/**
 * 充电桩管理服务接口（管理端）
 */
public interface ChargingPileAdminService {

    /**
     * 添加充电桩
     */
    ChargingPileResponse createChargingPile(ChargingPileCreateRequest request);

    /**
     * 更新充电桩信息
     */
    ChargingPileResponse updateChargingPile(Long id, ChargingPileUpdateRequest request);

    /**
     * 删除充电桩
     */
    void deleteChargingPile(Long id);

    /**
     * 查询充电桩列表（管理端）
     */
    Page<ChargingPileResponse> getAdminChargingPileList(ChargingPileType type, ChargingPileStatus status,
                                                          String keyword, Integer page, Integer size);

    /**
     * 查询充电桩详情（管理端，包含统计数据）
     */
    ChargingPileResponse getAdminChargingPileDetail(Long id);

    /**
     * 手动更新充电桩状态
     */
    ChargingPileResponse updateChargingPileStatus(Long id, ChargingPileStatusUpdateRequest request);

    /**
     * 批量删除充电桩
     */
    BatchDeleteResultResponse batchDeleteChargingPiles(ChargingPileBatchDeleteRequest request);

    /**
     * 获取充电桩统计数据
     */
    ChargingPileStatisticsResponse getChargingPileStatistics(Long id);
}
