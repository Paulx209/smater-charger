import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  createPriceConfig,
  updatePriceConfig,
  deletePriceConfig,
  getPriceConfigList,
  getPriceConfigDetail,
  getCurrentPriceConfig,
  estimatePrice
} from '@/api/priceConfig'
import type {
  PriceConfigInfo,
  PriceConfigCreateRequest,
  PriceConfigUpdateRequest,
  PriceConfigQueryParams,
  PriceEstimateRequest,
  ChargingPileType
} from '@/types/priceConfig'

export const usePriceConfigStore = defineStore('priceConfig', () => {
  // 状态
  const priceConfigs = ref<PriceConfigInfo[]>([])
  const currentPriceConfig = ref<PriceConfigInfo | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  /**
   * 查询费用配置列表
   */
  const fetchPriceConfigList = async (params?: PriceConfigQueryParams) => {
    try {
      loading.value = true
      const response = await getPriceConfigList({
        ...params,
        page: params?.page ?? currentPage.value,
        size: params?.size ?? pageSize.value
      })
      priceConfigs.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      return response
    } catch (error) {
      console.error('查询费用配置列表失败:', error)
      ElMessage.error('查询费用配置列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取费用配置详情
   */
  const fetchPriceConfigDetail = async (id: number) => {
    try {
      loading.value = true
      const data = await getPriceConfigDetail(id)
      currentPriceConfig.value = data
      return data
    } catch (error) {
      console.error('获取费用配置详情失败:', error)
      ElMessage.error('获取费用配置详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建费用配置
   */
  const addPriceConfig = async (data: PriceConfigCreateRequest) => {
    try {
      loading.value = true
      const result = await createPriceConfig(data)
      ElMessage.success('创建成功')
      await fetchPriceConfigList()
      return result
    } catch (error) {
      console.error('创建费用配置失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新费用配置
   */
  const modifyPriceConfig = async (id: number, data: PriceConfigUpdateRequest) => {
    try {
      loading.value = true
      const result = await updatePriceConfig(id, data)
      ElMessage.success('更新成功')
      await fetchPriceConfigList()
      return result
    } catch (error) {
      console.error('更新费用配置失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除费用配置
   */
  const removePriceConfig = async (id: number) => {
    try {
      loading.value = true
      await deletePriceConfig(id)
      ElMessage.success('删除成功')
      await fetchPriceConfigList()
    } catch (error) {
      console.error('删除费用配置失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 切换激活状态
   */
  const toggleActive = async (id: number, isActive: number) => {
    try {
      loading.value = true
      await updatePriceConfig(id, { isActive })
      ElMessage.success(isActive === 1 ? '已激活' : '已停用')
      await fetchPriceConfigList()
    } catch (error) {
      console.error('切换激活状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取当前有效费用配置
   */
  const fetchCurrentPriceConfig = async (chargingPileType: ChargingPileType) => {
    try {
      const data = await getCurrentPriceConfig(chargingPileType)
      return data
    } catch (error) {
      console.error('获取当前有效费用配置失败:', error)
      throw error
    }
  }

  /**
   * 费用预估
   */
  const estimateFee = async (data: PriceEstimateRequest) => {
    try {
      const result = await estimatePrice(data)
      return result
    } catch (error) {
      console.error('费用预估失败:', error)
      ElMessage.error('费用预估失败')
      throw error
    }
  }

  /**
   * 清空当前费用配置
   */
  const clearCurrentPriceConfig = () => {
    currentPriceConfig.value = null
  }

  /**
   * 重置分页
   */
  const resetPagination = () => {
    currentPage.value = 1
    total.value = 0
  }

  return {
    priceConfigs,
    currentPriceConfig,
    loading,
    total,
    currentPage,
    pageSize,
    fetchPriceConfigList,
    fetchPriceConfigDetail,
    addPriceConfig,
    modifyPriceConfig,
    removePriceConfig,
    toggleActive,
    fetchCurrentPriceConfig,
    estimateFee,
    clearCurrentPriceConfig,
    resetPagination
  }
})
