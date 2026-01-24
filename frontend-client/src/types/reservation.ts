// 预约状态枚举
export enum ReservationStatus {
  PENDING = 'PENDING',      // 待使用
  COMPLETED = 'COMPLETED',  // 已完成
  CANCELLED = 'CANCELLED',  // 已取消
  EXPIRED = 'EXPIRED'       // 已过期
}

// 预约状态文本映射
export const ReservationStatusText: Record<ReservationStatus, string> = {
  [ReservationStatus.PENDING]: '待使用',
  [ReservationStatus.COMPLETED]: '已完成',
  [ReservationStatus.CANCELLED]: '已取消',
  [ReservationStatus.EXPIRED]: '已过期'
}

// 预约状态颜色映射
export const ReservationStatusColor: Record<ReservationStatus, string> = {
  [ReservationStatus.PENDING]: 'warning',
  [ReservationStatus.COMPLETED]: 'success',
  [ReservationStatus.CANCELLED]: 'info',
  [ReservationStatus.EXPIRED]: 'danger'
}

// 预约信息接口
export interface ReservationInfo {
  id: number
  userId: number
  chargingPileId: number
  chargingPileCode?: string
  chargingPileLocation?: string
  chargingPileLng?: number
  chargingPileLat?: number
  chargingPileType?: string
  chargingPileTypeDesc?: string
  chargingPilePower?: number
  startTime: string
  endTime: string
  status: ReservationStatus
  statusDesc?: string
  remainingMinutes?: number
  createdTime: string
}

// 创建预约请求
export interface ReservationCreateRequest {
  chargingPileId: number
  startTime?: string
}

// 取消预约请求
export interface ReservationCancelRequest {
  cancelReason?: string
}

// 预约查询参数
export interface ReservationQueryParams {
  status?: ReservationStatus
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

// 预约列表响应
export interface ReservationListResponse {
  content: ReservationInfo[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 时间验证函数
export function validateReservationTime(startTime: string, endTime: string): boolean {
  const start = new Date(startTime)
  const end = new Date(endTime)
  const now = new Date()

  // 开始时间必须大于当前时间
  if (start <= now) {
    return false
  }

  // 结束时间必须大于开始时间
  if (end <= start) {
    return false
  }

  // 预约时长不能超过4小时
  const duration = (end.getTime() - start.getTime()) / (1000 * 60 * 60)
  if (duration > 4) {
    return false
  }

  return true
}

// 格式化预约时间范围
export function formatReservationTimeRange(startTime: string, endTime: string): string {
  const start = new Date(startTime)
  const end = new Date(endTime)

  const formatTime = (date: Date) => {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  }

  const formatDate = (date: Date) => {
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const startDate = formatDate(start)
  const endDate = formatDate(end)

  if (startDate === endDate) {
    return `${startDate} ${formatTime(start)} - ${formatTime(end)}`
  } else {
    return `${startDate} ${formatTime(start)} - ${endDate} ${formatTime(end)}`
  }
}

// 计算预约时长（小时）
export function calculateReservationDuration(startTime: string, endTime: string): number {
  const start = new Date(startTime)
  const end = new Date(endTime)
  const duration = (end.getTime() - start.getTime()) / (1000 * 60 * 60)
  return Math.round(duration * 10) / 10
}

// 判断预约是否可以取消
export function canCancelReservation(reservation: ReservationInfo): boolean {
  // 只有待使用的预约可以取消
  if (reservation.status !== ReservationStatus.PENDING) {
    return false
  }

  // 开始时间必须在未来
  const startTime = new Date(reservation.startTime)
  const now = new Date()

  return startTime > now
}
