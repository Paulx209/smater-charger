package com.smartcharger.service;

import com.smartcharger.dto.request.VehicleCreateRequest;
import com.smartcharger.dto.request.VehicleUpdateRequest;
import com.smartcharger.dto.response.VehicleResponse;

import java.util.List;

/**
 * 车辆服务接口
 */
public interface VehicleService {

    /**
     * 添加车辆
     */
    VehicleResponse createVehicle(Long userId, VehicleCreateRequest request);

    /**
     * 查询我的车辆列表
     */
    List<VehicleResponse> getMyVehicles(Long userId);

    /**
     * 获取车辆详情
     */
    VehicleResponse getVehicleById(Long userId, Long id);

    /**
     * 更新车辆信息
     */
    VehicleResponse updateVehicle(Long userId, Long id, VehicleUpdateRequest request);

    /**
     * 删除车辆
     */
    void deleteVehicle(Long userId, Long id);

    /**
     * 设置默认车辆
     */
    void setDefaultVehicle(Long userId, Long id);
}
