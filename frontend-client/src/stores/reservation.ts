import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  getMyReservations,
  getReservationById,
  createReservation,
  cancelReservation,
  checkPileAvailability
} from '@/api/reservation'
import type {
  ReservationInfo,
  ReservationCreateRequest,
  ReservationCancelRequest,
  ReservationQueryParams,
  ReservationStatus
} from '@/types/reservation'

export const useReservationStore = defineStore('reservation', () => {
  // 状态
  const reservations = ref<ReservationInfo[]>([])
  const currentReservation = ref<ReservationInfo | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  // 获取进行中的预约（待确认和已确认）
  const activeReservations = computed(() =>
    reservations.value.filter(r => r.status === 0 || r.status === 1)
  )

  // 获取历史预约（已取消、已完成、已过期）
  const historyReservations = computed(() =>
    reservations.value.filter(r => r.status === 2 || r.status === 3 || r.status === 4)
  )

  /**
   * 查询我的预约列表
   */
  const fetchMyReservations = async (params?: ReservationQueryParams) => {
    try {
      loading.value = true
      const response = await getMyReservations({
        ...params,
        page: params?.page ?? currentPage.value,  // 直接使用 currentPage，不需要减 1
        size: params?.size ?? pageSize.value
      })
      reservations.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      return response
    } catch (error) {
      console.error('查询预约列表失败:', error)
      ElMessage.error('查询预约列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取预约详情
   */
  const fetchReservationById = async (id: number) => {
    try {
      loading.value = true
      const data = await getReservationById(id)
      currentReservation.value = data
      return data
    } catch (error) {
      console.error('获取预约详情失败:', error)
      ElMessage.error('获取预约详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建预约
   */
  const addReservation = async (data: ReservationCreateRequest) => {
    try {
      loading.value = true
      const result = await createReservation(data)
      ElMessage.success('预约成功')
      await fetchMyReservations()
      return result
    } catch (error) {
      console.error('创建预约失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消预约
   */
  const cancelReservationById = async (id: number, data?: ReservationCancelRequest) => {
    try {
      loading.value = true
      await cancelReservation(id, data)
      ElMessage.success('取消成功')
      await fetchMyReservations()
    } catch (error) {
      console.error('取消预约失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 检查充电桩可用性
   */
  const checkAvailability = async (
    pileId: number,
    startTime: string,
    endTime: string
  ): Promise<boolean> => {
    try {
      return await checkPileAvailability(pileId, startTime, endTime)
    } catch (error) {
      console.error('检查充电桩可用性失败:', error)
      ElMessage.error('检查充电桩可用性失败')
      throw error
    }
  }

  /**
   * 清空当前预约
   */
  const clearCurrentReservation = () => {
    currentReservation.value = null
  }

  /**
   * 重置分页
   */
  const resetPagination = () => {
    currentPage.value = 1
    total.value = 0
  }

  return {
    reservations,
    currentReservation,
    loading,
    total,
    currentPage,
    pageSize,
    activeReservations,
    historyReservations,
    fetchMyReservations,
    fetchReservationById,
    addReservation,
    cancelReservationById,
    checkAvailability,
    clearCurrentReservation,
    resetPagination
  }
})
