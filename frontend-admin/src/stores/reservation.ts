import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cancelAdminReservation, getAdminReservationDetail, getAdminReservationList } from '@/api/reservation'
import { ReservationStatus, type ReservationInfo, type ReservationQueryParams } from '@/types/reservation'

export const useReservationStore = defineStore('reservationAdmin', () => {
  const loading = ref(false)
  const reservations = ref<ReservationInfo[]>([])
  const currentReservation = ref<ReservationInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const lastQuery = ref<Omit<ReservationQueryParams, 'page' | 'size'>>({})

  const fetchReservationList = async (params?: Omit<ReservationQueryParams, 'page' | 'size'>) => {
    try {
      loading.value = true
      lastQuery.value = {
        ...lastQuery.value,
        ...params
      }
      const response = await getAdminReservationList({
        ...lastQuery.value,
        page: currentPage.value,
        size: pageSize.value
      })
      reservations.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      pageSize.value = response.size
      return response
    } catch (error) {
      console.error('Failed to fetch reservation list:', error)
      ElMessage.error('Failed to load reservation list')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchReservationDetail = async (id: number) => {
    try {
      loading.value = true
      const response = await getAdminReservationDetail(id)
      currentReservation.value = response
      return response
    } catch (error) {
      console.error('Failed to fetch reservation detail:', error)
      ElMessage.error('Failed to load reservation detail')
      throw error
    } finally {
      loading.value = false
    }
  }

  const cancelReservation = async (id: number) => {
    try {
      loading.value = true
      await cancelAdminReservation(id)

      if (currentReservation.value?.id === id) {
        currentReservation.value = {
          ...currentReservation.value,
          status: currentReservation.value.status === ReservationStatus.PENDING
            ? ReservationStatus.CANCELLED
            : currentReservation.value.status,
          statusDesc: 'Cancelled',
          remainingMinutes: null
        }
      }

      const index = reservations.value.findIndex((item) => item.id === id)
      if (index !== -1) {
        const currentItem = reservations.value[index]
        if (currentItem) {
          reservations.value[index] = {
            ...currentItem,
            status: ReservationStatus.CANCELLED,
            statusDesc: 'Cancelled',
            remainingMinutes: null
          }
        }
      }

      ElMessage.success('Reservation cancelled')
    } catch (error) {
      console.error('Failed to cancel reservation:', error)
      ElMessage.error('Failed to cancel reservation')
      throw error
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    reservations.value = []
    currentReservation.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
    lastQuery.value = {}
  }

  return {
    loading,
    reservations,
    currentReservation,
    total,
    currentPage,
    pageSize,
    lastQuery,
    fetchReservationList,
    fetchReservationDetail,
    cancelReservation,
    reset
  }
})