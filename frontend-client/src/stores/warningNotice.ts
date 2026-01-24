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
  // 状态
  const notices = ref<WarningNoticeInfo[]>([])
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const unreadCount = ref(0)
  const threshold = ref(30) // 默认30分钟

  /**
   * 查询预警通知列表
   */
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
      console.error('查询预警通知列表失败:', error)
      ElMessage.error('查询预警通知列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 查询未读通知数量
   */
  const fetchUnreadCount = async () => {
    try {
      const response = await getUnreadCount()
      unreadCount.value = response.unreadCount
      return response.unreadCount
    } catch (error) {
      console.error('查询未读通知数量失败:', error)
      throw error
    }
  }

  /**
   * 标记通知为已读
   */
  const markNoticeAsRead = async (id: number) => {
    try {
      await markAsRead(id)

      // 更新本地状态
      const notice = notices.value.find(n => n.id === id)
      if (notice && notice.isRead === 0) {
        notice.isRead = 1
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }

      return true
    } catch (error) {
      console.error('标记通知为已读失败:', error)
      ElMessage.error('标记通知为已读失败')
      throw error
    }
  }

  /**
   * 标记所有通知为已读
   */
  const markAllNoticesAsRead = async () => {
    try {
      await markAllAsRead()

      // 更新本地状态
      notices.value.forEach(notice => {
        notice.isRead = 1
      })
      unreadCount.value = 0

      ElMessage.success('已全部标记为已读')
      return true
    } catch (error) {
      console.error('标记所有通知为已读失败:', error)
      ElMessage.error('标记所有通知为已读失败')
      throw error
    }
  }

  /**
   * 删除通知
   */
  const removeNotice = async (id: number) => {
    try {
      await deleteWarningNotice(id)

      // 更新本地状态
      const index = notices.value.findIndex(n => n.id === id)
      if (index !== -1) {
        const notice = notices.value[index]
        if (notice.isRead === 0) {
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
        notices.value.splice(index, 1)
        total.value = Math.max(0, total.value - 1)
      }

      ElMessage.success('删除成功')
      return true
    } catch (error) {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
      throw error
    }
  }

  /**
   * 获取预警阈值配置
   */
  const fetchThresholdConfig = async () => {
    try {
      const response = await getThresholdConfig()
      threshold.value = response.threshold
      return response.threshold
    } catch (error) {
      console.error('获取预警阈值配置失败:', error)
      throw error
    }
  }

  /**
   * 设置预警阈值配置
   */
  const updateThresholdConfig = async (data: SetThresholdRequest) => {
    try {
      const response = await setThresholdConfig(data)
      threshold.value = response.threshold
      ElMessage.success('设置成功')
      return response.threshold
    } catch (error) {
      console.error('设置预警阈值配置失败:', error)
      ElMessage.error('设置预警阈值配置失败')
      throw error
    }
  }

  /**
   * 重置分页
   */
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
