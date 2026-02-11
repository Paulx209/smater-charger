package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.VehicleCreateRequest;
import com.smartcharger.dto.request.VehicleUpdateRequest;
import com.smartcharger.dto.response.VehicleResponse;
import com.smartcharger.service.VehicleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * 添加车辆
     */
    @PostMapping
    public Result<VehicleResponse> createVehicle(@Valid @RequestBody VehicleCreateRequest request) {
        Long userId = getCurrentUserId();
        VehicleResponse response = vehicleService.createVehicle(userId, request);
        return Result.success(response);
    }

    /**
     * 查询我的车辆列表
     */
    @GetMapping
    public Result<List<VehicleResponse>> getMyVehicles() {
        Long userId = getCurrentUserId();
        List<VehicleResponse> vehicles = vehicleService.getMyVehicles(userId);
        return Result.success(vehicles);
    }

    /**
     * 获取车辆详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<VehicleResponse> getVehicleById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        VehicleResponse response = vehicleService.getVehicleById(userId, id);
        return Result.success(response);
    }

    /**
     * 更新车辆信息
     */
    @PutMapping("/{id:\\d+}")
    public Result<VehicleResponse> updateVehicle(@PathVariable Long id,
                                                  @Valid @RequestBody VehicleUpdateRequest request) {
        Long userId = getCurrentUserId();
        VehicleResponse response = vehicleService.updateVehicle(userId, id, request);
        return Result.success(response);
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> deleteVehicle(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        vehicleService.deleteVehicle(userId, id);
        return Result.success();
    }

    /**
     * 设置默认车辆
     */
    @PutMapping("/{id:\\d+}/default")
    public Result<Void> setDefaultVehicle(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        vehicleService.setDefaultVehicle(userId, id);
        return Result.success();
    }

    /**
     * 从SecurityContext中获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
