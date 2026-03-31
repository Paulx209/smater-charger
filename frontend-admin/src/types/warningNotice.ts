export enum WarningNoticeType {
  IDLE_REMINDER = 'IDLE_REMINDER',
  OVERTIME_WARNING = 'OVERTIME_WARNING',
  FAULT_NOTICE = 'FAULT_NOTICE',
  RESERVATION_REMINDER = 'RESERVATION_REMINDER'
}

export enum SendStatus {
  PENDING = 'PENDING',
  SENT = 'SENT',
  FAILED = 'FAILED'
}

export const WarningNoticeTypeText: Record<WarningNoticeType, string> = {
  [WarningNoticeType.IDLE_REMINDER]: '空闲提醒',
  [WarningNoticeType.OVERTIME_WARNING]: '超时预警',
  [WarningNoticeType.FAULT_NOTICE]: '故障通知',
  [WarningNoticeType.RESERVATION_REMINDER]: '预约提醒'
}

export const WarningNoticeTypeTagType: Record<WarningNoticeType, 'info' | 'warning' | 'danger' | 'success'> = {
  [WarningNoticeType.IDLE_REMINDER]: 'info',
  [WarningNoticeType.OVERTIME_WARNING]: 'warning',
  [WarningNoticeType.FAULT_NOTICE]: 'danger',
  [WarningNoticeType.RESERVATION_REMINDER]: 'success'
}

export const SendStatusText: Record<SendStatus, string> = {
  [SendStatus.PENDING]: '待发送',
  [SendStatus.SENT]: '已发送',
  [SendStatus.FAILED]: '发送失败'
}

export const SendStatusTagType: Record<SendStatus, 'info' | 'success' | 'danger'> = {
  [SendStatus.PENDING]: 'info',
  [SendStatus.SENT]: 'success',
  [SendStatus.FAILED]: 'danger'
}

export interface WarningNoticeInfo {
  id: number
  userId: number
  username?: string | null
  nickname?: string | null
  chargingPileId?: number | null
  pileName?: string | null
  pileLocation?: string | null
  chargingRecordId?: number | null
  type: WarningNoticeType
  typeDesc?: string | null
  content: string
  overtimeMinutes?: number | null
  isRead: number
  sendStatus: SendStatus
  sendStatusDesc?: string | null
  createdTime: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface WarningNoticeQueryParams {
  type?: WarningNoticeType
  isRead?: number
  userId?: number
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export interface AdminWarningNoticeReadRequest {
  ids: number[]
}

export interface ThresholdConfig {
  threshold: number
}

export interface SetThresholdRequest {
  threshold: number
}

export type WarningNoticePage = PageResponse<WarningNoticeInfo>

export function getWarningNoticeUserLabel(item: WarningNoticeInfo): string {
  return item.nickname || item.username || `用户 #${item.userId}`
}

export function getWarningNoticeTypeText(type: WarningNoticeType | string | null | undefined): string {
  if (!type) return '-'
  return WarningNoticeTypeText[type as WarningNoticeType] ?? type
}

export function getWarningNoticeTypeTagType(type: WarningNoticeType | string | null | undefined) {
  if (!type) return 'info'
  return WarningNoticeTypeTagType[type as WarningNoticeType] ?? 'info'
}

export function getSendStatusText(status: SendStatus | string | null | undefined): string {
  if (!status) return '-'
  return SendStatusText[status as SendStatus] ?? status
}

export function getSendStatusTagType(status: SendStatus | string | null | undefined) {
  if (!status) return 'info'
  return SendStatusTagType[status as SendStatus] ?? 'info'
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

export function formatReadStatus(isRead: number | null | undefined): string {
  if (isRead === 1) return '已读'
  if (isRead === 0) return '未读'
  return '-'
}

export function getReadStatusTagType(isRead: number | null | undefined) {
  return isRead === 1 ? 'success' : 'warning'
}