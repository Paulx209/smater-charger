import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  startCharging,
  endCharging,
  getChargingRecordList,
  getChargingRecordDetail,
  getCurrentChargingRecord,
  getMonthlyStatistics,
  getYearlyStatistics
} from '@/api/chargingRecord'
import type {
  ChargingRecordInfo,
  ChargingRecordStartRequest,
  ChargingRecordEndRequest,
  ChargingRecordQueryParams,
  ChargingRecordStatisticsMonthly,
  ChargingRecordStatisticsYearly
} from '@/types/chargingRecord'

export const useChargingRecordStore = defineStore('chargingRecord', () => {
  // 状态
  const chargingRecords = ref<ChargingRecordInfo[]>([])
  const currentRecord = ref<ChargingRecordInfo | null>(null)
  const currentChargingRecord = ref<ChargingRecordInfo | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  /**
   * 开始充电
   */
  const beginCharging = async (data: ChargingRecordStartRequest) => {
    try {
      loading.value = true
      const result = await startCharging(data)
      ElMessage.success('开始充电成功')
      currentChargingRecord.value = result
      return result
    } catch (error) {
      console.error('开始充电失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 结束充电
   */
  const finishCharging = async (id: number, data: ChargingRecordEndRequest) => {
    try {
      loading.value = true
      const result = await endCharging(id, data)
      ElMessage.success('结束充电成功')
      currentChargingRecord.value = null
      return result
    } catch (error) {
      console.error('结束充电失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 查询充电记录列表
   */
  const fetchChargingRecordList = async (params?: ChargingRecordQueryParams) => {
    try {
      loading.value = true
      const response = await getChargingRecordList({
        ...params,
        page: params?.page ?? currentPage.value,
        size: params?.size ?? pageSize.value
      })
      chargingRecords.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      return response
    } catch (error) {
      console.error('查询充电记录列表失败:', error)
      ElMessage.error('查询充电记录列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取充电记录详情
   */
  const fetchChargingRecordDetail = async (id: number) => {
    try {
      loading.value = true
      const data = await getChargingRecordDetail(id)
      currentRecord.value = data
      return data
    } catch (error) {
      console.error('获取充电记录详情失败:', error)
      ElMessage.error('获取充电记录详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取当前充电记录
   */
  const fetchCurrentChargingRecord = async () => {
    try {
      const data = await getCurrentChargingRecord()
      currentChargingRecord.value = data
      return data
    } catch (error) {
      console.error('获取当前充电记录失败:', error)
      throw error
    }
  }

  /**
   * 查询月度统计
   */
  const fetchMonthlyStatistics = async (year: number, month: number) => {
    try {
      loading.value = true
      const data = await getMonthlyStatistics(year, month)
      return data
    } catch (error) {
      console.error('查询月度统计失败:', error)
      ElMessage.error('查询月度统计失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 查询年度统计
   */
  const fetchYearlyStatistics = async (year: number) => {
    try {
      loading.value = true
      const data = await getYearlyStatistics(year)
      return data
    } catch (error) {
      console.error('查询年度统计失败:', error)
      ElMessage.error('查询年度统计失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 清空当前记录
   */
  const clearCurrentRecord = () => {
    currentRecord.value = null
  }

  /**
   * 重置分页
   */
  const resetPagination = () => {
    currentPage.value = 1
    total.value = 0
  }

  return {
    chargingRecords,
    currentRecord,
    currentChargingRecord,
    loading,
    total,
    currentPage,
    pageSize,
    beginCharging,
    finishCharging,
    fetchChargingRecordList,
    fetchChargingRecordDetail,
    fetchCurrentChargingRecord,
    fetchMonthlyStatistics,
    fetchYearlyStatistics,
    clearCurrentRecord,
    resetPagination
  }
})
