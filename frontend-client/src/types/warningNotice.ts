// 预警通知类型枚举
export enum WarningNoticeType {
  IDLE_REMINDER = 'IDLE_REMINDER',           // 空闲提醒
  OVERTIME_WARNING = 'OVERTIME_WARNING',     // 超时占位预警
  FAULT_NOTICE = 'FAULT_NOTICE',             // 故障通知
  RESERVATION_REMINDER = 'RESERVATION_REMINDER' // 预约提醒
}

// 预警通知类型文本映射
export const WarningNoticeTypeText: Record<WarningNoticeType, string> = {
  [WarningNoticeType.IDLE_REMINDER]: '空闲提醒',
  [WarningNoticeType.OVERTIME_WARNING]: '超时占位预警',
  [WarningNoticeType.FAULT_NOTICE]: '故障通知',
  [WarningNoticeType.RESERVATION_REMINDER]: '预约提醒'
}

// 预警通知类型颜色映射
export const WarningNoticeTypeColor: Record<WarningNoticeType, string> = {
  [WarningNoticeType.IDLE_REMINDER]: 'info',
  [WarningNoticeType.OVERTIME_WARNING]: 'warning',
  [WarningNoticeType.FAULT_NOTICE]: 'danger',
  [WarningNoticeType.RESERVATION_REMINDER]: 'success'
}

// 预警通知类型图标映射
export const WarningNoticeTypeIcon: Record<WarningNoticeType, string> = {
  [WarningNoticeType.IDLE_REMINDER]: 'InfoFilled',
  [WarningNoticeType.OVERTIME_WARNING]: 'Warning',
  [WarningNoticeType.FAULT_NOTICE]: 'CircleClose',
  [WarningNoticeType.RESERVATION_REMINDER]: 'Bell'
}

// 发送状态枚举
export enum SendStatus {
  PENDING = 'PENDING',   // 待发送
  SENT = 'SENT',         // 已发送
  FAILED = 'FAILED'      // 发送失败
}

// 发送状态���本映射
export const SendStatusText: Record<SendStatus, string> = {
  [SendStatus.PENDING]: '待发送',
  [SendStatus.SENT]: '已发送',
  [SendStatus.FAILED]: '发送失败'
}

// 预警通知信息接口
export interface WarningNoticeInfo {
  id: number
  userId: number
  chargingPileId?: number
  chargingRecordId?: number
  type: WarningNoticeType
  content: string
  isRead: number              // 0=未读，1=已读
  sendStatus: SendStatus
  createdTime: string
  // 关联信息
  pileName?: string
  pileLocation?: string
}

// 查询参数
export interface WarningNoticeQueryParams {
  type?: WarningNoticeType
  isRead?: number
  page?: number
  size?: number
}

// 预警通知列表响应
export interface WarningNoticeListResponse {
  content: WarningNoticeInfo[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 未读数量响应
export interface UnreadCountResponse {
  unreadCount: number
}

// 阈值配置
export interface ThresholdConfig {
  threshold: number  // 预警阈值（分钟）
}

// 设置阈值请求
export interface SetThresholdRequest {
  threshold: number  // 10-60之间的整数
}

// 格式化时间（相对时间）
export function formatRelativeTime(dateTime: string): string {
  const now = new Date()
  const time = new Date(dateTime)
  const diffMs = now.getTime() - time.getTime()
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`

  // 超过7天显示具体日期
  const year = time.getFullYear()
  const month = (time.getMonth() + 1).toString().padStart(2, '0')
  const day = time.getDate().toString().padStart(2, '0')
  const hours = time.getHours().toString().padStart(2, '0')
  const minutes = time.getMinutes().toString().padStart(2, '0')

  // 如果是今年，不显示年份
  if (year === now.getFullYear()) {
    return `${month}-${day} ${hours}:${minutes}`
  }

  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化完整时间
export function formatFullTime(dateTime: string): string {
  const time = new Date(dateTime)
  const year = time.getFullYear()
  const month = (time.getMonth() + 1).toString().padStart(2, '0')
  const day = time.getDate().toString().padStart(2, '0')
  const hours = time.getHours().toString().padStart(2, '0')
  const minutes = time.getMinutes().toString().padStart(2, '0')
  const seconds = time.getSeconds().toString().padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
