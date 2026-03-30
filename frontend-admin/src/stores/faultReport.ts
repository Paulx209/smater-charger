import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getFaultReportList,
  getFaultReportDetail,
  handleFaultReport,
  getFaultStatistics
} from '@/api/faultReport'
import type {
  FaultReportInfo,
  FaultReportQueryParams,
  FaultReportHandleRequest,
  FaultStatisticsResponse
} from '@/types/faultReport'

export const useFaultReportStore = defineStore('adminFaultReport', () => {
  const loading = ref(false)
  const reports = ref<FaultReportInfo[]>([])
  const currentReport = ref<FaultReportInfo | null>(null)
  const statistics = ref<FaultStatisticsResponse | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  const fetchReportList = async (params?: FaultReportQueryParams) => {
    try {
      loading.value = true
      const queryParams = {
        page: currentPage.value,
        size: pageSize.value,
        ...params
      }
      const result = await getFaultReportList(queryParams)
      reports.value = result.content
      total.value = result.totalElements
      currentPage.value = result.number + 1
      pageSize.value = result.size
      return result
    } catch (error) {
      console.error('获取故障报修列表失败:', error)
      ElMessage.error('获取故障报修列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchReportDetail = async (id: number) => {
    try {
      loading.value = true
      const result = await getFaultReportDetail(id)
      currentReport.value = result
      return result
    } catch (error) {
      console.error('获取故障报修详情失败:', error)
      ElMessage.error('获取故障报修详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const submitHandle = async (id: number, data: FaultReportHandleRequest) => {
    try {
      loading.value = true
      const result = await handleFaultReport(id, data)
      currentReport.value = result
      const index = reports.value.findIndex(report => report.id === id)
      if (index !== -1) {
        reports.value[index] = result
      }
      ElMessage.success('报修处理已更新')
      return result
    } catch (error) {
      console.error('处理故障报修失败:', error)
      ElMessage.error('处理故障报修失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchStatistics = async (
    params?: Pick<FaultReportQueryParams, 'startDate' | 'endDate'>
  ) => {
    try {
      loading.value = true
      const result = await getFaultStatistics(params)
      statistics.value = result
      return result
    } catch (error) {
      console.error('获取故障统计失败:', error)
      ElMessage.error('获取故障统计失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    reports.value = []
    currentReport.value = null
    statistics.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
  }

  return {
    loading,
    reports,
    currentReport,
    statistics,
    total,
    currentPage,
    pageSize,
    fetchReportList,
    fetchReportDetail,
    submitHandle,
    fetchStatistics,
    reset
  }
})
