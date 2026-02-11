/**
 * 系统公告类型定义
 */

// 公告状态
export enum AnnouncementStatus {
  DRAFT = 'DRAFT', // 草稿
  PUBLISHED = 'PUBLISHED' // 已发布
}

// 公告状态文本映射
export const AnnouncementStatusText: Record<AnnouncementStatus, string> = {
  [AnnouncementStatus.DRAFT]: '草稿',
  [AnnouncementStatus.PUBLISHED]: '已发布'
}

// 公告状态颜色映射
export const AnnouncementStatusColor: Record<AnnouncementStatus, 'info' | 'success'> = {
  [AnnouncementStatus.DRAFT]: 'info',
  [AnnouncementStatus.PUBLISHED]: 'success'
}

// 公告信息（管理端）
export interface AnnouncementInfo {
  id: number
  adminId: number
  adminName?: string
  title: string
  content: string
  status: AnnouncementStatus
  startTime?: string
  endTime?: string
  createdTime: string
  updatedTime: string
}

// 公告信息（车主端）
export interface AnnouncementClientInfo {
  id: number
  title: string
  content: string
  startTime?: string
  endTime?: string
  createdTime: string
}

// 公告创建请求
export interface AnnouncementCreateRequest {
  title: string
  content: string
  status?: AnnouncementStatus
  startTime?: string
  endTime?: string
}

// 公告更新请求
export interface AnnouncementUpdateRequest {
  title?: string
  content?: string
  startTime?: string
  endTime?: string
}

// 公告查询参数（管理端）
export interface AnnouncementQueryParams {
  status?: AnnouncementStatus
  keyword?: string
  page?: number
  size?: number
}

// 公告列表响应（管理端）
export interface AnnouncementListResponse {
  content: AnnouncementInfo[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 公告列表响应（车主端）
export interface AnnouncementClientListResponse {
  content: AnnouncementClientInfo[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 格式化时间范围
export const formatTimeRange = (startTime?: string, endTime?: string): string => {
  if (!startTime && !endTime) return '长期有效'

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  if (!startTime) return `至 ${formatDate(endTime!)}`
  if (!endTime) return `${formatDate(startTime)} 起`
  return `${formatDate(startTime)} 至 ${formatDate(endTime)}`
}

// 去除HTML标签并截取文本
export const stripHtmlAndTruncate = (html: string, maxLength: number = 100): string => {
  // 去除HTML标签
  const text = html.replace(/<[^>]*>/g, '')
  // 去除多余空白字符
  const cleanText = text.replace(/\s+/g, ' ').trim()
  // 截取指定长度
  if (cleanText.length <= maxLength) {
    return cleanText
  }
  return cleanText.substring(0, maxLength) + '...'
}
