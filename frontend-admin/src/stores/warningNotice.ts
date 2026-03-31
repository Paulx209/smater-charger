import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  deleteAdminWarningNotice,
  getAdminThresholdConfig,
  getAdminWarningNoticeDetail,
  getAdminWarningNoticeList,
  markAdminWarningsAsRead,
  setAdminThresholdConfig
} from '@/api/warningNotice'
import type { SetThresholdRequest, WarningNoticeInfo, WarningNoticeQueryParams } from '@/types/warningNotice'

export const useWarningNoticeStore = defineStore('warningNoticeAdmin', () => {
  const loading = ref(false)
  const notices = ref<WarningNoticeInfo[]>([])
  const currentNotice = ref<WarningNoticeInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const threshold = ref(30)
  const lastQuery = ref<Omit<WarningNoticeQueryParams, 'page' | 'size'>>({})

  const fetchWarningNoticeList = async (params?: Omit<WarningNoticeQueryParams, 'page' | 'size'>) => {
    try {
      loading.value = true
      lastQuery.value = {
        ...lastQuery.value,
        ...params
      }
      const response = await getAdminWarningNoticeList({
        ...lastQuery.value,
        page: currentPage.value,
        size: pageSize.value
      })
      notices.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      pageSize.value = response.size
      return response
    } catch (error) {
      console.error('Failed to fetch warning notice list:', error)
      ElMessage.error('加载预警通知列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchWarningNoticeDetail = async (id: number) => {
    try {
      loading.value = true
      const response = await getAdminWarningNoticeDetail(id)
      currentNotice.value = response
      return response
    } catch (error) {
      console.error('Failed to fetch warning notice detail:', error)
      ElMessage.error('加载预警通知详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const batchMarkAsRead = async (ids: number[]) => {
    if (!ids.length) {
      ElMessage.warning('请先选择需要标记的通知')
      return
    }

    try {
      loading.value = true
      await markAdminWarningsAsRead({ ids })

      const idSet = new Set(ids)
      notices.value = notices.value.map((item) => (
        idSet.has(item.id)
          ? { ...item, isRead: 1 }
          : item
      ))

      if (currentNotice.value && idSet.has(currentNotice.value.id)) {
        currentNotice.value = {
          ...currentNotice.value,
          isRead: 1
        }
      }

      ElMessage.success('已批量标记为已读')
    } catch (error) {
      console.error('Failed to mark warning notices as read:', error)
      ElMessage.error('批量标记已读失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const removeWarningNotice = async (id: number) => {
    try {
      loading.value = true
      await deleteAdminWarningNotice(id)

      const index = notices.value.findIndex((item) => item.id === id)
      if (index !== -1) {
        notices.value.splice(index, 1)
        total.value = Math.max(0, total.value - 1)
      }

      if (currentNotice.value?.id === id) {
        currentNotice.value = null
      }

      ElMessage.success('删除预警通知成功')
    } catch (error) {
      console.error('Failed to delete warning notice:', error)
      ElMessage.error('删除预警通知失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchThresholdConfig = async () => {
    try {
      loading.value = true
      const response = await getAdminThresholdConfig()
      threshold.value = response.threshold
      return response
    } catch (error) {
      console.error('Failed to fetch threshold config:', error)
      ElMessage.error('加载全局阈值失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateThresholdConfig = async (data: SetThresholdRequest) => {
    try {
      loading.value = true
      const response = await setAdminThresholdConfig(data)
      threshold.value = response.threshold
      ElMessage.success('更新全局阈值成功')
      return response
    } catch (error) {
      console.error('Failed to update threshold config:', error)
      ElMessage.error('更新全局阈值失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    notices.value = []
    currentNotice.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
    lastQuery.value = {}
  }

  return {
    loading,
    notices,
    currentNotice,
    total,
    currentPage,
    pageSize,
    threshold,
    lastQuery,
    fetchWarningNoticeList,
    fetchWarningNoticeDetail,
    batchMarkAsRead,
    removeWarningNotice,
    fetchThresholdConfig,
    updateThresholdConfig,
    reset
  }
})