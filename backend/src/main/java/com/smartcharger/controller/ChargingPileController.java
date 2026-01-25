package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.*;
import com.smartcharger.dto.response.BatchDeleteResultResponse;
import com.smartcharger.dto.response.ChargingPileResponse;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import com.smartcharger.service.ChargingPileAdminService;
import com.smartcharger.service.ChargingPileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/charging-pile")
@RequiredArgsConstructor
public class ChargingPileController {

    private final ChargingPileService chargingPileService;
    private final ChargingPileAdminService chargingPileAdminService;

    // ==================== 车主端接口 ====================

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
    @GetMapping("/{id:\\d+}")
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

    // ==================== 管理端接口 ====================

    /**
     * 添加充电桩（管理端）
     */
    @PostMapping("/admin")
    public Result<ChargingPileResponse> createChargingPile(@Valid @RequestBody ChargingPileCreateRequest request) {
        ChargingPileResponse response = chargingPileAdminService.createChargingPile(request);
        return Result.success(response);
    }

    /**
     * 更新充电桩信息（管理端）
     */
    @PutMapping("/admin/{id:\\d+}")
    public Result<ChargingPileResponse> updateChargingPile(@PathVariable Long id,
                                                             @Valid @RequestBody ChargingPileUpdateRequest request) {
        ChargingPileResponse response = chargingPileAdminService.updateChargingPile(id, request);
        return Result.success(response);
    }

    /**
     * 删除充电桩（管理端）
     */
    @DeleteMapping("/admin/{id:\\d+}")
    public Result<Void> deleteChargingPile(@PathVariable Long id) {
        chargingPileAdminService.deleteChargingPile(id);
        return Result.success(null);
    }

    /**
     * 查询充电桩列表（管理端）
     */
    @GetMapping("/admin")
    public Result<Page<ChargingPileResponse>> getAdminChargingPileList(
            @RequestParam(required = false) ChargingPileType type,
            @RequestParam(required = false) ChargingPileStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ChargingPileResponse> result = chargingPileAdminService.getAdminChargingPileList(
                type, status, keyword, page, size);
        return Result.success(result);
    }

    /**
     * 查询充电桩详情（管理端）
     */
    @GetMapping("/admin/{id:\\d+}")
    public Result<ChargingPileResponse> getAdminChargingPileDetail(@PathVariable Long id) {
        ChargingPileResponse response = chargingPileAdminService.getAdminChargingPileDetail(id);
        return Result.success(response);
    }

    /**
     * 手动更新充电桩状态（管理端）
     */
    @PutMapping("/admin/{id:\\d+}/status")
    public Result<ChargingPileResponse> updateChargingPileStatus(@PathVariable Long id,
                                                                   @Valid @RequestBody ChargingPileStatusUpdateRequest request) {
        ChargingPileResponse response = chargingPileAdminService.updateChargingPileStatus(id, request);
        return Result.success(response);
    }

    /**
     * 批量删除充电桩（管理端）
     */
    @DeleteMapping("/admin/batch")
    public Result<BatchDeleteResultResponse> batchDeleteChargingPiles(@Valid @RequestBody ChargingPileBatchDeleteRequest request) {
        BatchDeleteResultResponse response = chargingPileAdminService.batchDeleteChargingPiles(request);
        return Result.success(response);
    }
}
