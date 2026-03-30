package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.PriceEstimateRequest;
import com.smartcharger.dto.response.PriceConfigResponse;
import com.smartcharger.dto.response.PriceEstimateResponse;
import com.smartcharger.service.PriceConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/price-config")
@RequiredArgsConstructor
public class PriceConfigController {

    private final PriceConfigService priceConfigService;

    @GetMapping("/current")
    public Result<PriceConfigResponse> getCurrentPriceConfig(@RequestParam String chargingPileType) {
        return Result.success(priceConfigService.getCurrentPriceConfig(chargingPileType));
    }

    @PostMapping("/estimate")
    public Result<PriceEstimateResponse> estimatePrice(@Valid @RequestBody PriceEstimateRequest request) {
        return Result.success(priceConfigService.estimatePrice(request));
    }
}