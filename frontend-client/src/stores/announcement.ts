import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAnnouncementList,
  getAnnouncementDetail,
  getLatestAnnouncements
} from '@/api/announcement'
import type {
  AnnouncementClientInfo,
} from '@/types/announcement'

export const useAnnouncementStore = defineStore('announcement', () => {
  // 状态
  const loading = ref(false)
  const clientAnnouncements = ref<AnnouncementClientInfo[]>([])
  const latestAnnouncements = ref<AnnouncementClientInfo[]>([])
  const currentAnnouncement = ref<AnnouncementClientInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  // ==================== 车主端方法 ====================

  // 获取车主端公告列表
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
      console.error('获取车主端公告列表失败:', error)
      ElMessage.error('获取公告列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取车主端公告详情
  const fetchClientAnnouncementDetail = async (id: number) => {
    try {
      loading.value = true
      const result = await getAnnouncementDetail(id)
      currentAnnouncement.value = result
      return result
    } catch (error) {
      console.error('获取车主端公告详情失败:', error)
      ElMessage.error('获取公告详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取最新公告
  const fetchLatestAnnouncements = async (limit: number = 3) => {
    try {
      const result = await getLatestAnnouncements(limit)
      latestAnnouncements.value = result
      return result
    } catch (error) {
      console.error('获取最新公告失败:', error)
      throw error
    }
  }

  // 重置状态
  const reset = () => {
    clientAnnouncements.value = []
    latestAnnouncements.value = []
    currentAnnouncement.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
  }

  return {
    // 状态
    loading,
    clientAnnouncements,
    latestAnnouncements,
    currentAnnouncement,
    total,
    currentPage,
    pageSize,

    // 车主端方法
    fetchClientAnnouncementList,
    fetchClientAnnouncementDetail,
    fetchLatestAnnouncements,

    // 工具方法
    reset
  }
})
