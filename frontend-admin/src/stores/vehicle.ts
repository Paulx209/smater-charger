import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { deleteAdminVehicle, getAdminVehicleDetail, getAdminVehicleList } from '@/api/vehicle'
import type { VehicleInfo, VehicleQueryParams } from '@/types/vehicle'

export const useVehicleStore = defineStore('vehicleAdmin', () => {
  const loading = ref(false)
  const vehicles = ref<VehicleInfo[]>([])
  const currentVehicle = ref<VehicleInfo | null>(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const lastQuery = ref<Omit<VehicleQueryParams, 'page' | 'size'>>({})

  const fetchVehicleList = async (params?: Omit<VehicleQueryParams, 'page' | 'size'>) => {
    try {
      loading.value = true
      lastQuery.value = {
        ...lastQuery.value,
        ...params
      }

      const response = await getAdminVehicleList({
        ...lastQuery.value,
        page: currentPage.value,
        size: pageSize.value
      })

      vehicles.value = response.content
      total.value = response.totalElements
      currentPage.value = response.number + 1
      pageSize.value = response.size
      return response
    } catch (error) {
      console.error('Failed to fetch vehicle list:', error)
      ElMessage.error('加载车辆列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchVehicleDetail = async (id: number) => {
    try {
      loading.value = true
      const response = await getAdminVehicleDetail(id)
      currentVehicle.value = response
      return response
    } catch (error) {
      console.error('Failed to fetch vehicle detail:', error)
      ElMessage.error('加载车辆详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const removeVehicle = async (id: number) => {
    try {
      loading.value = true
      await deleteAdminVehicle(id)

      vehicles.value = vehicles.value.filter((vehicle) => vehicle.id !== id)
      if (currentVehicle.value?.id === id) {
        currentVehicle.value = null
      }

      ElMessage.success('车辆删除成功')
    } catch (error) {
      console.error('Failed to delete vehicle:', error)
      ElMessage.error('删除车辆失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    vehicles.value = []
    currentVehicle.value = null
    total.value = 0
    currentPage.value = 1
    pageSize.value = 10
    lastQuery.value = {}
  }

  return {
    loading,
    vehicles,
    currentVehicle,
    total,
    currentPage,
    pageSize,
    lastQuery,
    fetchVehicleList,
    fetchVehicleDetail,
    removeVehicle,
    reset
  }
})
