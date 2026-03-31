export enum ReservationStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED'
}

export const ReservationStatusText: Record<ReservationStatus, string> = {
  [ReservationStatus.PENDING]: '待使用',
  [ReservationStatus.COMPLETED]: '已完成',
  [ReservationStatus.CANCELLED]: '已取消',
  [ReservationStatus.EXPIRED]: '已过期'
}

export interface ReservationInfo {
  id: number
  userId: number
  username?: string | null
  nickname?: string | null
  chargingPileId: number
  chargingPileCode?: string | null
  chargingPileLocation?: string | null
  chargingPileLng?: number | null
  chargingPileLat?: number | null
  chargingPileType?: string | null
  chargingPileTypeDesc?: string | null
  chargingPilePower?: number | null
  startTime: string
  endTime: string
  status: ReservationStatus
  statusDesc?: string | null
  remainingMinutes?: number | null
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

export interface ReservationQueryParams {
  userId?: number
  chargingPileId?: number
  status?: ReservationStatus
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export type ReservationPage = PageResponse<ReservationInfo>

export function getReservationStatusText(status: ReservationStatus | string | null | undefined): string {
  if (!status) return '-'
  return ReservationStatusText[status as ReservationStatus] ?? status
}

export function getReservationStatusTagType(status: ReservationStatus | string | null | undefined) {
  switch (status) {
    case ReservationStatus.PENDING:
      return 'warning'
    case ReservationStatus.COMPLETED:
      return 'success'
    case ReservationStatus.CANCELLED:
      return 'info'
    case ReservationStatus.EXPIRED:
      return 'danger'
    default:
      return 'info'
  }
}

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

export function formatRemainingMinutes(minutes: number | null | undefined): string {
  if (minutes == null) return '-'
  if (minutes <= 0) return '已到期'
  if (minutes < 60) return `${minutes} 分钟`
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return rest > 0 ? `${hours} 小时 ${rest} 分钟` : `${hours} 小时`
}

export function getReservationUserLabel(item: ReservationInfo): string {
  return item.nickname || item.username || `用户 #${item.userId}`
}