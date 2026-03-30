import request from '@/utils/request'
import type {
  AnnouncementClientInfo,
  AnnouncementClientListResponse
} from '@/types/announcement'

/**
 * 系统公告API
 */

// ==================== 车主端接口 ====================

// 查询公告列表（车主端）
export const getAnnouncementList = (params?: { page?: number; size?: number }) => {
  return request<AnnouncementClientListResponse>({
    url: '/announcement',
    method: 'get',
    params
  })
}

// 查询公告详情（车主端）
export const getAnnouncementDetail = (id: number) => {
  return request<AnnouncementClientInfo>({
    url: `/announcement/${id}`,
    method: 'get'
  })
}

// 查询最新公告（车主端）
export const getLatestAnnouncements = (limit: number = 3) => {
  return request<AnnouncementClientInfo[]>({
    url: '/announcement/latest',
    method: 'get',
    params: { limit }
  })
}
