import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createFaultReport,
  getFaultReportList,
  getMyFaultReportList,
  getFaultReportDetail,
  updateFaultReport,
  deleteFaultReport,
  uploadFaultImage,
  getFaultReportStatsByPile
} from '@/api/faultReport'
import type {
  FaultReportInfo,
  FaultReportCreateRequest,
  FaultReportUpdateRequest,
  FaultReportQueryParams
} from '@/types/faultReport'

export const useFaultReportStore = defineStore('faultReport', () => {
  // 状态
  const loading = ref(false)
  const reports = ref<FaultReportInfo[]>([])
  const currentReport = ref<FaultReportInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  // 创建故障报修
  const createReport = async (data: FaultReportCreateRequest) => {
    try {
      loading.value = true
      const result = await createFaultReport(data)
      ElMessage.success('报修提交成功')
      return result
    } catch (error) {
      console.error('创建故障报修失败:', error)
      ElMessage.error('报修提交失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取故障报修列表
  const fetchReportList = async (params?: FaultReportQueryParams) => {
    try {
      loading.value = true
      const queryParams = {
        page: currentPage.value,
        size: pageSize.value,
        ...params
      }
      const result = await getFaultReportList(queryParams)
      reports.value = result.records
      total.value = result.total
      currentPage.value = result.current
      pageSize.value = result.size
      return result
    } catch (error) {
      console.error('获取故障报修列表失败:', error)
      ElMessage.error('获取报修列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取我的故障报修列表
  const fetchMyReportList = async (params?: FaultReportQueryParams) => {
    try {
      loading.value = true
      const queryParams = {
        page: currentPage.value,
        size: pageSize.value,
        ...params
      }
      const result = await getMyFaultReportList(queryParams)
      reports.value = result.records
      total.value = result.total
      currentPage.value = result.current
      pageSize.value = result.size
      return result
    } catch (error) {
      console.error('获取我的故障报修列表失败:', error)
      ElMessage.error('获取我的报修列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取故障报修详情
  const fetchReportDetail = async (id: number) => {
    try {
      loading.value = true
      const result = await getFaultReportDetail(id)
      currentReport.value = result
      return result
    } catch (error) {
      console.error('获取故障报修详情失败:', error)
      ElMessage.error('获取报修详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新故障报修
  const updateReport = async (id: number, data: FaultReportUpdateRequest) => {
    try {
      loading.value = true
      const result = await updateFaultReport(id, data)
      ElMessage.success('更新成功')
      // 更新当前报修信息
      if (currentReport.value && currentReport.value.id === id) {
        currentReport.value = result
      }
      // 更新列表中的报修信息
      const index = reports.value.findIndex(r => r.id === id)
      if (index !== -1) {
        reports.value[index] = result
      }
      return result
    } catch (error) {
      console.error('更新故障报修失败:', error)
      ElMessage.error('更新失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除故障报修
  const removeReport = async (id: number) => {
    try {
      loading.value = true
      await deleteFaultReport(id)
      ElMessage.success('删除成功')
      // 从列表中移除
      reports.value = reports.value.filter(r => r.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('删除故障报修失败:', error)
      ElMessage.error('删除失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 上传故障图片
  const uploadImage = async (file: File) => {
    try {
      const result = await uploadFaultImage(file)
      return result.url
    } catch (error) {
      console.error('上传故障图片失败:', error)
      ElMessage.error('图片上传失败')
      throw error
    }
  }

  // 获取充电桩的故障报修统计
  const fetchPileStats = async (pileId: number) => {
    try {
      const result = await getFaultReportStatsByPile(pileId)
      return result
    } catch (error) {
      console.error('获取充电桩故障报修统计失败:', error)
      throw error
    }
  }

  // 重置状态
  const reset = () => {
    reports.value = []
    currentReport.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
  }

  return {
    // 状态
    loading,
    reports,
    currentReport,
    total,
    currentPage,
    pageSize,

    // 方法
    createReport,
    fetchReportList,
    fetchMyReportList,
    fetchReportDetail,
    updateReport,
    removeReport,
    uploadImage,
    fetchPileStats,
    reset
  }
})
