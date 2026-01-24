package com.smartcharger.service;

import com.smartcharger.dto.request.ChargingPileQueryRequest;
import com.smartcharger.dto.request.NearbyQueryRequest;
import com.smartcharger.dto.response.ChargingPileResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充电桩服务接口
 */
public interface ChargingPileService {

    /**
     * 分页查询充电桩
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<ChargingPileResponse> queryChargingPiles(ChargingPileQueryRequest request);

    /**
     * 获取充电桩详情
     *
     * @param id 充电桩ID
     * @return 充电桩详情
     */
    ChargingPileResponse getChargingPileById(Long id);

    /**
     * 获取附近充电桩
     *
     * @param request 查询请求
     * @return 附近充电桩列表
     */
    List<ChargingPileResponse> getNearbyChargingPiles(NearbyQueryRequest request);

    /**
     * 计算两点之间的距离（Haversine公式）
     *
     * @param lng1 点1经度
     * @param lat1 点1纬度
     * @param lng2 点2经度
     * @param lat2 点2纬度
     * @return 距离（km）
     */
    Double calculateDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2);
}
