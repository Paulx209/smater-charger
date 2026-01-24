package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.ChargingPileQueryRequest;
import com.smartcharger.dto.request.NearbyQueryRequest;
import com.smartcharger.dto.response.ChargingPileResponse;
import com.smartcharger.service.ChargingPileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 充电桩控制器
 */
@Slf4j
@RestController
@RequestMapping("/charging-piles")
public class ChargingPileController {

    private final ChargingPileService chargingPileService;

    public ChargingPileController(ChargingPileService chargingPileService) {
        this.chargingPileService = chargingPileService;
    }

    /**
     * 查询充电桩列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> queryChargingPiles(ChargingPileQueryRequest request) {
        log.info("查询充电桩列表，参数：{}", request);

        Page<ChargingPileResponse> page = chargingPileService.queryChargingPiles(request);

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("total", page.getTotalElements());
        data.put("pages", page.getTotalPages());
        data.put("current", page.getNumber() + 1);
        data.put("records", page.getContent());

        return Result.success(data);
    }

    /**
     * 获取充电桩详情
     */
    @GetMapping("/{id}")
    public Result<ChargingPileResponse> getChargingPileById(@PathVariable Long id) {
        log.info("获取充电桩详情，ID：{}", id);

        ChargingPileResponse response = chargingPileService.getChargingPileById(id);
        return Result.success(response);
    }

    /**
     * 获取附近充电桩（地图模式）
     */
    @GetMapping("/nearby")
    public Result<List<ChargingPileResponse>> getNearbyChargingPiles(@Valid NearbyQueryRequest request) {
        log.info("查询附近充电桩，参数：{}", request);

        List<ChargingPileResponse> list = chargingPileService.getNearbyChargingPiles(request);
        return Result.success(list);
    }
}
