import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  getChargingPiles,
  getChargingPileById,
  getNearbyChargingPiles
} from '@/api/chargingPile'
import type {
  ChargingPileInfo,
  ChargingPileQueryParams,
  NearbyQueryParams
} from '@/types/chargingPile'

export const useChargingPileStore = defineStore('chargingPile', () => {
  // 充电桩列表
  const chargingPiles = ref<ChargingPileInfo[]>([])

  // 当前查看的充电桩
  const currentPile = ref<ChargingPileInfo | null>(null)

  // 查询参数
  const queryParams = ref<ChargingPileQueryParams>({
    keyword: '',
    type: undefined,
    status: undefined,
    page: 1,
    size: 10
  })

  // 总数
  const total = ref(0)

  // 总页数
  const pages = ref(0)

  // 加载状态
  const loading = ref(false)

  /**
   * 查询充电桩列表
   */
  const fetchChargingPiles = async (params?: ChargingPileQueryParams) => {
    try {
      loading.value = true

      // 合并查询参数
      const mergedParams = { ...queryParams.value, ...params }
      queryParams.value = mergedParams

      const data = await getChargingPiles(mergedParams)

      chargingPiles.value = data.records
      total.value = data.total
      pages.value = data.pages

      return data
    } catch (error) {
      console.error('查询充电桩列表失败:', error)
      ElMessage.error('查询充电桩列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取充电桩详情
   */
  const fetchChargingPileById = async (id: number) => {
    try {
      loading.value = true

      const data = await getChargingPileById(id)
      currentPile.value = data

      return data
    } catch (error) {
      console.error('获取充电桩详情失败:', error)
      ElMessage.error('获取充电桩详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取附近充电桩
   */
  const fetchNearbyPiles = async (params: NearbyQueryParams) => {
    try {
      loading.value = true

      const data = await getNearbyChargingPiles(params)
      chargingPiles.value = data

      return data
    } catch (error) {
      console.error('获取附近充电桩失败:', error)
      ElMessage.error('获取附近充电桩失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新查询参数
   */
  const updateQueryParams = (params: Partial<ChargingPileQueryParams>) => {
    queryParams.value = { ...queryParams.value, ...params }
  }

  /**
   * 重置查询参数
   */
  const resetQueryParams = () => {
    queryParams.value = {
      keyword: '',
      type: undefined,
      status: undefined,
      page: 1,
      size: 10
    }
  }

  /**
   * 清空当前充电桩
   */
  const clearCurrentPile = () => {
    currentPile.value = null
  }

  return {
    chargingPiles,
    currentPile,
    queryParams,
    total,
    pages,
    loading,
    fetchChargingPiles,
    fetchChargingPileById,
    fetchNearbyPiles,
    updateQueryParams,
    resetQueryParams,
    clearCurrentPile
  }
})
