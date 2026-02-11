import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as chargingPileApi from '@/api/chargingPile'
import type {
  ChargingPile,
  ChargingPileQueryParams,
  ChargingPileCreateRequest,
  ChargingPileUpdateRequest,
  ChargingPileStatusUpdateRequest,
  BatchDeleteRequest,
  PageResponse
} from '@/api/chargingPile'

export const useChargingPileStore = defineStore('chargingPile', () => {
  // 状态
  const chargingPiles = ref<ChargingPile[]>([])
  const currentChargingPile = ref<ChargingPile | null>(null)
  const total = ref(0)
  const loading = ref(false)

  // 查询充电桩列表
  const fetchChargingPiles = async (params: ChargingPileQueryParams) => {
    loading.value = true
    try {
      const response: PageResponse<ChargingPile> = await chargingPileApi.getAdminChargingPileList(params)
      chargingPiles.value = response.content
      total.value = response.totalElements
      return response
    } finally {
      loading.value = false
    }
  }

  // 查询充电桩详情
  const fetchChargingPileDetail = async (id: number) => {
    loading.value = true
    try {
      const response = await chargingPileApi.getAdminChargingPileDetail(id)
      currentChargingPile.value = response
      return response
    } finally {
      loading.value = false
    }
  }

  // 创建充电桩
  const createChargingPile = async (data: ChargingPileCreateRequest) => {
    loading.value = true
    try {
      const response = await chargingPileApi.createChargingPile(data)
      return response
    } finally {
      loading.value = false
    }
  }

  // 更新充电桩
  const updateChargingPile = async (id: number, data: ChargingPileUpdateRequest) => {
    loading.value = true
    try {
      const response = await chargingPileApi.updateChargingPile(id, data)
      return response
    } finally {
      loading.value = false
    }
  }

  // 删除充电桩
  const deleteChargingPile = async (id: number) => {
    loading.value = true
    try {
      await chargingPileApi.deleteChargingPile(id)
    } finally {
      loading.value = false
    }
  }

  // 更新充电桩状态
  const updateChargingPileStatus = async (id: number, data: ChargingPileStatusUpdateRequest) => {
    loading.value = true
    try {
      const response = await chargingPileApi.updateChargingPileStatus(id, data)
      return response
    } finally {
      loading.value = false
    }
  }

  // 批量删除充电桩
  const batchDeleteChargingPiles = async (data: BatchDeleteRequest) => {
    loading.value = true
    try {
      const response = await chargingPileApi.batchDeleteChargingPiles(data)
      return response
    } finally {
      loading.value = false
    }
  }

  // 批量导入充电桩
  const importChargingPiles = async (file: File) => {
    loading.value = true
    try {
      const response = await chargingPileApi.importChargingPiles(file)
      return response
    } finally {
      loading.value = false
    }
  }

  // 批量导出充电桩
  const exportChargingPiles = async (params: {
    type?: 'SLOW' | 'FAST' | 'SUPER_FAST'
    status?: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'FAULT'
  }) => {
    loading.value = true
    try {
      const blob = await chargingPileApi.exportChargingPiles(params)

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `charging_piles_${new Date().getTime()}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } finally {
      loading.value = false
    }
  }

  return {
    chargingPiles,
    currentChargingPile,
    total,
    loading,
    fetchChargingPiles,
    fetchChargingPileDetail,
    createChargingPile,
    updateChargingPile,
    deleteChargingPile,
    updateChargingPileStatus,
    batchDeleteChargingPiles,
    importChargingPiles,
    exportChargingPiles
  }
})
