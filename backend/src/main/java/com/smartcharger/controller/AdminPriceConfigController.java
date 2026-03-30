package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.PriceConfigCreateRequest;
import com.smartcharger.dto.request.PriceConfigUpdateRequest;
import com.smartcharger.dto.response.PriceConfigResponse;
import com.smartcharger.service.PriceConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/price-config")
@RequiredArgsConstructor
public class AdminPriceConfigController {

    private final PriceConfigService priceConfigService;

    @PostMapping
    public Result<PriceConfigResponse> createPriceConfig(@Valid @RequestBody PriceConfigCreateRequest request) {
        return Result.success(priceConfigService.createPriceConfig(request));
    }

    @PutMapping("/{id:\\d+}")
    public Result<PriceConfigResponse> updatePriceConfig(
            @PathVariable Long id,
            @Valid @RequestBody PriceConfigUpdateRequest request) {
        return Result.success(priceConfigService.updatePriceConfig(id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> deletePriceConfig(@PathVariable Long id) {
        priceConfigService.deletePriceConfig(id);
        return Result.success(null);
    }

    @GetMapping
    public Result<Page<PriceConfigResponse>> getPriceConfigList(
            @RequestParam(required = false) String chargingPileType,
            @RequestParam(required = false) Integer isActive,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(priceConfigService.getPriceConfigList(chargingPileType, isActive, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public Result<PriceConfigResponse> getPriceConfigDetail(@PathVariable Long id) {
        return Result.success(priceConfigService.getPriceConfigDetail(id));
    }
}