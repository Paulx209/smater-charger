import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { getCurrentPriceConfig, estimatePrice } from '@/api/priceConfig'
import type {
  PriceConfigInfo,
  PriceEstimateRequest,
  ChargingPileType
} from '@/types/priceConfig'

export const usePriceConfigStore = defineStore('priceConfig', () => {
  const currentPriceConfig = ref<PriceConfigInfo | null>(null)
  const loading = ref(false)

  const fetchCurrentPriceConfig = async (chargingPileType: ChargingPileType) => {
    try {
      loading.value = true
      const data = await getCurrentPriceConfig(chargingPileType)
      currentPriceConfig.value = data
      return data
    } catch (error) {
      console.error('Failed to load current price config:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const estimateFee = async (data: PriceEstimateRequest) => {
    try {
      return await estimatePrice(data)
    } catch (error) {
      console.error('Failed to estimate charging fee:', error)
      ElMessage.error('费用估算失败')
      throw error
    }
  }

  const clearCurrentPriceConfig = () => {
    currentPriceConfig.value = null
  }

  return {
    currentPriceConfig,
    loading,
    fetchCurrentPriceConfig,
    estimateFee,
    clearCurrentPriceConfig
  }
})