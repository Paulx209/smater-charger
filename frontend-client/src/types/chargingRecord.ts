// 充电记录状态枚举
export enum ChargingRecordStatus {
  CHARGING = 'CHARGING',    // 充电中
  COMPLETED = 'COMPLETED',  // 已完成
  CANCELLED = 'CANCELLED',  // 已取消
  OVERTIME = 'OVERTIME'     // 超时占位
}

// 充电记录状态文本映射
export const ChargingRecordStatusText: Record<ChargingRecordStatus, string> = {
  [ChargingRecordStatus.CHARGING]: '充电中',
  [ChargingRecordStatus.COMPLETED]: '已完成',
  [ChargingRecordStatus.CANCELLED]: '已取消',
  [ChargingRecordStatus.OVERTIME]: '超时占位'
}

// 充电记录状态颜色映射
export const ChargingRecordStatusColor: Record<ChargingRecordStatus, string> = {
  [ChargingRecordStatus.CHARGING]: 'warning',
  [ChargingRecordStatus.COMPLETED]: 'success',
  [ChargingRecordStatus.CANCELLED]: 'info',
  [ChargingRecordStatus.OVERTIME]: 'danger'
}

// 充电记录信息接口
export interface ChargingRecordInfo {
  id: number
  userId: number
  chargingPileId: number
  vehicleId?: number
  startTime: string
  endTime?: string
  duration?: number           // 充电时长（分钟）
  electricQuantity?: number   // 充电量（度）
  fee?: number                // 费用（元）
  status: ChargingRecordStatus
  createdTime: string
  updatedTime: string
  // 关联信息
  pileName?: string
  pileLocation?: string
  pileType?: string
  vehicleLicensePlate?: string
  pricePerKwh?: number
  serviceFee?: number
  feeBreakdown?: {
    electricityFee: number
    serviceFee: number
  }
}

// 开始充电请求
export interface ChargingRecordStartRequest {
  chargingPileId: number
  vehicleId?: number
}

// 结束充电请求
export interface ChargingRecordEndRequest {
  electricQuantity: number  // 充电量（度），>0
}

// 查询参数
export interface ChargingRecordQueryParams {
  status?: ChargingRecordStatus
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

// 充电记录列表响应
export interface ChargingRecordListResponse {
  content: ChargingRecordInfo[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 月度统计
export interface ChargingRecordStatisticsMonthly {
  year: number
  month: number
  totalCount: number
  totalElectricQuantity: number
  totalFee: number
  records: Array<{
    date: string
    count: number
    electricQuantity: number
    fee: number
  }>
}

// 年度统计
export interface ChargingRecordStatisticsYearly {
  year: number
  totalCount: number
  totalElectricQuantity: number
  totalFee: number
  records: Array<{
    month: number
    count: number
    electricQuantity: number
    fee: number
  }>
}

// 格式化充电时长（分钟 -> 小时分钟）
export function formatDuration(minutes?: number): string {
  if (!minutes) return '-'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}分钟`
  if (mins === 0) return `${hours}小时`
  return `${hours}小时${mins}分钟`
}

// 格式化充电量
export function formatElectricQuantity(quantity?: number): string {
  if (quantity === undefined || quantity === null) return '-'
  return `${quantity.toFixed(2)} 度`
}

// 格式化费用
export function formatFee(fee?: number): string {
  if (fee === undefined || fee === null) return '-'
  return `¥${fee.toFixed(2)}`
}

// 计算充电时长（开始时间到现在）
export function calculateChargingDuration(startTime: string): number {
  const start = new Date(startTime)
  const now = new Date()
  const diffMs = now.getTime() - start.getTime()
  return Math.floor(diffMs / (1000 * 60)) // 转换为分钟
}
