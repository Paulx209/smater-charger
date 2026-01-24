package com.smartcharger.service;

import com.smartcharger.dto.request.PriceConfigCreateRequest;
import com.smartcharger.dto.request.PriceConfigUpdateRequest;
import com.smartcharger.dto.request.PriceEstimateRequest;
import com.smartcharger.dto.response.PriceConfigResponse;
import com.smartcharger.dto.response.PriceEstimateResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

/**
 * 费用配置服务接口
 */
public interface PriceConfigService {

    /**
     * 创建费用配置
     */
    PriceConfigResponse createPriceConfig(PriceConfigCreateRequest request);

    /**
     * 更新费用配置
     */
    PriceConfigResponse updatePriceConfig(Long id, PriceConfigUpdateRequest request);

    /**
     * 删除费用配置
     */
    void deletePriceConfig(Long id);

    /**
     * 查询费用配置列表（分页）
     */
    Page<PriceConfigResponse> getPriceConfigList(String chargingPileType, Integer isActive, Integer page, Integer size);

    /**
     * 查询费用配置详情
     */
    PriceConfigResponse getPriceConfigDetail(Long id);

    /**
     * 获取当前有效费用配置
     */
    PriceConfigResponse getCurrentPriceConfig(String chargingPileType);

    /**
     * 费用预估
     */
    PriceEstimateResponse estimatePrice(PriceEstimateRequest request);

    /**
     * 计算充电费用（内部方法，供其他模块调用）
     */
    BigDecimal calculateFee(String chargingPileType, BigDecimal electricQuantity);
}
