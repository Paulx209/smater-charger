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

const DEFAULT_PAGE_SIZE = 10

export const useAnnouncementStore = defineStore('announcement', () => {
  const loading = ref(false)
  const adminAnnouncements = ref<AnnouncementInfo[]>([])
  const clientAnnouncements = ref<AnnouncementClientInfo[]>([])
  const latestAnnouncements = ref<AnnouncementClientInfo[]>([])
  const currentAnnouncement = ref<AnnouncementInfo | AnnouncementClientInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(DEFAULT_PAGE_SIZE)

  const createNewAnnouncement = async (data: AnnouncementCreateRequest) => {
    try {
      loading.value = true
      const result = await createAnnouncement(data)
      ElMessage.success('公告创建成功')
      return result
    } finally {
      loading.value = false
    }
  }

  const modifyAnnouncement = async (id: number, data: AnnouncementUpdateRequest) => {
    try {
      loading.value = true
      const result = await updateAnnouncement(id, data)
      const index = adminAnnouncements.value.findIndex((item) => item.id === id)
      if (index !== -1) {
        adminAnnouncements.value[index] = result
      }
      if (currentAnnouncement.value?.id === id) {
        currentAnnouncement.value = result
      }
      ElMessage.success('公告更新成功')
      return result
    } finally {
      loading.value = false
    }
  }

  const removeAnnouncement = async (id: number) => {
    try {
      loading.value = true
      await deleteAnnouncement(id)
      adminAnnouncements.value = adminAnnouncements.value.filter((item) => item.id !== id)
      total.value = Math.max(0, total.value - 1)
      ElMessage.success('公告删除成功')
    } finally {
      loading.value = false
    }
  }

  const publishAnnouncementById = async (id: number) => {
    try {
      loading.value = true
      const result = await publishAnnouncement(id)
      const index = adminAnnouncements.value.findIndex((item) => item.id === id)
      if (index !== -1) {
        adminAnnouncements.value[index] = result
      }
      if (currentAnnouncement.value?.id === id) {
        currentAnnouncement.value = result
      }
      ElMessage.success('公告发布成功')
      return result
    } finally {
      loading.value = false
    }
  }

  const unpublishAnnouncementById = async (id: number) => {
    try {
      loading.value = true
      const result = await unpublishAnnouncement(id)
      const index = adminAnnouncements.value.findIndex((item) => item.id === id)
      if (index !== -1) {
        adminAnnouncements.value[index] = result
      }
      if (currentAnnouncement.value?.id === id) {
        currentAnnouncement.value = result
      }
      ElMessage.success('公告下线成功')
      return result
    } finally {
      loading.value = false
    }
  }

  const fetchAdminAnnouncementList = async (params: AnnouncementQueryParams = {}) => {
    try {
      loading.value = true
      const result = await getAdminAnnouncementList({
        page: params.page ?? currentPage.value,
        size: params.size ?? pageSize.value,
        status: params.status,
        keyword: params.keyword
      })
      adminAnnouncements.value = result.content
      total.value = result.totalElements
      currentPage.value = result.number + 1
      pageSize.value = result.size
      return result
    } finally {
      loading.value = false
    }
  }

  const fetchAdminAnnouncementDetail = async (id: number) => {
    try {
      loading.value = true
      const result = await getAdminAnnouncementDetail(id)
      currentAnnouncement.value = result
      return result
    } finally {
      loading.value = false
    }
  }

  const fetchClientAnnouncementList = async (params: { page?: number; size?: number } = {}) => {
    try {
      loading.value = true
      const result = await getAnnouncementList({
        page: params.page ?? currentPage.value,
        size: params.size ?? pageSize.value
      })
      clientAnnouncements.value = result.content
      total.value = result.totalElements
      currentPage.value = result.number + 1
      pageSize.value = result.size
      return result
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
    } finally {
      loading.value = false
    }
  }

  const fetchLatestAnnouncements = async (limit: number = 3) => {
    const result = await getLatestAnnouncements(limit)
    latestAnnouncements.value = result
    return result
  }

  const reset = () => {
    adminAnnouncements.value = []
    clientAnnouncements.value = []
    latestAnnouncements.value = []
    currentAnnouncement.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = DEFAULT_PAGE_SIZE
  }

  return {
    loading,
    adminAnnouncements,
    clientAnnouncements,
    latestAnnouncements,
    currentAnnouncement,
    total,
    currentPage,
    pageSize,
    createNewAnnouncement,
    modifyAnnouncement,
    removeAnnouncement,
    publishAnnouncementById,
    unpublishAnnouncementById,
    fetchAdminAnnouncementList,
    fetchAdminAnnouncementDetail,
    fetchClientAnnouncementList,
    fetchClientAnnouncementDetail,
    fetchLatestAnnouncements,
    reset
  }
})