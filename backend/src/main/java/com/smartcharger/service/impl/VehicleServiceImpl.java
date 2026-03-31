package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.VehicleCreateRequest;
import com.smartcharger.dto.request.VehicleUpdateRequest;
import com.smartcharger.dto.response.AdminVehicleResponse;
import com.smartcharger.dto.response.VehicleResponse;
import com.smartcharger.entity.User;
import com.smartcharger.entity.Vehicle;
import com.smartcharger.repository.UserRepository;
import com.smartcharger.repository.VehicleRepository;
import com.smartcharger.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public VehicleResponse createVehicle(Long userId, VehicleCreateRequest request) {
        log.info("Create vehicle: userId={}, licensePlate={}", userId, request.getLicensePlate());

        if (vehicleRepository.existsByUserIdAndLicensePlate(userId, request.getLicensePlate())) {
            throw new BusinessException(ResultCode.LICENSE_PLATE_EXISTS);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(userId);
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setBatteryCapacity(request.getBatteryCapacity());

        long vehicleCount = vehicleRepository.countByUserId(userId);
        if (vehicleCount == 0) {
            vehicle.setIsDefault(1);
        } else if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            vehicleRepository.clearDefaultByUserId(userId);
            vehicle.setIsDefault(1);
        } else {
            vehicle.setIsDefault(0);
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created: vehicleId={}, licensePlate={}", savedVehicle.getId(), savedVehicle.getLicensePlate());
        return convertToResponse(savedVehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles(Long userId) {
        log.info("List user vehicles: userId={}", userId);
        return vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedTimeDesc(userId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long userId, Long id) {
        log.info("Get vehicle detail: userId={}, vehicleId={}", userId, id);

        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        return convertToResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long userId, Long id, VehicleUpdateRequest request) {
        log.info("Update vehicle: userId={}, vehicleId={}", userId, id);

        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        if (StringUtils.hasText(request.getLicensePlate())
                && !request.getLicensePlate().equals(vehicle.getLicensePlate())
                && vehicleRepository.existsByUserIdAndLicensePlateAndIdNot(userId, request.getLicensePlate(), id)) {
            throw new BusinessException(ResultCode.LICENSE_PLATE_EXISTS);
        }

        if (StringUtils.hasText(request.getLicensePlate())) {
            vehicle.setLicensePlate(request.getLicensePlate());
        }
        if (StringUtils.hasText(request.getBrand())) {
            vehicle.setBrand(request.getBrand());
        }
        if (StringUtils.hasText(request.getModel())) {
            vehicle.setModel(request.getModel());
        }
        if (request.getBatteryCapacity() != null) {
            vehicle.setBatteryCapacity(request.getBatteryCapacity());
        }

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated: vehicleId={}", updatedVehicle.getId());
        return convertToResponse(updatedVehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long userId, Long id) {
        log.info("Delete vehicle: userId={}, vehicleId={}", userId, id);

        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        deleteVehicleInternal(vehicle);
        log.info("Vehicle deleted: vehicleId={}", id);
    }

    @Override
    @Transactional
    public void setDefaultVehicle(Long userId, Long id) {
        log.info("Set default vehicle: userId={}, vehicleId={}", userId, id);

        vehicleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN));

        vehicleRepository.clearDefaultByUserId(userId);
        vehicleRepository.setDefaultById(id);
        log.info("Default vehicle updated: vehicleId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminVehicleResponse> getAdminVehicles(Long userId, String licensePlate, Integer page, Integer size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Vehicle> vehiclePage = vehicleRepository.findByAdminFilters(
                userId,
                StringUtils.hasText(licensePlate) ? licensePlate.trim() : null,
                pageable
        );

        Map<Long, User> userMap = getUserMap(vehiclePage.getContent());
        List<AdminVehicleResponse> responses = vehiclePage.getContent().stream()
                .map(vehicle -> convertToAdminResponse(vehicle, userMap.get(vehicle.getUserId())))
                .toList();

        return new PageImpl<>(responses, pageable, vehiclePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminVehicleResponse getAdminVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        User user = userRepository.findById(vehicle.getUserId()).orElse(null);
        return convertToAdminResponse(vehicle, user);
    }

    @Override
    @Transactional
    public void deleteVehicleByAdmin(Long id) {
        log.info("Delete vehicle by admin: vehicleId={}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        deleteVehicleInternal(vehicle);
        log.info("Vehicle deleted by admin: vehicleId={}", id);
    }

    private void deleteVehicleInternal(Vehicle vehicle) {
        Long userId = vehicle.getUserId();
        boolean wasDefault = Objects.equals(vehicle.getIsDefault(), 1);

        vehicleRepository.delete(vehicle);

        if (!wasDefault) {
            return;
        }

        List<Vehicle> remainingVehicles = vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedTimeDesc(userId);
        if (remainingVehicles.isEmpty()) {
            return;
        }

        Vehicle nextDefaultVehicle = remainingVehicles.get(0);
        nextDefaultVehicle.setIsDefault(1);
        vehicleRepository.save(nextDefaultVehicle);
    }

    private Map<Long, User> getUserMap(List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = vehicles.stream()
                .map(Vehicle::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

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

    private AdminVehicleResponse convertToAdminResponse(Vehicle vehicle, User user) {
        return AdminVehicleResponse.builder()
                .id(vehicle.getId())
                .userId(vehicle.getUserId())
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
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
