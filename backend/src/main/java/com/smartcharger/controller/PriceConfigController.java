package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.PriceConfigCreateRequest;
import com.smartcharger.dto.request.PriceConfigUpdateRequest;
import com.smartcharger.dto.request.PriceEstimateRequest;
import com.smartcharger.dto.response.PriceConfigResponse;
import com.smartcharger.dto.response.PriceEstimateResponse;
import com.smartcharger.service.PriceConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 费用配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/price-config")
@RequiredArgsConstructor
public class PriceConfigController {

    private final PriceConfigService priceConfigService;

    /**
     * 创建费用配置
     * 权限：管理员
     */
    @PostMapping
    public Result<PriceConfigResponse> createPriceConfig(@Valid @RequestBody PriceConfigCreateRequest request) {
        PriceConfigResponse response = priceConfigService.createPriceConfig(request);
        return Result.success(response);
    }

    /**
     * 更新费用配置
     * 权限：管理员
     */
    @PutMapping("/{id:\\d+}")
    public Result<PriceConfigResponse> updatePriceConfig(@PathVariable Long id,
                                                          @Valid @RequestBody PriceConfigUpdateRequest request) {
        PriceConfigResponse response = priceConfigService.updatePriceConfig(id, request);
        return Result.success(response);
    }

    /**
     * 删除费用配置
     * 权限：管理员
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> deletePriceConfig(@PathVariable Long id) {
        priceConfigService.deletePriceConfig(id);
        return Result.success(null);
    }

    /**
     * 查询费用配置列表（分页）
     * 权限：管理员
     */
    @GetMapping
    public Result<Page<PriceConfigResponse>> getPriceConfigList(
            @RequestParam(required = false) String chargingPileType,
            @RequestParam(required = false) Integer isActive,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<PriceConfigResponse> result = priceConfigService.getPriceConfigList(chargingPileType, isActive, page, size);
        return Result.success(result);
    }

    /**
     * 查询费用配置详情
     * 权限：管理员
     */
    @GetMapping("/{id:\\d+}")
    public Result<PriceConfigResponse> getPriceConfigDetail(@PathVariable Long id) {
        PriceConfigResponse response = priceConfigService.getPriceConfigDetail(id);
        return Result.success(response);
    }

    /**
     * 获取当前有效费用配置
     * 权限：所有登录用户
     */
    @GetMapping("/current")
    public Result<PriceConfigResponse> getCurrentPriceConfig(@RequestParam String chargingPileType) {
        PriceConfigResponse response = priceConfigService.getCurrentPriceConfig(chargingPileType);
        return Result.success(response);
    }

    /**
     * 费用预估
     * 权限：所有登录用户
     */
    @PostMapping("/estimate")
    public Result<PriceEstimateResponse> estimatePrice(@Valid @RequestBody PriceEstimateRequest request) {
        PriceEstimateResponse response = priceConfigService.estimatePrice(request);
        return Result.success(response);
    }
}
