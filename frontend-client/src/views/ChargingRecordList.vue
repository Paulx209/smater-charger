<template>
  <div class="charging-record-list-container">
    <el-card class="list-card" v-loading="chargingRecordStore.loading">
      <template #header>
        <div class="card-header">
          <span>充电记录</span>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
              @change="handleFilter"
            >
              <el-option
                v-for="(text, status) in ChargingRecordStatusText"
                :key="status"
                :label="text"
                :value="status"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 300px"
              @change="handleDateRangeChange"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 充电记录列表 -->
      <el-table
        :data="chargingRecordStore.chargingRecords"
        stripe
        style="width: 100%"
        class="charging-record-table"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="充电桩" min-width="200">
          <template #default="{ row }">
            <div class="pile-info">
              <div class="pile-name">{{ row.pileName || '未知充电桩' }}</div>
              <div class="pile-location">{{ row.pileLocation || '-' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="车辆" width="120">
          <template #default="{ row }">
            {{ row.vehicleLicensePlate || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>

        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.endTime ? formatDateTime(row.endTime) : '-' }}
          </template>
        </el-table-column>

        <el-table-column label="充电时长" width="120">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>

        <el-table-column label="充电量" width="120">
          <template #default="{ row }">
            {{ formatElectricQuantity(row.electricQuantity) }}
          </template>
        </el-table-column>

        <el-table-column label="费用" width="120">
          <template #default="{ row }">
            <span class="fee-text">{{ formatFee(row.fee) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="ChargingRecordStatusColor[row.status]">
              {{ ChargingRecordStatusText[row.status] }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row.id)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="chargingRecordStore.chargingRecords.length === 0 && !chargingRecordStore.loading" class="empty-state">
        <el-empty description="暂无充电记录" />
      </div>

      <!-- 分页 -->
      <div v-if="chargingRecordStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="chargingRecordStore.currentPage"
          v-model:page-size="chargingRecordStore.pageSize"
          :total="chargingRecordStore.total"
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
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import {
  ChargingRecordStatusText,
  ChargingRecordStatusColor,
  formatDuration,
  formatElectricQuantity,
  formatFee,
  type ChargingRecordStatus
} from '@/types/chargingRecord'

const router = useRouter()
const chargingRecordStore = useChargingRecordStore()

// 筛选表单
const filterForm = reactive({
  status: undefined as ChargingRecordStatus | undefined,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 日期范围改变
const handleDateRangeChange = (value: [string, string] | null) => {
  if (value) {
    filterForm.startDate = value[0]
    filterForm.endDate = value[1]
  } else {
    filterForm.startDate = undefined
    filterForm.endDate = undefined
  }
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/charging-record/${id}`)
}

// 筛选
const handleFilter = async () => {
  try {
    await chargingRecordStore.fetchChargingRecordList({
      status: filterForm.status,
      startDate: filterForm.startDate,
      endDate: filterForm.endDate
    })
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// 重置
const handleReset = async () => {
  filterForm.status = undefined
  filterForm.startDate = undefined
  filterForm.endDate = undefined
  dateRange.value = null
  chargingRecordStore.currentPage = 1
  await handleFilter()
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  chargingRecordStore.pageSize = size
  await handleFilter()
}

// 页码改变
const handlePageChange = async (page: number) => {
  chargingRecordStore.currentPage = page
  await handleFilter()
}

// 组件挂载时加载数据
onMounted(async () => {
  await chargingRecordStore.fetchChargingRecordList()
})
</script>

<style scoped>
.charging-record-list-container {
  padding: 20px;
  max-width: 1800px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
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

.charging-record-table {
  margin-bottom: 20px;
}

.pile-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pile-name {
  font-weight: 600;
  color: #303133;
}

.pile-location {
  font-size: 12px;
  color: #909399;
}

.fee-text {
  font-weight: 600;
  color: #f56c6c;
}

.empty-state {
  padding: 60px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
