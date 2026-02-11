import request from '@/utils/request'
import type {
  UserAdminResponse,
  UserQueryParams,
  UserStatusUpdateRequest,
  PasswordResetRequest,
  PasswordResetResponse,
  BatchUserStatusUpdateRequest,
  BatchDeleteResultResponse,
  ChargingRecordInfo,
  ReservationInfo,
  ViolationInfo
} from '@/types/userManagement'
import type { Page } from '@/types/common'

/**
 * 获取用户列表
 */
export function getUserList(params: UserQueryParams) {
  return request.get<Page<UserAdminResponse>>('/admin/users', { params })
}

/**
 * 获取用户详情
 */
export function getUserDetail(id: number) {
  return request.get<UserAdminResponse>(`/admin/users/${id}`)
}

/**
 * 更新用户状态
 */
export function updateUserStatus(id: number, data: UserStatusUpdateRequest) {
  return request.put<UserAdminResponse>(`/admin/users/${id}/status`, data)
}

/**
 * 重置用户密码
 */
export function resetPassword(id: number, data: PasswordResetRequest) {
  return request.put<PasswordResetResponse>(`/admin/users/${id}/reset-password`, data)
}

/**
 * 获取用户充电记录
 */
export function getUserChargingRecords(
  userId: number,
  params?: {
    status?: string
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }
) {
  return request.get<Page<ChargingRecordInfo>>(`/admin/users/${userId}/charging-records`, {
    params
  })
}

/**
 * 获取用户预约记录
 */
export function getUserReservations(
  userId: number,
  params?: {
    status?: string
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }
) {
  return request.get<Page<ReservationInfo>>(`/admin/users/${userId}/reservations`, { params })
}

/**
 * 获取用户违规记录
 */
export function getUserViolations(
  userId: number,
  params?: {
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }
) {
  return request.get<Page<ViolationInfo>>(`/admin/users/${userId}/violations`, { params })
}

/**
 * 批量更新用户状态
 */
export function batchUpdateUserStatus(data: BatchUserStatusUpdateRequest) {
  return request.put<BatchDeleteResultResponse>('/admin/users/batch-status', data)
}

/**
 * 导出用户列表
 */
export function exportUsers(params?: { status?: number; isActive?: boolean }) {
  return request.get('/admin/users/export', {
    params,
    responseType: 'blob'
  })
}
