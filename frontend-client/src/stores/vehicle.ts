import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import {
  getMyVehicles,
  getVehicleById,
  createVehicle,
  updateVehicle,
  deleteVehicle,
  setDefaultVehicle
} from '@/api/vehicle'
import type { VehicleInfo, VehicleCreateRequest, VehicleUpdateRequest } from '@/types/vehicle'

export const useVehicleStore = defineStore('vehicle', () => {
  // 状态
  const vehicles = ref<VehicleInfo[]>([])
  const currentVehicle = ref<VehicleInfo | null>(null)
  const loading = ref(false)

  // 获取默认车辆
  const defaultVehicle = computed(() => vehicles.value.find(v => v.isDefault === 1))

  /**
   * 查询我的车辆列表
   */
  const fetchMyVehicles = async () => {
    try {
      loading.value = true
      const data = await getMyVehicles()
      vehicles.value = data
      return data
    } catch (error) {
      console.error('查询车辆列表失败:', error)
      ElMessage.error('查询车辆列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取车辆详情
   */
  const fetchVehicleById = async (id: number) => {
    try {
      loading.value = true
      const data = await getVehicleById(id)
      currentVehicle.value = data
      return data
    } catch (error) {
      console.error('获取车辆详情失败:', error)
      ElMessage.error('获取车辆详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加车辆
   */
  const addVehicle = async (data: VehicleCreateRequest) => {
    try {
      loading.value = true
      const result = await createVehicle(data)
      ElMessage.success('添加成功')
      await fetchMyVehicles()
      return result
    } catch (error) {
      console.error('添加车辆失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新车辆
   */
  const modifyVehicle = async (id: number, data: VehicleUpdateRequest) => {
    try {
      loading.value = true
      const result = await updateVehicle(id, data)
      ElMessage.success('更新成功')
      await fetchMyVehicles()
      return result
    } catch (error) {
      console.error('更新车辆失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除车辆
   */
  const removeVehicle = async (id: number) => {
    try {
      loading.value = true
      await deleteVehicle(id)
      ElMessage.success('删除成功')
      await fetchMyVehicles()
    } catch (error) {
      console.error('删除车辆失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置默认车辆
   */
  const setDefault = async (id: number) => {
    try {
      loading.value = true
      await setDefaultVehicle(id)
      ElMessage.success('设置成功')
      await fetchMyVehicles()
    } catch (error) {
      console.error('设置默认车辆失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 清空当前车辆
   */
  const clearCurrentVehicle = () => {
    currentVehicle.value = null
  }

  return {
    vehicles,
    currentVehicle,
    loading,
    defaultVehicle,
    fetchMyVehicles,
    fetchVehicleById,
    addVehicle,
    modifyVehicle,
    removeVehicle,
    setDefault,
    clearCurrentVehicle
  }
})
