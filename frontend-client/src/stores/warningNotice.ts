import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  getWarningNoticeList,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteWarningNotice,
  getThresholdConfig,
  setThresholdConfig
} from '@/api/warningNotice'
import type {
  WarningNoticeInfo,
  WarningNoticeQueryParams,
  SetThresholdRequest
} from '@/types/warningNotice'

export const useWarningNoticeStore = defineStore('warningNotice', () => {
  const notices = ref<WarningNoticeInfo[]>([])
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const unreadCount = ref(0)
  const threshold = ref(30)

  const fetchNoticeList = async (params?: WarningNoticeQueryParams) => {
    try {
      loading.value = true
      const response = await getWarningNoticeList({
        ...params,
        page: params?.page ?? currentPage.value,
        size: params?.size ?? pageSize.value
      })
      notices.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      return response
    } catch (error) {
      console.error('Failed to fetch warning notices:', error)
      ElMessage.error('加载预警通知列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchUnreadCount = async () => {
    try {
      const response = await getUnreadCount()
      unreadCount.value = response.unreadCount
      return response.unreadCount
    } catch (error) {
      console.error('Failed to fetch unread warning notice count:', error)
      throw error
    }
  }

  const markNoticeAsRead = async (id: number) => {
    try {
      await markAsRead(id)

      const notice = notices.value.find((item) => item.id === id)
      if (notice && notice.isRead === 0) {
        notice.isRead = 1
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }

      return true
    } catch (error) {
      console.error('Failed to mark warning notice as read:', error)
      ElMessage.error('标记预警通知已读失败')
      throw error
    }
  }

  const markAllNoticesAsRead = async () => {
    try {
      await markAllAsRead()

      notices.value.forEach((notice) => {
        notice.isRead = 1
      })
      unreadCount.value = 0

      ElMessage.success('已全部标记为已读')
      return true
    } catch (error) {
      console.error('Failed to mark all warning notices as read:', error)
      ElMessage.error('全部标记已读失败')
      throw error
    }
  }

  const removeNotice = async (id: number) => {
    try {
      await deleteWarningNotice(id)

      const index = notices.value.findIndex((item) => item.id === id)
      if (index !== -1) {
        const notice = notices.value[index]
        if (notice?.isRead === 0) {
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
        notices.value.splice(index, 1)
        total.value = Math.max(0, total.value - 1)
      }

      ElMessage.success('删除预警通知成功')
      return true
    } catch (error) {
      console.error('Failed to remove warning notice:', error)
      ElMessage.error('删除预警通知失败')
      throw error
    }
  }

  const fetchThresholdConfig = async () => {
    try {
      const response = await getThresholdConfig()
      threshold.value = response.threshold
      return response.threshold
    } catch (error) {
      console.error('Failed to fetch warning threshold config:', error)
      throw error
    }
  }

  const updateThresholdConfig = async (data: SetThresholdRequest) => {
    try {
      const response = await setThresholdConfig(data)
      threshold.value = response.threshold
      ElMessage.success('更新预警阈值成功')
      return response.threshold
    } catch (error) {
      console.error('Failed to update warning threshold config:', error)
      ElMessage.error('更新预警阈值失败')
      throw error
    }
  }

  const resetPagination = () => {
    currentPage.value = 1
    total.value = 0
  }

  return {
    notices,
    loading,
    total,
    currentPage,
    pageSize,
    unreadCount,
    threshold,
    fetchNoticeList,
    fetchUnreadCount,
    markNoticeAsRead,
    markAllNoticesAsRead,
    removeNotice,
    fetchThresholdConfig,
    updateThresholdConfig,
    resetPagination
  }
})