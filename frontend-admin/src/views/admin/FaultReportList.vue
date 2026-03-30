<template>
  <div class="fault-report-list-container">
    <el-card class="list-card" v-loading="faultReportStore.loading">
      <template #header>
        <div class="card-header">
          <span>故障报修管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="goStatistics">查看统计</el-button>
          </div>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 160px"
              @change="handleFilter"
            >
              <el-option
                v-for="(text, status) in FaultReportStatusText"
                :key="status"
                :label="text"
                :value="status"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="充电桩 ID">
            <el-input-number
              v-model="filterForm.chargingPileId"
              :min="1"
              :controls="false"
              placeholder="输入充电桩 ID"
            />
          </el-form-item>

          <el-form-item label="创建时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 360px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="faultReportStore.reports" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="pileName" label="充电桩" min-width="180">
          <template #default="{ row }">
            <div>{{ row.pileName || `充电桩 ${row.chargingPileId}` }}</div>
            <div v-if="row.pileLocation" class="sub-text">{{ row.pileLocation }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="140" />
        <el-table-column prop="userPhone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status as FaultReportStatus)">
              {{ getStatusText(row.status as FaultReportStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="故障描述" min-width="280" show-overflow-tooltip />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column prop="updatedTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="faultReportStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="faultReportStore.currentPage"
          v-model:page-size="faultReportStore.pageSize"
          :total="faultReportStore.total"
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
import { useFaultReportStore } from '@/stores/faultReport'
import { FaultReportStatus, FaultReportStatusColor, FaultReportStatusText } from '@/types/faultReport'

const router = useRouter()
const faultReportStore = useFaultReportStore()

const filterForm = reactive({
  chargingPileId: undefined as number | undefined,
  status: undefined as FaultReportStatus | undefined,
  dateRange: null as [string, string] | null
})

const buildQueryParams = () => ({
  chargingPileId: filterForm.chargingPileId,
  status: filterForm.status,
  startDate: filterForm.dateRange?.[0],
  endDate: filterForm.dateRange?.[1]
})

const getStatusTagType = (status: FaultReportStatus) => FaultReportStatusColor[status]

const getStatusText = (status: FaultReportStatus) => FaultReportStatusText[status]

const handleFilter = async () => {
  faultReportStore.currentPage = 1
  await faultReportStore.fetchReportList(buildQueryParams())
}

const handleReset = async () => {
  filterForm.chargingPileId = undefined
  filterForm.status = undefined
  filterForm.dateRange = null
  faultReportStore.currentPage = 1
  await faultReportStore.fetchReportList()
}

const handleSizeChange = async (size: number) => {
  faultReportStore.pageSize = size
  await faultReportStore.fetchReportList(buildQueryParams())
}

const handlePageChange = async (page: number) => {
  faultReportStore.currentPage = page
  await faultReportStore.fetchReportList(buildQueryParams())
}

const handleView = (id: number) => {
  router.push(`/fault-reports/${id}`)
}

const goStatistics = () => {
  router.push('/fault-reports/statistics')
}

onMounted(async () => {
  await faultReportStore.fetchReportList()
})
</script>

<style scoped>
.fault-report-list-container {
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

.header-actions {
  display: flex;
  gap: 12px;
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
