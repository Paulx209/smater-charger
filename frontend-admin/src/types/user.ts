export enum UserStatus {
  DISABLED = 0,
  ENABLED = 1
}

export const UserStatusText: Record<UserStatus, string> = {
  [UserStatus.DISABLED]: '禁用',
  [UserStatus.ENABLED]: '启用'
}

export const UserStatusColor: Record<UserStatus, string> = {
  [UserStatus.DISABLED]: 'danger',
  [UserStatus.ENABLED]: 'success'
}

export interface UserInfo {
  id: number
  username: string
  phone: string | null
  nickname: string | null
  name: string | null
  avatar: string | null
  warningThreshold: number | null
  status: UserStatus
  createdTime: string
  updatedTime: string
}

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

export interface UserVehicle {
  id: number
  licensePlate: string
  brand: string
  model: string
  batteryCapacity?: number | null
  isDefault: boolean
  createdTime?: string
  updatedTime?: string
}

export interface ChargingRecord {
  id: number
  userId?: number
  chargingPileId: number
  chargingPileCode: string
  chargingPileLocation: string
  pileName?: string
  pileLocation?: string
  pileType?: string
  vehicleId?: number
  vehicleLicensePlate: string
  startTime: string
  endTime: string | null
  duration: number | null
  electricQuantity: number
  fee: number
  status: string
  statusDesc?: string
  pricePerKwh?: number
  serviceFee?: number
  createdTime: string
  updatedTime?: string | null
}

export interface ReservationRecord {
  id: number
  userId?: number
  chargingPileId: number
  chargingPileCode: string
  chargingPileLocation: string
  chargingPileLng?: number
  chargingPileLat?: number
  chargingPileType?: string
  chargingPileTypeDesc?: string
  chargingPilePower?: number
  startTime: string
  endTime: string
  status: string
  statusDesc?: string
  remainingMinutes?: number | null
  createdTime: string
}

export interface ViolationRecord {
  id: number
  chargingRecordId: number
  chargingPileCode: string
  chargingPileLocation: string
  chargingEndTime: string | null
  overtimeMinutes: number
  warningTime: string
  violationType: string
  createdTime: string
}

export interface ViolationSummary {
  totalViolations: number
  totalOvertimeMinutes: number
}

export interface UserAdminResponse extends UserInfo {
  statistics: UserStatistics
  vehicles?: UserVehicle[]
  recentChargingRecords?: ChargingRecord[]
}

export interface UserQueryParams {
  status?: UserStatus
  keyword?: string
  startDate?: string
  endDate?: string
  isActive?: boolean
  page?: number
  size?: number
}

export interface UserStatusUpdateRequest {
  status: UserStatus
  reason?: string
}

export interface PasswordResetRequest {
  newPassword?: string
}

export interface PasswordResetResponse {
  id: number
  username: string
  newPassword: string
  message: string
}

export interface BatchStatusUpdateRequest {
  userIds: number[]
  status: UserStatus
  reason?: string
}

export interface BatchOperationResult {
  totalCount: number
  successCount: number
  failCount: number
  failedRecords: Array<{
    id: number
    reason: string
  }>
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ViolationPageResponse {
  summary?: ViolationSummary
  content: ViolationRecord[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function getUserStatusText(status: UserStatus): string {
  return UserStatusText[status]
}

export function getUserStatusColor(status: UserStatus): string {
  return UserStatusColor[status]
}

export function isUserEnabled(status: UserStatus | number | null | undefined): boolean {
  return status === UserStatus.ENABLED
}

export function formatMoney(amount: number): string {
  return `¥${amount.toFixed(2)}`
}

export function formatElectricity(quantity: number): string {
  return `${quantity.toFixed(2)} kWh`
}

export function formatDuration(minutes: number | null | undefined): string {
  if (minutes == null) return '-'
  if (minutes < 60) {
    return `${minutes} 分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours} 小时 ${mins} 分钟` : `${hours} 小时`
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

export function formatDate(date: string | null | undefined): string {
  if (!date) return '-'
  const value = new Date(date)
  return value.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}