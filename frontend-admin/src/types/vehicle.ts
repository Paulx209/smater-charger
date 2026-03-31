export interface VehicleInfo {
  id: number
  userId: number
  username?: string | null
  nickname?: string | null
  licensePlate: string
  brand?: string | null
  model?: string | null
  batteryCapacity?: number | null
  isDefault: number
  createdTime: string
  updatedTime?: string | null
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface VehicleQueryParams {
  userId?: number
  licensePlate?: string
  page?: number
  size?: number
}

export type VehiclePage = PageResponse<VehicleInfo>

export function formatDateTime(dateTime: string | null | undefined): string {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

export function getVehicleOwnerLabel(vehicle: VehicleInfo): string {
  return vehicle.nickname || vehicle.username || `用户 #${vehicle.userId}`
}

export function getDefaultStatusText(isDefault: number): string {
  return isDefault === 1 ? '是' : '否'
}
