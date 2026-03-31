<template>
  <div class="vehicle-list-container">
    <el-card class="list-card" v-loading="vehicleStore.loading">
      <template #header>
        <div class="card-header">
          <span>车辆管理</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="用户 ID">
            <el-input-number
              v-model="filterForm.userId"
              :min="1"
              :controls="false"
              placeholder="请输入用户 ID"
            />
          </el-form-item>

          <el-form-item label="车牌号">
            <el-input
              v-model="filterForm.licensePlate"
              clearable
              placeholder="请输入车牌号关键字"
              style="width: 220px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="vehicleStore.vehicles" stripe style="width: 100%">
        <el-table-column prop="id" label="车辆 ID" width="100" />
        <el-table-column prop="licensePlate" label="车牌号" min-width="160" />
        <el-table-column label="所属用户" min-width="180">
          <template #default="{ row }">
            <div>{{ getVehicleOwnerLabel(row) }}</div>
            <div class="sub-text">用户 #{{ row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="品牌/型号" min-width="180">
          <template #default="{ row }">
            <div>{{ row.brand || '-' }}</div>
            <div class="sub-text">{{ row.model || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="电池容量" width="120">
          <template #default="{ row }">
            {{ row.batteryCapacity != null ? `${row.batteryCapacity} kWh` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="默认车辆" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isDefault === 1 ? 'success' : 'info'">
              {{ getDefaultStatusText(row.isDefault) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row.id)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="vehicleStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="vehicleStore.currentPage"
          v-model:page-size="vehicleStore.pageSize"
          :total="vehicleStore.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useVehicleStore } from '@/stores/vehicle'
import { formatDateTime, getDefaultStatusText, getVehicleOwnerLabel, type VehicleQueryParams } from '@/types/vehicle'

const router = useRouter()
const vehicleStore = useVehicleStore()

const filterForm = reactive({
  userId: undefined as number | undefined,
  licensePlate: ''
})

const buildQueryParams = (): Omit<VehicleQueryParams, 'page' | 'size'> => ({
  userId: filterForm.userId,
  licensePlate: filterForm.licensePlate.trim() || undefined
})

const reloadList = async () => {
  await vehicleStore.fetchVehicleList(buildQueryParams())
}

const handleFilter = async () => {
  vehicleStore.currentPage = 1
  await reloadList()
}

const handleReset = async () => {
  filterForm.userId = undefined
  filterForm.licensePlate = ''
  vehicleStore.reset()
  await vehicleStore.fetchVehicleList({})
}

const handleSizeChange = async (size: number) => {
  vehicleStore.pageSize = size
  await reloadList()
}

const handlePageChange = async (page: number) => {
  vehicleStore.currentPage = page
  await reloadList()
}

const handleView = (id: number) => {
  router.push(`/vehicles/${id}`)
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('删除车辆后不可恢复，是否继续？', '删除车辆', {
    type: 'warning'
  })
  await vehicleStore.removeVehicle(id)
  await reloadList()
}

onMounted(async () => {
  await vehicleStore.fetchVehicleList({})
})
</script>

<style scoped>
.vehicle-list-container {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.filter-form {
  margin-bottom: 0;
}

.sub-text {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
