<template>
  <div class="vehicle-list-container">
    <el-card class="list-card" v-loading="vehicleStore.loading">
      <template #header>
        <div class="card-header">
          <span>我的车辆</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加车辆
          </el-button>
        </div>
      </template>

      <div v-if="vehicleStore.vehicles.length === 0" class="empty-state">
        <el-empty description="暂无车辆数据">
          <el-button type="primary" @click="handleAdd">添加车辆</el-button>
        </el-empty>
      </div>

      <div v-else class="vehicle-grid">
        <el-card
          v-for="vehicle in vehicleStore.vehicles"
          :key="vehicle.id"
          class="vehicle-card"
          shadow="hover"
        >
          <div class="vehicle-header">
            <div class="vehicle-plate">{{ vehicle.licensePlate }}</div>
            <el-tag v-if="vehicle.isDefault === 1" type="success" size="small">
              默认车辆
            </el-tag>
          </div>

          <div class="vehicle-info">
            <div v-if="vehicle.brand" class="info-item">
              <el-icon><Van /></el-icon>
              <span class="info-label">品牌：</span>
              <span class="info-text">{{ vehicle.brand }}</span>
            </div>

            <div v-if="vehicle.model" class="info-item">
              <el-icon><Tickets /></el-icon>
              <span class="info-label">车型：</span>
              <span class="info-text">{{ vehicle.model }}</span>
            </div>

            <div v-if="vehicle.batteryCapacity" class="info-item">
              <el-icon><Lightning /></el-icon>
              <span class="info-label">电池容量：</span>
              <span class="info-text">{{ vehicle.batteryCapacity }} kWh</span>
            </div>
          </div>

          <div class="vehicle-actions">
            <el-button
              v-if="vehicle.isDefault !== 1"
              type="success"
              size="small"
              @click="handleSetDefault(vehicle.id)"
            >
              设为默认
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(vehicle.id)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(vehicle.id, vehicle.licensePlate)"
            >
              删除
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Plus, Van, Tickets, Lightning } from '@element-plus/icons-vue'
import { useVehicleStore } from '@/stores/vehicle'

const router = useRouter()
const vehicleStore = useVehicleStore()

// 添加车辆
const handleAdd = () => {
  router.push('/vehicles/add')
}

// 编辑车辆
const handleEdit = (id: number) => {
  router.push(`/vehicles/${id}/edit`)
}

// 设置默认车辆
const handleSetDefault = async (id: number) => {
  try {
    await vehicleStore.setDefault(id)
  } catch (error) {
    console.error('设置默认车辆失败:', error)
  }
}

// 删除车辆
const handleDelete = async (id: number, licensePlate: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除车辆 ${licensePlate} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await vehicleStore.removeVehicle(id)
  } catch (error) {
    // 用户取消删除
    if (error === 'cancel') {
      return
    }
    console.error('删除车辆失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  await vehicleStore.fetchMyVehicles()
})
</script>

<style scoped>
.vehicle-list-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 60px 0;
}

.vehicle-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.vehicle-card {
  transition: all 0.3s;
}

.vehicle-card:hover {
  transform: translateY(-4px);
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.vehicle-plate {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.vehicle-info {
  margin-bottom: 16px;
  min-height: 80px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.info-item .el-icon {
  margin-right: 8px;
  color: #909399;
}

.info-label {
  font-weight: 500;
  margin-right: 4px;
}

.info-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vehicle-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
