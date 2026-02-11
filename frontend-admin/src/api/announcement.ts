import request from '@/utils/request'
import type {
  AnnouncementInfo,
  AnnouncementClientInfo,
  AnnouncementCreateRequest,
  AnnouncementUpdateRequest,
  AnnouncementQueryParams,
  AnnouncementListResponse,
  AnnouncementClientListResponse
} from '@/types/announcement'

/**
 * 系统公告API
 */

// ==================== 管理端接口 ====================

// 创建公告
export const createAnnouncement = (data: AnnouncementCreateRequest) => {
  return request<AnnouncementInfo>({
    url: '/api/announcement/admin',
    method: 'post',
    data
  })
}

// 更新公告
export const updateAnnouncement = (id: number, data: AnnouncementUpdateRequest) => {
  return request<AnnouncementInfo>({
    url: `/api/announcement/admin/${id}`,
    method: 'put',
    data
  })
}

// 删除公告
export const deleteAnnouncement = (id: number) => {
  return request<void>({
    url: `/api/announcement/admin/${id}`,
    method: 'delete'
  })
}

// 发布公告
export const publishAnnouncement = (id: number) => {
  return request<AnnouncementInfo>({
    url: `/api/announcement/admin/${id}/publish`,
    method: 'put'
  })
}

// 下线公告
export const unpublishAnnouncement = (id: number) => {
  return request<AnnouncementInfo>({
    url: `/api/announcement/admin/${id}/unpublish`,
    method: 'put'
  })
}

// 查询公告列表（管理端）
export const getAdminAnnouncementList = (params?: AnnouncementQueryParams) => {
  return request<AnnouncementListResponse>({
    url: '/api/announcement/admin',
    method: 'get',
    params
  })
}

// 查询公告详情（管理端）
export const getAdminAnnouncementDetail = (id: number) => {
  return request<AnnouncementInfo>({
    url: `/api/announcement/admin/${id}`,
    method: 'get'
  })
}

// ==================== 车主端接口 ====================

// 查询公告列表（车主端）
export const getAnnouncementList = (params?: { page?: number; size?: number }) => {
  return request<AnnouncementClientListResponse>({
    url: '/api/announcement',
    method: 'get',
    params
  })
}

// 查询公告详情（车主端）
export const getAnnouncementDetail = (id: number) => {
  return request<AnnouncementClientInfo>({
    url: `/api/announcement/${id}`,
    method: 'get'
  })
}

// 查询最新公告（车主端）
export const getLatestAnnouncements = (limit: number = 3) => {
  return request<AnnouncementClientInfo[]>({
    url: '/api/announcement/latest',
    method: 'get',
    params: { limit }
  })
}
