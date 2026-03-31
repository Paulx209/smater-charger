import request from '@/utils/request'
import type {
  AnnouncementClientInfo,
  AnnouncementClientListResponse
} from '@/types/announcement'

/**
 * 客户端公告 API
 */

export const getAnnouncementList = (params?: { page?: number; size?: number }) => {
  return request.get<AnnouncementClientListResponse, AnnouncementClientListResponse>('/announcement', { params })
}

export const getAnnouncementDetail = (id: number) => {
  return request.get<AnnouncementClientInfo, AnnouncementClientInfo>(`/announcement/${id}`)
}

export const getLatestAnnouncements = (limit: number = 3) => {
  return request.get<AnnouncementClientInfo[], AnnouncementClientInfo[]>('/announcement/latest', {
    params: { limit }
  })
}