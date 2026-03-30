<template>
  <div class="charging-record-list-container">
    <el-card class="list-card" v-loading="chargingRecordStore.loading">
      <template #header>
        <div class="card-header">
          <span>充电记录管理</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 160px">
              <el-option
                v-for="(text, status) in ChargingRecordStatusText"
                :key="status"
                :label="text"
                :value="status"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="用户 ID">
            <el-input-number
              v-model="filterForm.userId"
              :min="1"
              :controls="false"
              placeholder="输入用户 ID"
            />
          </el-form-item>

          <el-form-item label="充电桩 ID">
            <el-input-number
              v-model="filterForm.chargingPileId"
              :min="1"
              :controls="false"
              placeholder="输入充电桩 ID"
            />
          </el-form-item>

          <el-form-item label="开始日期">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              range-separator="至"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="chargingRecordStore.records" stripe style="width: 100%">
        <el-table-column prop="id" label="记录 ID" width="90" />
        <el-table-column label="用户" width="100">
          <template #default="{ row }">#{{ row.userId }}</template>
        </el-table-column>
        <el-table-column label="充电桩" min-width="220">
          <template #default="{ row }">
            <div>{{ row.pileName || row.chargingPileCode || `充电桩 ${row.chargingPileId}` }}</div>
            <div class="sub-text">{{ row.pileLocation || row.chargingPileLocation || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="车辆" min-width="140">
          <template #default="{ row }">{{ row.vehicleLicensePlate || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getChargingRecordStatusTagType(row.status)">
              {{ getChargingRecordStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="充电量" width="120">
          <template #default="{ row }">{{ formatElectricity(row.electricQuantity) }}</template>
        </el-table-column>
        <el-table-column label="费用" width="120">
          <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

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
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import {
  ChargingRecordStatusText,
  formatDateTime,
  formatElectricity,
  formatMoney,
  getChargingRecordStatusTagType,
  getChargingRecordStatusText,
  type ChargingRecordQueryParams,
  type ChargingRecordStatus
} from '@/types/chargingRecord'

const router = useRouter()
const chargingRecordStore = useChargingRecordStore()

const filterForm = reactive({
  userId: undefined as number | undefined,
  chargingPileId: undefined as number | undefined,
  status: undefined as ChargingRecordStatus | undefined,
  dateRange: null as [string, string] | null
})

const buildQueryParams = (): Omit<ChargingRecordQueryParams, 'page' | 'size'> => ({
  userId: filterForm.userId,
  chargingPileId: filterForm.chargingPileId,
  status: filterForm.status,
  startDate: filterForm.dateRange?.[0],
  endDate: filterForm.dateRange?.[1]
})

const handleFilter = async () => {
  chargingRecordStore.currentPage = 1
  await chargingRecordStore.fetchRecordList(buildQueryParams())
}

const handleReset = async () => {
  filterForm.userId = undefined
  filterForm.chargingPileId = undefined
  filterForm.status = undefined
  filterForm.dateRange = null
  chargingRecordStore.reset()
  await chargingRecordStore.fetchRecordList({})
}

const handleSizeChange = async (size: number) => {
  chargingRecordStore.pageSize = size
  await chargingRecordStore.fetchRecordList(buildQueryParams())
}

const handlePageChange = async (page: number) => {
  chargingRecordStore.currentPage = page
  await chargingRecordStore.fetchRecordList(buildQueryParams())
}

const handleView = (id: number) => {
  router.push(`/charging-records/${id}`)
}

onMounted(async () => {
  await chargingRecordStore.fetchRecordList({})
})
</script>

<style scoped>
.charging-record-list-container {
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