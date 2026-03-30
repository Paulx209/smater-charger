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

export const createAnnouncement = (data: AnnouncementCreateRequest): Promise<AnnouncementInfo> => {
  return request({
    url: '/announcement/admin',
    method: 'post',
    data
  }) as Promise<AnnouncementInfo>
}

export const updateAnnouncement = (id: number, data: AnnouncementUpdateRequest): Promise<AnnouncementInfo> => {
  return request({
    url: `/announcement/admin/${id}`,
    method: 'put',
    data
  }) as Promise<AnnouncementInfo>
}

export const deleteAnnouncement = (id: number): Promise<void> => {
  return request({
    url: `/announcement/admin/${id}`,
    method: 'delete'
  }) as Promise<void>
}

export const publishAnnouncement = (id: number): Promise<AnnouncementInfo> => {
  return request({
    url: `/announcement/admin/${id}/publish`,
    method: 'put'
  }) as Promise<AnnouncementInfo>
}

export const unpublishAnnouncement = (id: number): Promise<AnnouncementInfo> => {
  return request({
    url: `/announcement/admin/${id}/unpublish`,
    method: 'put'
  }) as Promise<AnnouncementInfo>
}

export const getAdminAnnouncementList = (params?: AnnouncementQueryParams): Promise<AnnouncementListResponse> => {
  return request({
    url: '/announcement/admin',
    method: 'get',
    params
  }) as Promise<AnnouncementListResponse>
}

export const getAdminAnnouncementDetail = (id: number): Promise<AnnouncementInfo> => {
  return request({
    url: `/announcement/admin/${id}`,
    method: 'get'
  }) as Promise<AnnouncementInfo>
}

export const getAnnouncementList = (params?: { page?: number; size?: number }): Promise<AnnouncementClientListResponse> => {
  return request({
    url: '/announcement',
    method: 'get',
    params
  }) as Promise<AnnouncementClientListResponse>
}

export const getAnnouncementDetail = (id: number): Promise<AnnouncementClientInfo> => {
  return request({
    url: `/announcement/${id}`,
    method: 'get'
  }) as Promise<AnnouncementClientInfo>
}

export const getLatestAnnouncements = (limit: number = 3): Promise<AnnouncementClientInfo[]> => {
  return request({
    url: '/announcement/latest',
    method: 'get',
    params: { limit }
  }) as Promise<AnnouncementClientInfo[]>
}