/**
 * 用户管理相关类型定义
 */

// 用户状态枚举
export enum UserStatus {
  DISABLED = 0,
  ENABLED = 1
}

// 用户状态文本映射
export const UserStatusText: Record<UserStatus, string> = {
  [UserStatus.DISABLED]: '禁用',
  [UserStatus.ENABLED]: '启用'
}

// 用户状态颜色映射
export const UserStatusColor: Record<UserStatus, string> = {
  [UserStatus.DISABLED]: 'danger',
  [UserStatus.ENABLED]: 'success'
}

// 用户基本信息
export interface UserInfo {
  id: number
  username: string
  phone: string
  nickname: string
  name: string | null
  avatar: string | null
  warningThreshold: number | null
  status: UserStatus
  createdTime: string
  updatedTime: string
}

// 用户统计数据
export interface UserStatistics {
  vehicleCount: number
  chargingRecordCount: number
  totalElectricQuantity: number
  totalSpent: number
  avgChargingDuration?: number
  overtimeCount: number
  reservationCount?: number
  cancelledReservationCount?: number
  faultReportCount?: number
  lastLoginTime?: string | null
  lastChargingTime: string | null
}

// 用户车辆信息
export interface UserVehicle {
  id: number
  licensePlate: string
  brand: string
  model: string
  isDefault: boolean
}

// 充电记录信息
export interface ChargingRecord {
  id: number
  chargingPileId: number
  chargingPileCode: string
  chargingPileLocation: string
  vehicleLicensePlate: string
  startTime: string
  endTime: string | null
  duration: number | null
  electricQuantity: number
  fee: number
  status: string
  createdTime: string
}

// 预约记录信息
export interface ReservationRecord {
  id: number
  chargingPileId: number
  chargingPileCode: string
  chargingPileLocation: string
  reservationTime: string
  expirationTime: string
  status: string
  createdTime: string
}

// 违规记录信息
export interface ViolationRecord {
  id: number
  chargingRecordId: number
  chargingPileCode: string
  chargingPileLocation: string
  chargingEndTime: string
  overtimeMinutes: number
  warningTime: string
  violationType: string
  createdTime: string
}

// 违规记录汇总
export interface ViolationSummary {
  totalViolations: number
  totalOvertimeMinutes: number
}

// 用户管理响应（列表和详情）
export interface UserAdminResponse extends UserInfo {
  statistics: UserStatistics
  vehicles?: UserVehicle[]
  recentChargingRecords?: ChargingRecord[]
}

// 用户查询参数
export interface UserQueryParams {
  status?: UserStatus
  keyword?: string
  startDate?: string
  endDate?: string
  isActive?: boolean
  page?: number
  size?: number
}

// 更新用户状态请求
export interface UserStatusUpdateRequest {
  status: UserStatus
  reason?: string
}

// 重置密码请求
export interface PasswordResetRequest {
  newPassword?: string
}

// 重置密码响应
export interface PasswordResetResponse {
  id: number
  username: string
  newPassword: string
  message: string
}

// 批量更新状态请求
export interface BatchStatusUpdateRequest {
  userIds: number[]
  status: UserStatus
  reason?: string
}

// 批量操作结果
export interface BatchOperationResult {
  totalCount: number
  successCount: number
  failCount: number
  failedRecords: Array<{
    id: number
    reason: string
  }>
}

// 分页响应
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 违规记录分页响应（包含汇总）
export interface ViolationPageResponse {
  summary: ViolationSummary
  content: ViolationRecord[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 格式化用户状态文本
export function getUserStatusText(status: UserStatus): string {
  return UserStatusText[status]
}

// 格式化用户状态颜色
export function getUserStatusColor(status: UserStatus): string {
  return UserStatusColor[status]
}

// 格式化金额（保留2位小数）
export function formatMoney(amount: number): string {
  return `¥${amount.toFixed(2)}`
}

// 格式化电量（保留2位小数）
export function formatElectricity(quantity: number): string {
  return `${quantity.toFixed(2)} 度`
}

// 格式化时长（分钟转小时分钟）
export function formatDuration(minutes: number): string {
  if (minutes < 60) {
    return `${minutes} 分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours} 小时 ${mins} 分钟` : `${hours} 小时`
}

// 格式化日期时间
export function formatDateTime(dateTime: string | null): string {
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

// 格式化日期
export function formatDate(date: string | null): string {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
