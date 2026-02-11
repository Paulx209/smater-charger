/**
 * 充电桩管理相关API接口（管理端）
 */
import request from '@/utils/request'

export interface ChargingPile {
  id?: number
  name: string
  location: string
  latitude: number
  longitude: number
  type: 'SLOW' | 'FAST' | 'SUPER_FAST'
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'FAULT'
  power: number
  currentPower: number
  createdAt?: string
  updatedAt?: string
}

export interface ChargingPileQueryParams {
  type?: 'SLOW' | 'FAST' | 'SUPER_FAST'
  status?: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'FAULT'
  keyword?: string
  page?: number
  size?: number
}

export interface ChargingPileCreateRequest {
  name: string
  location: string
  latitude: number
  longitude: number
  type: 'SLOW' | 'FAST' | 'SUPER_FAST'
  power: number
}

export interface ChargingPileUpdateRequest {
  name?: string
  location?: string
  latitude?: number
  longitude?: number
  type?: 'SLOW' | 'FAST' | 'SUPER_FAST'
  power?: number
}

export interface ChargingPileStatusUpdateRequest {
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'FAULT'
}

export interface BatchDeleteRequest {
  ids: number[]
}

export interface BatchDeleteResult {
  successCount: number
  failCount: number
  failedIds: number[]
}

export interface ImportResult {
  successCount: number
  failCount: number
  errorMessages: string[]
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

/**
 * 查询充电桩列表（管理端）
 */
export function getAdminChargingPileList(params: ChargingPileQueryParams): Promise<PageResponse<ChargingPile>> {
  return request({
    url: '/admin/charging-piles',
    method: 'get',
    params
  })
}

/**
 * 查询充电桩详情（管理端）
 */
export function getAdminChargingPileDetail(id: number): Promise<ChargingPile> {
  return request({
    url: `/admin/charging-piles/${id}`,
    method: 'get'
  })
}

/**
 * 添加充电桩（管理端）
 */
export function createChargingPile(data: ChargingPileCreateRequest): Promise<ChargingPile> {
  return request({
    url: '/admin/charging-piles',
    method: 'post',
    data
  })
}

/**
 * 更新充电桩信息（管理端）
 */
export function updateChargingPile(id: number, data: ChargingPileUpdateRequest): Promise<ChargingPile> {
  return request({
    url: `/admin/charging-piles/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除充电桩（管理端）
 */
export function deleteChargingPile(id: number): Promise<void> {
  return request({
    url: `/admin/charging-piles/${id}`,
    method: 'delete'
  })
}

/**
 * 更新充电桩状态（管理端）
 */
export function updateChargingPileStatus(id: number, data: ChargingPileStatusUpdateRequest): Promise<ChargingPile> {
  return request({
    url: `/admin/charging-piles/${id}/status`,
    method: 'put',
    data
  })
}

/**
 * 批量删除充电桩（管理端）
 */
export function batchDeleteChargingPiles(data: BatchDeleteRequest): Promise<BatchDeleteResult> {
  return request({
    url: '/admin/charging-piles/batch',
    method: 'delete',
    data
  })
}

/**
 * 批量导入充电桩（管理端）
 */
export function importChargingPiles(file: File): Promise<ImportResult> {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/admin/charging-piles/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量导出充电桩（管理端）
 */
export function exportChargingPiles(params: {
  type?: 'SLOW' | 'FAST' | 'SUPER_FAST'
  status?: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'FAULT'
}): Promise<Blob> {
  return request({
    url: '/admin/charging-piles/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
