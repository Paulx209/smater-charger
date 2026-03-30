import type { Page } from '@/types/common'

export type ChargingRecordStatus = 'CHARGING' | 'COMPLETED' | 'CANCELLED' | 'OVERTIME'
export type ChargingRecordTagType = '' | 'success' | 'warning' | 'info' | 'danger'

export const ChargingRecordStatusText: Record<ChargingRecordStatus, string> = {
  CHARGING: '充电中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  OVERTIME: '超时未驶离'
}

export const ChargingRecordStatusTagType: Record<ChargingRecordStatus, ChargingRecordTagType> = {
  CHARGING: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'info',
  OVERTIME: 'danger'
}

export interface ChargingRecordFeeBreakdown {
  electricityFee: number
  serviceFee: number
}

export interface ChargingRecordInfo {
  id: number
  userId: number
  chargingPileId: number
  chargingPileCode?: string | null
  chargingPileLocation?: string | null
  pileName?: string | null
  pileLocation?: string | null
  pileType?: string | null
  vehicleId?: number | null
  vehicleLicensePlate?: string | null
  startTime: string
  endTime?: string | null
  duration?: number | null
  electricQuantity?: number | null
  fee?: number | null
  status: ChargingRecordStatus
  statusDesc?: string | null
  pricePerKwh?: number | null
  serviceFee?: number | null
  feeBreakdown?: ChargingRecordFeeBreakdown | null
  createdTime: string
  updatedTime: string
}

export interface ChargingRecordQueryParams {
  userId?: number
  chargingPileId?: number
  status?: ChargingRecordStatus
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export type ChargingRecordPage = Page<ChargingRecordInfo>

export const formatDateTime = (value?: string | null): string => {
  if (!value) return '-'
  const date = new Date(value)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

export const formatMoney = (value?: number | null): string => {
  if (value === null || value === undefined) return '-'
  return `￥${Number(value).toFixed(2)}`
}

export const formatElectricity = (value?: number | null): string => {
  if (value === null || value === undefined) return '-'
  return `${Number(value).toFixed(2)} kWh`
}

export const formatDuration = (value?: number | null): string => {
  if (value === null || value === undefined) return '-'
  if (value < 60) return `${value} 分钟`
  const hours = Math.floor(value / 60)
  const minutes = value % 60
  return minutes > 0 ? `${hours} 小时 ${minutes} 分钟` : `${hours} 小时`
}

export const getChargingRecordStatusText = (status?: string | null): string => {
  if (!status) return '-'
  return ChargingRecordStatusText[status as ChargingRecordStatus] ?? status
}

export const getChargingRecordStatusTagType = (status?: string | null): ChargingRecordTagType => {
  if (!status) return 'info'
  return ChargingRecordStatusTagType[status as ChargingRecordStatus] ?? 'info'
}