import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminChargingRecordDetail, getAdminChargingRecordList } from '@/api/chargingRecord'
import type { ChargingRecordInfo, ChargingRecordQueryParams } from '@/types/chargingRecord'

export const useChargingRecordStore = defineStore('chargingRecordAdmin', () => {
  const loading = ref(false)
  const records = ref<ChargingRecordInfo[]>([])
  const currentRecord = ref<ChargingRecordInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const lastQuery = ref<Omit<ChargingRecordQueryParams, 'page' | 'size'>>({})

  const fetchRecordList = async (params?: Omit<ChargingRecordQueryParams, 'page' | 'size'>) => {
    try {
      loading.value = true
      lastQuery.value = {
        ...lastQuery.value,
        ...params
      }
      const response = await getAdminChargingRecordList({
        ...lastQuery.value,
        page: currentPage.value,
        size: pageSize.value
      })
      records.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      pageSize.value = response.size
      return response
    } catch (error) {
      console.error('Failed to fetch charging record list:', error)
      ElMessage.error('加载充电记录失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchRecordDetail = async (id: number) => {
    try {
      loading.value = true
      const response = await getAdminChargingRecordDetail(id)
      currentRecord.value = response
      return response
    } catch (error) {
      console.error('Failed to fetch charging record detail:', error)
      ElMessage.error('加载充电记录详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    records.value = []
    currentRecord.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
    lastQuery.value = {}
  }

  return {
    loading,
    records,
    currentRecord,
    total,
    currentPage,
    pageSize,
    lastQuery,
    fetchRecordList,
    fetchRecordDetail,
    reset
  }
})