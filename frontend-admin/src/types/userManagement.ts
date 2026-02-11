// 用户管理类型定义

export interface UserAdminResponse {
  id: number
  username: string
  email: string
  phone?: string
  realName?: string
  enabled: boolean
  createdTime: string
  updatedTime: string
  lastLoginTime?: string
}

export interface UserQueryParams {
  keyword?: string
  status?: number
  startDate?: string
  endDate?: string
  isActive?: boolean
  page?: number
  size?: number
}

export interface UserStatusUpdateRequest {
  enabled: boolean
}

export interface PasswordResetRequest {
  // 空对象，后端自动生成密码
}

export interface PasswordResetResponse {
  newPassword: string
  userId: number
  username: string
}

export interface BatchUserStatusUpdateRequest {
  userIds: number[]
  enabled: boolean
}

export interface BatchDeleteResultResponse {
  successCount: number
  failureCount: number
  failureReasons?: string[]
}

export interface ChargingRecordInfo {
  id: number
  chargingPileId: number
  pileName: string
  pileLocation: string
  startTime: string
  endTime?: string
  electricQuantity: number
  fee: number
  status: string
}

export interface ReservationInfo {
  id: number
  chargingPileId: number
  pileName: string
  pileLocation: string
  startTime: string
  endTime: string
  status: string
}

export interface ViolationInfo {
  id: number
  type: string
  description: string
  violationTime: string
  penaltyAmount?: number
}
