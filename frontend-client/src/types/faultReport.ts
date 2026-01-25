/**
 * 故障报修类型定义
 */

// 故障类型
export enum FaultType {
  CANNOT_CHARGE = 'CANNOT_CHARGE', // 无法充电
  SLOW_CHARGING = 'SLOW_CHARGING', // 充电速度慢
  DISPLAY_ERROR = 'DISPLAY_ERROR', // 显示异常
  CONNECTOR_DAMAGED = 'CONNECTOR_DAMAGED', // 接口损坏
  PAYMENT_FAILED = 'PAYMENT_FAILED', // 支付失败
  OTHER = 'OTHER' // 其他
}

// 故障类型文本映射
export const FaultTypeText: Record<FaultType, string> = {
  [FaultType.CANNOT_CHARGE]: '无法充电',
  [FaultType.SLOW_CHARGING]: '充电速度慢',
  [FaultType.DISPLAY_ERROR]: '显示异常',
  [FaultType.CONNECTOR_DAMAGED]: '接口损坏',
  [FaultType.PAYMENT_FAILED]: '支付失败',
  [FaultType.OTHER]: '其他'
}

// 故障类型颜色映射
export const FaultTypeColor: Record<FaultType, 'danger' | 'warning' | 'info'> = {
  [FaultType.CANNOT_CHARGE]: 'danger',
  [FaultType.SLOW_CHARGING]: 'warning',
  [FaultType.DISPLAY_ERROR]: 'warning',
  [FaultType.CONNECTOR_DAMAGED]: 'danger',
  [FaultType.PAYMENT_FAILED]: 'warning',
  [FaultType.OTHER]: 'info'
}

// 故障报修状态
export enum FaultReportStatus {
  PENDING = 'PENDING', // 待处理
  PROCESSING = 'PROCESSING', // 处理中
  RESOLVED = 'RESOLVED', // 已解决
  CLOSED = 'CLOSED' // 已关闭
}

// 故障报修状态文本映射
export const FaultReportStatusText: Record<FaultReportStatus, string> = {
  [FaultReportStatus.PENDING]: '待处理',
  [FaultReportStatus.PROCESSING]: '处理中',
  [FaultReportStatus.RESOLVED]: '已解决',
  [FaultReportStatus.CLOSED]: '已关闭'
}

// 故障报修状态颜色映射
export const FaultReportStatusColor: Record<FaultReportStatus, 'info' | 'warning' | 'success' | ''> = {
  [FaultReportStatus.PENDING]: 'info',
  [FaultReportStatus.PROCESSING]: 'warning',
  [FaultReportStatus.RESOLVED]: 'success',
  [FaultReportStatus.CLOSED]: ''
}

// 故障报修信息
export interface FaultReportInfo {
  id: number
  userId: number
  username?: string
  pileId: number
  pileName: string
  pileLocation?: string
  faultType: FaultType
  description: string
  images?: string[] // 故障图片URL列表
  status: FaultReportStatus
  reportTime: string
  processTime?: string
  resolveTime?: string
  closeTime?: string
  processorId?: number
  processorName?: string
  processNote?: string
  resolveNote?: string
}

// 故障报修创建请求
export interface FaultReportCreateRequest {
  pileId: number
  faultType: FaultType
  description: string
  images?: string[]
}

// 故障报修更新请求
export interface FaultReportUpdateRequest {
  status?: FaultReportStatus
  processNote?: string
  resolveNote?: string
}

// 故障报修查询参数
export interface FaultReportQueryParams {
  pileId?: number
  faultType?: FaultType
  status?: FaultReportStatus
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

// 故障报修列表响应
export interface FaultReportListResponse {
  records: FaultReportInfo[]
  total: number
  current: number
  size: number
}

// 格式化相对时间
export const formatRelativeTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}
