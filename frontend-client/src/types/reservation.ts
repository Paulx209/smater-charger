// 预约状态枚举
export enum ReservationStatus {
  PENDING = 0,      // 待确认
  CONFIRMED = 1,    // 已确认
  CANCELLED = 2,    // 已取消
  COMPLETED = 3,    // 已完成
  EXPIRED = 4       // 已过期
}

// 预约状态文本映射
export const ReservationStatusText: Record<ReservationStatus, string> = {
  [ReservationStatus.PENDING]: '待确认',
  [ReservationStatus.CONFIRMED]: '已确认',
  [ReservationStatus.CANCELLED]: '已取消',
  [ReservationStatus.COMPLETED]: '已完成',
  [ReservationStatus.EXPIRED]: '已过期'
}

// 预约状态颜色映射
export const ReservationStatusColor: Record<ReservationStatus, string> = {
  [ReservationStatus.PENDING]: 'warning',
  [ReservationStatus.CONFIRMED]: 'success',
  [ReservationStatus.CANCELLED]: 'info',
  [ReservationStatus.COMPLETED]: 'primary',
  [ReservationStatus.EXPIRED]: 'danger'
}

// 预约信息接口
export interface ReservationInfo {
  id: number
  userId: number
  pileId: number
  vehicleId: number
  startTime: string
  endTime: string
  status: ReservationStatus
  cancelReason?: string
  createdTime: string
  updatedTime: string
  // 关联信息
  pileName?: string
  pileLocation?: string
  vehicleLicensePlate?: string
}

// 创建预约请求
export interface ReservationCreateRequest {
  pileId: number
  vehicleId: number
  startTime: string
  endTime: string
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
  // 只有待确认和已确认的预约可以取消
  if (reservation.status !== ReservationStatus.PENDING &&
      reservation.status !== ReservationStatus.CONFIRMED) {
    return false
  }

  // 开始时间必须在未来
  const startTime = new Date(reservation.startTime)
  const now = new Date()

  return startTime > now
}
