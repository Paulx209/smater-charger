import request from '@/utils/request'
import type { VehicleInfo, VehiclePage, VehicleQueryParams } from '@/types/vehicle'

export function getAdminVehicleList(params?: VehicleQueryParams): Promise<VehiclePage> {
  return request.get('/admin/vehicles', { params })
}

export function getAdminVehicleDetail(id: number): Promise<VehicleInfo> {
  return request.get(`/admin/vehicles/${id}`)
}

export function deleteAdminVehicle(id: number): Promise<void> {
  return request.delete(`/admin/vehicles/${id}`)
}
