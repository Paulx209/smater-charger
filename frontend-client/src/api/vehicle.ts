import request from '@/utils/request'
import type { VehicleInfo, VehicleCreateRequest, VehicleUpdateRequest } from '@/types/vehicle'

/**
 * 添加车辆
 */
export function createVehicle(data: VehicleCreateRequest): Promise<VehicleInfo> {
  return request.post('/vehicles', data)
}

/**
 * 查询我的车辆列表
 */
export function getMyVehicles(): Promise<VehicleInfo[]> {
  return request.get('/vehicles')
}

/**
 * 获取车辆详情
 */
export function getVehicleById(id: number): Promise<VehicleInfo> {
  return request.get(`/vehicles/${id}`)
}

/**
 * 更新车辆信息
 */
export function updateVehicle(id: number, data: VehicleUpdateRequest): Promise<VehicleInfo> {
  return request.put(`/vehicles/${id}`, data)
}

/**
 * 删除车辆
 */
export function deleteVehicle(id: number): Promise<void> {
  return request.delete(`/vehicles/${id}`)
}

/**
 * 设置默认车辆
 */
export function setDefaultVehicle(id: number): Promise<void> {
  return request.put(`/vehicles/${id}/default`)
}
