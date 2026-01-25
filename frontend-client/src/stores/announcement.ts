import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  publishAnnouncement,
  unpublishAnnouncement,
  getAdminAnnouncementList,
  getAdminAnnouncementDetail,
  getAnnouncementList,
  getAnnouncementDetail,
  getLatestAnnouncements
} from '@/api/announcement'
import type {
  AnnouncementInfo,
  AnnouncementClientInfo,
  AnnouncementCreateRequest,
  AnnouncementUpdateRequest,
  AnnouncementQueryParams
} from '@/types/announcement'

export const useAnnouncementStore = defineStore('announcement', () => {
  // 状态
  const loading = ref(false)
  const adminAnnouncements = ref<AnnouncementInfo[]>([])
  const clientAnnouncements = ref<AnnouncementClientInfo[]>([])
  const latestAnnouncements = ref<AnnouncementClientInfo[]>([])
  const currentAnnouncement = ref<AnnouncementInfo | AnnouncementClientInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  // ==================== 管理端方法 ====================

  // 创建公告
  const createNewAnnouncement = async (data: AnnouncementCreateRequest) => {
    try {
      loading.value = true
      const result = await createAnnouncement(data)
      ElMessage.success('创建成功')
      return result
    } catch (error) {
      console.error('创建公告失败:', error)
      ElMessage.error('创建失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新公告
  const modifyAnnouncement = async (id: number, data: AnnouncementUpdateRequest) => {
    try {
      loading.value = true
      const result = await updateAnnouncement(id, data)
      ElMessage.success('更新成功')
      // 更新列表中的公告
      const index = adminAnnouncements.value.findIndex(a => a.id === id)
      if (index !== -1) {
        adminAnnouncements.value[index] = result
      }
      return result
    } catch (error) {
      console.error('更新公告失败:', error)
      ElMessage.error('更新失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除公告
  const removeAnnouncement = async (id: number) => {
    try {
      loading.value = true
      await deleteAnnouncement(id)
      ElMessage.success('删除成功')
      // 从列表中移除
      adminAnnouncements.value = adminAnnouncements.value.filter(a => a.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('删除公告失败:', error)
      ElMessage.error('删除失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 发布公告
  const publishAnnouncementById = async (id: number) => {
    try {
      loading.value = true
      const result = await publishAnnouncement(id)
      ElMessage.success('发布成功')
      // 更新列表中的公告
      const index = adminAnnouncements.value.findIndex(a => a.id === id)
      if (index !== -1) {
        adminAnnouncements.value[index] = result
      }
      return result
    } catch (error) {
      console.error('发布公告失败:', error)
      ElMessage.error('发布失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 下线公告
  const unpublishAnnouncementById = async (id: number) => {
    try {
      loading.value = true
      const result = await unpublishAnnouncement(id)
      ElMessage.success('下线成功')
      // 更新列表中的公告
      const index = adminAnnouncements.value.findIndex(a => a.id === id)
      if (index !== -1) {
        adminAnnouncements.value[index] = result
      }
      return result
    } catch (error) {
      console.error('下线公告失败:', error)
      ElMessage.error('下线失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取管理端公告列表
  const fetchAdminAnnouncementList = async (params?: AnnouncementQueryParams) => {
    try {
      loading.value = true
      const queryParams = {
        page: currentPage.value,
        size: pageSize.value,
        ...params
      }
      const result = await getAdminAnnouncementList(queryParams)
      adminAnnouncements.value = result.content
      total.value = result.totalElements
      currentPage.value = result.number + 1 // Spring Data JPA 的 page 从 0 开始
      pageSize.value = result.size
      return result
    } catch (error) {
      console.error('获取管理端公告列表失败:', error)
      ElMessage.error('获取公告列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取管理端公告详情
  const fetchAdminAnnouncementDetail = async (id: number) => {
    try {
      loading.value = true
      const result = await getAdminAnnouncementDetail(id)
      currentAnnouncement.value = result
      return result
    } catch (error) {
      console.error('获取管理端公告详情失败:', error)
      ElMessage.error('获取公告详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

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
    adminAnnouncements.value = []
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
    adminAnnouncements,
    clientAnnouncements,
    latestAnnouncements,
    currentAnnouncement,
    total,
    currentPage,
    pageSize,

    // 管理端方法
    createNewAnnouncement,
    modifyAnnouncement,
    removeAnnouncement,
    publishAnnouncementById,
    unpublishAnnouncementById,
    fetchAdminAnnouncementList,
    fetchAdminAnnouncementDetail,

    // 车主端方法
    fetchClientAnnouncementList,
    fetchClientAnnouncementDetail,
    fetchLatestAnnouncements,

    // 工具方法
    reset
  }
})
