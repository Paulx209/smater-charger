import request from '@/utils/request'
import type {
  AdminWarningNoticeReadRequest,
  SetThresholdRequest,
  ThresholdConfig,
  WarningNoticeInfo,
  WarningNoticePage,
  WarningNoticeQueryParams
} from '@/types/warningNotice'

export function getAdminWarningNoticeList(params?: WarningNoticeQueryParams): Promise<WarningNoticePage> {
  return request.get('/admin/warning-notices', { params })
}

export function getAdminWarningNoticeDetail(id: number): Promise<WarningNoticeInfo> {
  return request.get(`/admin/warning-notices/${id}`)
}

export function markAdminWarningsAsRead(data: AdminWarningNoticeReadRequest): Promise<void> {
  return request.put('/admin/warning-notices/read', data)
}

export function deleteAdminWarningNotice(id: number): Promise<void> {
  return request.delete(`/admin/warning-notices/${id}`)
}

export function getAdminThresholdConfig(): Promise<ThresholdConfig> {
  return request.get('/admin/warning-notices/config/threshold')
}

export function setAdminThresholdConfig(data: SetThresholdRequest): Promise<ThresholdConfig> {
  return request.put('/admin/warning-notices/config/threshold', data)
}