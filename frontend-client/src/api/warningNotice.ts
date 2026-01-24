import request from '@/utils/request'
import type {
  WarningNoticeInfo,
  WarningNoticeQueryParams,
  WarningNoticeListResponse,
  UnreadCountResponse,
  ThresholdConfig,
  SetThresholdRequest
} from '@/types/warningNotice'

/**
 * 查询预警通知列表
 */
export function getWarningNoticeList(
  params?: WarningNoticeQueryParams
): Promise<WarningNoticeListResponse> {
  return request.get('/warning-notice', { params })
}

/**
 * 查询未读通知数量
 */
export function getUnreadCount(): Promise<UnreadCountResponse> {
  return request.get('/warning-notice/unread-count')
}

/**
 * 标记通知为已读
 */
export function markAsRead(id: number): Promise<void> {
  return request.put(`/warning-notice/${id}/read`)
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead(): Promise<void> {
  return request.put('/warning-notice/read-all')
}

/**
 * 删除通知
 */
export function deleteWarningNotice(id: number): Promise<void> {
  return request.delete(`/warning-notice/${id}`)
}

/**
 * 获取预警阈值配置
 */
export function getThresholdConfig(): Promise<ThresholdConfig> {
  return request.get('/warning-notice/config/threshold')
}

/**
 * 设置预警阈值配置
 */
export function setThresholdConfig(data: SetThresholdRequest): Promise<ThresholdConfig> {
  return request.put('/warning-notice/config/threshold', data)
}
