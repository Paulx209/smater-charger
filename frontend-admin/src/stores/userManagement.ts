/**
 * 用户管理状态管理
 */
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

export const useUserManagementStore = defineStore('userManagement', () => {
  // 状态
  const loading = ref(false)
  const users = ref<UserAdminResponse[]>([])
  const currentUser = ref<UserAdminResponse | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  /**
   * 获取用户列表
   */
  const fetchUserList = async (params?: UserQueryParams) => {
    try {
      loading.value = true
      const queryParams = {
        ...params,
        page: params?.page || currentPage.value,
        size: params?.size || pageSize.value
      }
      const response = await getAdminUserList(queryParams)
      users.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1 // 后端从0开始，前端从1开始
      return response
    } catch (error) {
      console.error('获取用户列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户详情
   */
  const fetchUserDetail = async (id: number) => {
    try {
      loading.value = true
      const response = await getAdminUserDetail(id)
      currentUser.value = response
      return response
    } catch (error) {
      console.error('获取用户详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新用户状态
   */
  const updateStatus = async (id: number, data: UserStatusUpdateRequest) => {
    try {
      loading.value = true
      const response = await updateUserStatus(id, data)
      ElMessage.success('用户状态更新成功')

      // 更新列表中的用户状态
      const index = users.value.findIndex(u => u.id === id)
      if (index !== -1) {
        users.value[index] = { ...users.value[index], ...response }
      }

      // 更新当前用户详情
      if (currentUser.value && currentUser.value.id === id) {
        currentUser.value = { ...currentUser.value, ...response }
      }

      return response
    } catch (error) {
      console.error('更新用户状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置用户密码
   */
  const resetPassword = async (id: number, data: PasswordResetRequest): Promise<PasswordResetResponse> => {
    try {
      loading.value = true
      const response = await resetUserPassword(id, data)
      ElMessage.success('密码重置成功')
      return response
    } catch (error) {
      console.error('重置密码失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户充电记录
   */
  const fetchChargingRecords = async (
    id: number,
    params: {
      status?: string
      startDate?: string
      endDate?: string
      page?: number
      size?: number
    }
  ): Promise<PageResponse<ChargingRecord>> => {
    try {
      loading.value = true
      const response = await getUserChargingRecords(id, params)
      return response
    } catch (error) {
      console.error('获取充电记录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户预约记录
   */
  const fetchReservations = async (
    id: number,
    params: {
      status?: string
      startDate?: string
      endDate?: string
      page?: number
      size?: number
    }
  ): Promise<PageResponse<ReservationRecord>> => {
    try {
      loading.value = true
      const response = await getUserReservations(id, params)
      return response
    } catch (error) {
      console.error('获取预约记录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户违规记录
   */
  const fetchViolations = async (
    id: number,
    params: {
      startDate?: string
      endDate?: string
      page?: number
      size?: number
    }
  ): Promise<ViolationPageResponse> => {
    try {
      loading.value = true
      const response = await getUserViolations(id, params)
      return response
    } catch (error) {
      console.error('获取违规记录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 导出用户列表
   */
  const exportUserList = async (params: { status?: number; isActive?: boolean }) => {
    try {
      loading.value = true
      const blob = await exportUsers(params)

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `users_${new Date().toISOString().split('T')[0]}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出用户列表失败:', error)
      ElMessage.error('导出失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量更新用户状态
   */
  const batchUpdateStatus = async (data: BatchStatusUpdateRequest): Promise<BatchOperationResult> => {
    try {
      loading.value = true
      const response = await batchUpdateUserStatus(data)

      if (response.successCount > 0) {
        ElMessage.success(`成功更新 ${response.successCount} 个用户状态`)
      }

      if (response.failCount > 0) {
        ElMessage.warning(`${response.failCount} 个用户更新失败`)
      }

      return response
    } catch (error) {
      console.error('批量更新用户状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    users.value = []
    currentUser.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
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
