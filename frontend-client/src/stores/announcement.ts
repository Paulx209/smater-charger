import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAnnouncementList,
  getAnnouncementDetail,
  getLatestAnnouncements
} from '@/api/announcement'
import type { AnnouncementClientInfo } from '@/types/announcement'

export const useAnnouncementStore = defineStore('announcement', () => {
  const loading = ref(false)
  const clientAnnouncements = ref<AnnouncementClientInfo[]>([])
  const latestAnnouncements = ref<AnnouncementClientInfo[]>([])
  const currentAnnouncement = ref<AnnouncementClientInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  const fetchClientAnnouncementList = async (params?: { page?: number; size?: number }) => {
    try {
      loading.value = true
      const queryParams = {
        page: currentPage.value,
        size: pageSize.value,
        ...params
      }
      const result = await getAnnouncementList(queryParams)
      clientAnnouncements.value = result.content
      total.value = result.totalElements
      currentPage.value = result.number + 1
      pageSize.value = result.size
      return result
    } catch (error) {
      console.error('Failed to fetch client announcement list:', error)
      ElMessage.error('加载公告列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchClientAnnouncementDetail = async (id: number) => {
    try {
      loading.value = true
      const result = await getAnnouncementDetail(id)
      currentAnnouncement.value = result
      return result
    } catch (error) {
      console.error('Failed to fetch client announcement detail:', error)
      ElMessage.error('加载公告详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchLatestAnnouncements = async (limit: number = 3) => {
    try {
      const result = await getLatestAnnouncements(limit)
      latestAnnouncements.value = result
      return result
    } catch (error) {
      console.error('Failed to fetch latest announcements:', error)
      throw error
    }
  }

  const reset = () => {
    clientAnnouncements.value = []
    latestAnnouncements.value = []
    currentAnnouncement.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
  }

  return {
    loading,
    clientAnnouncements,
    latestAnnouncements,
    currentAnnouncement,
    total,
    currentPage,
    pageSize,
    fetchClientAnnouncementList,
    fetchClientAnnouncementDetail,
    fetchLatestAnnouncements,
    reset
  }
})