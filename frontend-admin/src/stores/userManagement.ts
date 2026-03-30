import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  getAdminUserList,
  getAdminUserDetail,
  updateUserStatus,
  resetUserPassword,
  getUserChargingRecords,
  getUserReservations,
  getUserViolations,
  exportUsers,
  batchUpdateUserStatus
} from '@/api/user'
import type {
  UserAdminResponse,
  UserQueryParams,
  UserStatusUpdateRequest,
  PasswordResetRequest,
  PasswordResetResponse,
  BatchStatusUpdateRequest,
  BatchOperationResult,
  PageResponse,
  ChargingRecord,
  ReservationRecord,
  ViolationPageResponse
} from '@/types/user'

const DEFAULT_PAGE_SIZE = 10

export const useUserManagementStore = defineStore('userManagement', () => {
  const loading = ref(false)
  const users = ref<UserAdminResponse[]>([])
  const currentUser = ref<UserAdminResponse | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(DEFAULT_PAGE_SIZE)

  const fetchUserList = async (params: UserQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams: UserQueryParams = {
        ...params,
        page: params.page ?? currentPage.value,
        size: params.size ?? pageSize.value
      }
      const response = await getAdminUserList(queryParams)
      users.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      pageSize.value = response.size
      return response
    } finally {
      loading.value = false
    }
  }

  const fetchUserDetail = async (id: number) => {
    try {
      loading.value = true
      const response = await getAdminUserDetail(id)
      currentUser.value = response
      return response
    } finally {
      loading.value = false
    }
  }

  const applyUserUpdate = (id: number, nextUser: UserAdminResponse) => {
    const index = users.value.findIndex((user) => user.id === id)
    if (index !== -1) {
      users.value[index] = nextUser
    }
    if (currentUser.value?.id === id) {
      currentUser.value = nextUser
    }
  }

  const updateStatus = async (id: number, data: UserStatusUpdateRequest) => {
    try {
      loading.value = true
      const response = await updateUserStatus(id, data)
      applyUserUpdate(id, response)
      ElMessage.success('用户状态更新成功')
      return response
    } finally {
      loading.value = false
    }
  }

  const resetPassword = async (id: number, data: PasswordResetRequest = {}): Promise<PasswordResetResponse> => {
    try {
      loading.value = true
      const response = await resetUserPassword(id, data)
      ElMessage.success('密码重置成功')
      return response
    } finally {
      loading.value = false
    }
  }

  const fetchChargingRecords = async (
    id: number,
    params: {
      status?: string
      startDate?: string
      endDate?: string
      page?: number
      size?: number
    } = {}
  ): Promise<PageResponse<ChargingRecord>> => {
    try {
      loading.value = true
      return await getUserChargingRecords(id, params)
    } finally {
      loading.value = false
    }
  }

  const fetchReservations = async (
    id: number,
    params: {
      status?: string
      startDate?: string
      endDate?: string
      page?: number
      size?: number
    } = {}
  ): Promise<PageResponse<ReservationRecord>> => {
    try {
      loading.value = true
      return await getUserReservations(id, params)
    } finally {
      loading.value = false
    }
  }

  const fetchViolations = async (
    id: number,
    params: {
      startDate?: string
      endDate?: string
      page?: number
      size?: number
    } = {}
  ): Promise<ViolationPageResponse> => {
    try {
      loading.value = true
      return await getUserViolations(id, params)
    } finally {
      loading.value = false
    }
  }

  const exportUserList = async (params: { status?: number; isActive?: boolean } = {}) => {
    try {
      loading.value = true
      const blob = await exportUsers(params)
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `users_${new Date().toISOString().split('T')[0]}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('用户导出成功')
    } finally {
      loading.value = false
    }
  }

  const batchUpdateStatus = async (data: BatchStatusUpdateRequest): Promise<BatchOperationResult> => {
    try {
      loading.value = true
      const response = await batchUpdateUserStatus(data)

      if (response.successCount > 0) {
        ElMessage.success(`已更新 ${response.successCount} 个用户`)
      }
      if (response.failCount > 0) {
        ElMessage.warning(`${response.failCount} 个用户更新失败`)
      }

      return response
    } finally {
      loading.value = false
    }
  }

  const resetState = () => {
    users.value = []
    currentUser.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = DEFAULT_PAGE_SIZE
  }

  return {
    loading,
    users,
    currentUser,
    total,
    currentPage,
    pageSize,
    fetchUserList,
    fetchUserDetail,
    updateStatus,
    resetPassword,
    fetchChargingRecords,
    fetchReservations,
    fetchViolations,
    exportUserList,
    batchUpdateStatus,
    resetState
  }
})