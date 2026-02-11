/**
 * 用户管理相关API接口
 */
import request from '@/utils/request'
import type {
  UserAdminResponse,
  UserQueryParams,
  UserStatusUpdateRequest,
  PasswordResetRequest,
  PasswordResetResponse,
  BatchStatusUpdateRequest,
  BatchOperationResult,
  PageResponse,
  ChargingRecord,
  ReservationRecord,
  ViolationPageResponse
} from '@/types/user'

/**
 * 查询用户列表（管理端）
 */
export function getAdminUserList(params: UserQueryParams): Promise<PageResponse<UserAdminResponse>> {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 查询用户详情（管理端）
 */
export function getAdminUserDetail(id: number): Promise<UserAdminResponse> {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  })
}

/**
 * 更新用户状态
 */
export function updateUserStatus(id: number, data: UserStatusUpdateRequest): Promise<UserAdminResponse> {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data
  })
}

/**
 * 重置用户密码
 */
export function resetUserPassword(id: number, data: PasswordResetRequest): Promise<PasswordResetResponse> {
  return request({
    url: `/admin/users/${id}/reset-password`,
    method: 'put',
    data
  })
}

/**
 * 查看用户充电记录
 */
export function getUserChargingRecords(
  id: number,
  params: {
    status?: string
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }
): Promise<PageResponse<ChargingRecord>> {
  return request({
    url: `/admin/users/${id}/charging-records`,
    method: 'get',
    params
  })
}

/**
 * 查看用户预约记录
 */
export function getUserReservations(
  id: number,
  params: {
    status?: string
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }
): Promise<PageResponse<ReservationRecord>> {
  return request({
    url: `/admin/users/${id}/reservations`,
    method: 'get',
    params
  })
}

/**
 * 查看用户违规记录
 */
export function getUserViolations(
  id: number,
  params: {
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }
): Promise<ViolationPageResponse> {
  return request({
    url: `/admin/users/${id}/violations`,
    method: 'get',
    params
  })
}

/**
 * 导出用户列表
 */
export function exportUsers(params: {
  status?: number
  isActive?: boolean
}): Promise<Blob> {
  return request({
    url: '/admin/users/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 批量更新用户状态
 */
export function batchUpdateUserStatus(data: BatchStatusUpdateRequest): Promise<BatchOperationResult> {
  return request({
    url: '/admin/users/batch-status',
    method: 'put',
    data
  })
}
