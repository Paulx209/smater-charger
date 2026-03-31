package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.response.AdminVehicleResponse;
import com.smartcharger.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/vehicles")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public Result<Page<AdminVehicleResponse>> getAdminVehicleList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Admin vehicle list query: userId={}, licensePlate={}, page={}, size={}",
                userId, licensePlate, page, size);
        return Result.success(vehicleService.getAdminVehicles(userId, licensePlate, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public Result<AdminVehicleResponse> getAdminVehicleDetail(@PathVariable Long id) {
        log.info("Admin vehicle detail query: id={}", id);
        return Result.success(vehicleService.getAdminVehicleById(id));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> deleteAdminVehicle(@PathVariable Long id) {
        log.info("Admin vehicle delete: id={}", id);
        vehicleService.deleteVehicleByAdmin(id);
        return Result.success();
    }
}
