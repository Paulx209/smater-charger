<template>
  <div class="charging-record-list-container">
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>充电记录</span>
        </div>
      </template>

      <!-- 加载骨架屏 -->
      <template v-if="chargingRecordStore.loading && chargingRecordStore.chargingRecords.length === 0">
        <div class="skeleton-container">
          <el-skeleton :rows="1" animated class="filter-skeleton" />
          <el-skeleton :rows="8" animated class="table-skeleton" />
        </div>
      </template>

      <!-- 错误状态 -->
      <template v-else-if="error">
        <el-result icon="error" title="加载失败" :sub-title="error">
          <template #extra>
            <el-button type="primary" @click="handleRetry">重试</el-button>
          </template>
        </el-result>
      </template>

      <!-- 正常内容 -->
      <template v-else>
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

        <!-- 桌面端表格视图 -->
        <el-table
          v-if="!isMobile"
          :data="chargingRecordStore.chargingRecords"
          stripe
          style="width: 100%"
          class="charging-record-table"
          v-loading="chargingRecordStore.loading"
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

        <!-- 移动端卡片视图 -->
        <div v-else class="mobile-card-list" v-loading="chargingRecordStore.loading">
          <el-card
            v-for="record in chargingRecordStore.chargingRecords"
            :key="record.id"
            class="mobile-record-card"
            shadow="hover"
            @click="handleViewDetail(record.id)"
          >
            <div class="mobile-card-header">
              <el-tag :type="ChargingRecordStatusColor[record.status]" size="small">
                {{ ChargingRecordStatusText[record.status] }}
              </el-tag>
              <span class="mobile-record-id">#{{ record.id }}</span>
            </div>
            <div class="mobile-card-body">
              <div class="mobile-info-row">
                <span class="mobile-label">充电桩</span>
                <span class="mobile-value">{{ record.pileName || '未知充电桩' }}</span>
              </div>
              <div class="mobile-info-row">
                <span class="mobile-label">位置</span>
                <span class="mobile-value">{{ record.pileLocation || '-' }}</span>
              </div>
              <div class="mobile-info-row">
                <span class="mobile-label">车辆</span>
                <span class="mobile-value">{{ record.vehicleLicensePlate || '-' }}</span>
              </div>
              <div class="mobile-info-row">
                <span class="mobile-label">开始时间</span>
                <span class="mobile-value">{{ formatDateTime(record.startTime) }}</span>
              </div>
              <div class="mobile-info-row">
                <span class="mobile-label">充电时长</span>
                <span class="mobile-value">{{ formatDuration(record.duration) }}</span>
              </div>
              <div class="mobile-info-row">
                <span class="mobile-label">充电量</span>
                <span class="mobile-value">{{ formatElectricQuantity(record.electricQuantity) }}</span>
              </div>
              <div class="mobile-info-row">
                <span class="mobile-label">费用</span>
                <span class="mobile-value fee-text">{{ formatFee(record.fee) }}</span>
              </div>
            </div>
          </el-card>
        </div>

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
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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

// 错误状态
const error = ref<string | null>(null)

// 响应式布局
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
}

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
    error.value = null
    await chargingRecordStore.fetchChargingRecordList({
      status: filterForm.status,
      startDate: filterForm.startDate,
      endDate: filterForm.endDate
    })
  } catch (err: any) {
    error.value = err.message || '查询失败，请稍后重试'
    ElMessage.error(error.value)
  }
}

// 重试
const handleRetry = async () => {
  await handleFilter()
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
  window.addEventListener('resize', handleResize)
  try {
    error.value = null
    await chargingRecordStore.fetchChargingRecordList()
  } catch (err: any) {
    error.value = err.message || '加载失败，请稍后重试'
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
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

.skeleton-container {
  padding: 20px 0;
}

.filter-skeleton {
  margin-bottom: 20px;
}

.table-skeleton {
  margin-top: 20px;
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

/* 移动端样式 */
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-record-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.mobile-record-card:active {
  transform: scale(0.98);
}

.mobile-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.mobile-record-id {
  font-size: 14px;
  color: #909399;
}

.mobile-card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.mobile-label {
  color: #909399;
  flex-shrink: 0;
  margin-right: 12px;
}

.mobile-value {
  color: #303133;
  text-align: right;
  word-break: break-all;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .charging-record-list-container {
    padding: 12px;
  }

  .filter-bar {
    padding: 12px;
  }

  .filter-form {
    display: flex;
    flex-direction: column;
  }

  .filter-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .filter-form :deep(.el-select),
  .filter-form :deep(.el-date-editor) {
    width: 100% !important;
  }

  .pagination {
    overflow-x: auto;
  }

  .pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
