import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  createPriceConfig,
  updatePriceConfig,
  deletePriceConfig,
  getPriceConfigList,
  getPriceConfigDetail
} from '@/api/priceConfig'
import type {
  PriceConfigInfo,
  PriceConfigCreateRequest,
  PriceConfigUpdateRequest,
  PriceConfigQueryParams
} from '@/types/priceConfig'

export const usePriceConfigStore = defineStore('priceConfig', () => {
  const priceConfigs = ref<PriceConfigInfo[]>([])
  const currentPriceConfig = ref<PriceConfigInfo | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

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
      console.error('Failed to load price config list:', error)
      ElMessage.error('加载价格配置列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchPriceConfigDetail = async (id: number) => {
    try {
      loading.value = true
      const data = await getPriceConfigDetail(id)
      currentPriceConfig.value = data
      return data
    } catch (error) {
      console.error('Failed to load price config detail:', error)
      ElMessage.error('加载价格配置详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const addPriceConfig = async (data: PriceConfigCreateRequest) => {
    try {
      loading.value = true
      const result = await createPriceConfig(data)
      ElMessage.success('新增价格配置成功')
      await fetchPriceConfigList()
      return result
    } catch (error) {
      console.error('Failed to create price config:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const modifyPriceConfig = async (id: number, data: PriceConfigUpdateRequest) => {
    try {
      loading.value = true
      const result = await updatePriceConfig(id, data)
      ElMessage.success('更新价格配置成功')
      await fetchPriceConfigList()
      return result
    } catch (error) {
      console.error('Failed to update price config:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const removePriceConfig = async (id: number) => {
    try {
      loading.value = true
      await deletePriceConfig(id)
      ElMessage.success('删除价格配置成功')
      await fetchPriceConfigList()
    } catch (error) {
      console.error('Failed to delete price config:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const toggleActive = async (id: number, isActive: number) => {
    try {
      loading.value = true
      await updatePriceConfig(id, { isActive })
      ElMessage.success(isActive === 1 ? '启用价格配置成功' : '停用价格配置成功')
      await fetchPriceConfigList()
    } catch (error) {
      console.error('Failed to toggle price config status:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const clearCurrentPriceConfig = () => {
    currentPriceConfig.value = null
  }

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
    clearCurrentPriceConfig,
    resetPagination
  }
})