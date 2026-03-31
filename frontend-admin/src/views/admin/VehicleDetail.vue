<template>
  <div class="vehicle-detail-container">
    <el-card v-loading="vehicleStore.loading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button :icon="ArrowLeft" @click="handleBack">返回列表</el-button>
            <span class="header-title">车辆详情</span>
          </div>
          <el-button
            v-if="vehicleStore.currentVehicle"
            type="danger"
            @click="handleDelete"
          >
            删除车辆
          </el-button>
        </div>
      </template>

      <div v-if="vehicleStore.currentVehicle" class="detail-content">
        <div class="section">
          <div class="section-title">车辆信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="车辆 ID">#{{ vehicleStore.currentVehicle.id }}</el-descriptions-item>
            <el-descriptions-item label="车牌号">{{ vehicleStore.currentVehicle.licensePlate }}</el-descriptions-item>
            <el-descriptions-item label="品牌">{{ vehicleStore.currentVehicle.brand || '-' }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ vehicleStore.currentVehicle.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="电池容量">
              {{ vehicleStore.currentVehicle.batteryCapacity != null ? `${vehicleStore.currentVehicle.batteryCapacity} kWh` : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="默认车辆">
              <el-tag :type="vehicleStore.currentVehicle.isDefault === 1 ? 'success' : 'info'">
                {{ getDefaultStatusText(vehicleStore.currentVehicle.isDefault) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">所属用户</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户">{{ getVehicleOwnerLabel(vehicleStore.currentVehicle) }}</el-descriptions-item>
            <el-descriptions-item label="用户 ID">#{{ vehicleStore.currentVehicle.userId }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">时间信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="创建时间">{{ formatDateTime(vehicleStore.currentVehicle.createdTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(vehicleStore.currentVehicle.updatedTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="未找到车辆详情" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useVehicleStore } from '@/stores/vehicle'
import { formatDateTime, getDefaultStatusText, getVehicleOwnerLabel } from '@/types/vehicle'

const route = useRoute()
const router = useRouter()
const vehicleStore = useVehicleStore()

const handleBack = () => {
  router.push('/vehicles')
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id || Number.isNaN(id)) {
    ElMessage.error('车辆 ID 无效')
    handleBack()
    return
  }

  try {
    await vehicleStore.fetchVehicleDetail(id)
  } catch (error) {
    console.error('Failed to load vehicle detail:', error)
    handleBack()
  }
}

const handleDelete = async () => {
  if (!vehicleStore.currentVehicle) {
    return
  }

  await ElMessageBox.confirm('删除车辆后不可恢复，是否继续？', '删除车辆', {
    type: 'warning'
  })
  await vehicleStore.removeVehicle(vehicleStore.currentVehicle.id)
  handleBack()
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.vehicle-detail-container {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.empty-state {
  padding: 60px 0;
}
</style>
