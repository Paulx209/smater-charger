package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.VehicleCreateRequest;
import com.smartcharger.dto.request.VehicleUpdateRequest;
import com.smartcharger.dto.response.VehicleResponse;
import com.smartcharger.entity.Vehicle;
import com.smartcharger.repository.VehicleRepository;
import com.smartcharger.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆服务实现类
 */
@Slf4j
@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public VehicleResponse createVehicle(Long userId, VehicleCreateRequest request) {
        log.info("添加车辆: userId={}, licensePlate={}", userId, request.getLicensePlate());

        // 1. 检查车牌号是否已存在（同一用户下）
        if (vehicleRepository.existsByUserIdAndLicensePlate(userId, request.getLicensePlate())) {
            throw new BusinessException(ResultCode.LICENSE_PLATE_EXISTS);
        }

        // 2. 创建车辆
        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(userId);
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setBatteryCapacity(request.getBatteryCapacity());

        // 3. 处理默认车辆逻辑
        long vehicleCount = vehicleRepository.countByUserId(userId);
        if (vehicleCount == 0) {
            // 如果是第一辆车，自动设为默认
            vehicle.setIsDefault(1);
        } else if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            // 如果设置为默认，需要将其他车辆设为非默认
            vehicleRepository.clearDefaultByUserId(userId);
            vehicle.setIsDefault(1);
        } else {
            vehicle.setIsDefault(0);
        }

        // 4. 保存车辆
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        log.info("添加车辆成功: vehicleId={}, licensePlate={}", savedVehicle.getId(), savedVehicle.getLicensePlate());

        return convertToResponse(savedVehicle);
    }

    @Override
    public List<VehicleResponse> getMyVehicles(Long userId) {
        log.info("查询我的车辆列表: userId={}", userId);

        List<Vehicle> vehicles = vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedTimeDesc(userId);

        return vehicles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getVehicleById(Long userId, Long id) {
        log.info("获取车辆详情: userId={}, vehicleId={}", userId, id);

        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        return convertToResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long userId, Long id, VehicleUpdateRequest request) {
        log.info("更新车辆信息: userId={}, vehicleId={}", userId, id);

        // 1. 查询车辆（权限校验）
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        // 2. 检查车牌号是否重复（排除当前车辆）
        if (StringUtils.hasText(request.getLicensePlate()) &&
                !request.getLicensePlate().equals(vehicle.getLicensePlate())) {
            if (vehicleRepository.existsByUserIdAndLicensePlateAndIdNot(userId, request.getLicensePlate(), id)) {
                throw new BusinessException(ResultCode.LICENSE_PLATE_EXISTS);
            }
            vehicle.setLicensePlate(request.getLicensePlate());
        }

        // 3. 更新车辆信息
        if (StringUtils.hasText(request.getBrand())) {
            vehicle.setBrand(request.getBrand());
        }
        if (StringUtils.hasText(request.getModel())) {
            vehicle.setModel(request.getModel());
        }
        if (request.getBatteryCapacity() != null) {
            vehicle.setBatteryCapacity(request.getBatteryCapacity());
        }

        // 4. 保存更新
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        log.info("更新车辆成功: vehicleId={}", updatedVehicle.getId());

        return convertToResponse(updatedVehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long userId, Long id) {
        log.info("删除车辆: userId={}, vehicleId={}", userId, id);

        // 1. 查询车辆（权限校验）
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        // 2. 删除车辆
        vehicleRepository.delete(vehicle);

        // 3. 如果删除的是默认车辆，且用户还有其他车辆，自动设置第一辆为默认
        if (vehicle.getIsDefault() == 1) {
            List<Vehicle> remainingVehicles = vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedTimeDesc(userId);
            if (!remainingVehicles.isEmpty()) {
                Vehicle firstVehicle = remainingVehicles.get(0);
                firstVehicle.setIsDefault(1);
                vehicleRepository.save(firstVehicle);
                log.info("自动设置新的默认车辆: vehicleId={}", firstVehicle.getId());
            }
        }

        log.info("删除车辆成功: vehicleId={}", id);
    }

    @Override
    @Transactional
    public void setDefaultVehicle(Long userId, Long id) {
        log.info("设置默认车辆: userId={}, vehicleId={}", userId, id);

        // 1. 查询车辆（权限校验）
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        // 2. 将用户的所有车辆设为非默认
        vehicleRepository.clearDefaultByUserId(userId);

        // 3. 设置指定车辆为默认
        vehicleRepository.setDefaultById(id);

        log.info("设置默认车辆成功: vehicleId={}", id);
    }

    /**
     * 将Vehicle实体转换为VehicleResponse
     */
    private VehicleResponse convertToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .userId(vehicle.getUserId())
                .licensePlate(vehicle.getLicensePlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .batteryCapacity(vehicle.getBatteryCapacity())
                .isDefault(vehicle.getIsDefault())
                .createdTime(vehicle.getCreatedTime())
                .updatedTime(vehicle.getUpdatedTime())
                .build();
    }
}
