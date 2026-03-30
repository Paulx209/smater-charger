<template>
  <div class="fault-report-list-container">
    <el-card class="list-card" v-loading="faultReportStore.loading">
      <template #header>
        <div class="card-header">
          <span>我的报修</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="处理状态">
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

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="faultReportStore.reports.length === 0 && !faultReportStore.loading" class="empty-state">
        <el-empty description="暂无报修记录" />
      </div>

      <div v-else class="report-list">
        <div
          v-for="report in faultReportStore.reports"
          :key="report.id"
          class="report-item"
          @click="handleViewDetail(report.id)"
        >
          <div class="report-main">
            <div class="report-header">
              <span class="report-id">#{{ report.id }}</span>
              <el-tag :type="FaultReportStatusColor[report.status]" size="small">
                {{ FaultReportStatusText[report.status] }}
              </el-tag>
              <span class="report-time">{{ formatRelativeTime(report.createdTime) }}</span>
            </div>

            <div class="report-pile">
              <span class="pile-name">{{ report.pileName || `充电桩 ${report.chargingPileId}` }}</span>
              <span v-if="report.pileLocation" class="pile-location">{{ report.pileLocation }}</span>
            </div>

            <div class="report-description">{{ report.description }}</div>

            <div class="report-meta">
              <span>创建时间：{{ formatDateTime(report.createdTime) }}</span>
              <span>更新时间：{{ formatDateTime(report.updatedTime) }}</span>
            </div>

            <div v-if="report.handleRemark" class="report-remark">
              处理备注：{{ report.handleRemark }}
            </div>
          </div>

          <div class="report-actions">
            <el-button type="primary" size="small" @click.stop="handleViewDetail(report.id)">查看详情</el-button>
            <el-button
              v-if="report.status === FaultReportStatus.PENDING"
              type="danger"
              size="small"
              @click.stop="handleDelete(report.id)"
            >
              取消报修
            </el-button>
          </div>
        </div>
      </div>

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
import { ElMessageBox } from 'element-plus'
import { useFaultReportStore } from '@/stores/faultReport'
import {
  FaultReportStatus,
  FaultReportStatusColor,
  FaultReportStatusText,
  formatRelativeTime
} from '@/types/faultReport'

const router = useRouter()
const faultReportStore = useFaultReportStore()

const filterForm = reactive({
  status: undefined as FaultReportStatus | undefined
})

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

const handleViewDetail = (id: number) => {
  router.push(`/fault-reports/${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认取消这条报修记录吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await faultReportStore.removeReport(id)
  } catch (error) {
    if (error === 'cancel') return
    console.error('取消报修失败:', error)
  }
}

const handleFilter = async () => {
  try {
    faultReportStore.currentPage = 1
    await faultReportStore.fetchMyReportList({ status: filterForm.status })
  } catch (error) {
    console.error('查询失败:', error)
  }
}

const handleReset = async () => {
  filterForm.status = undefined
  faultReportStore.currentPage = 1
  await handleFilter()
}

const handleSizeChange = async (size: number) => {
  faultReportStore.pageSize = size
  await faultReportStore.fetchMyReportList({ status: filterForm.status })
}

const handlePageChange = async (page: number) => {
  faultReportStore.currentPage = page
  await faultReportStore.fetchMyReportList({ status: filterForm.status })
}

onMounted(async () => {
  await faultReportStore.fetchMyReportList()
})
</script>

<style scoped>
.fault-report-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.report-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-item {
  display: flex;
  gap: 16px;
  justify-content: space-between;
  padding: 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.report-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.report-main {
  flex: 1;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.report-id {
  font-weight: 600;
  color: #303133;
}

.report-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.report-pile {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.pile-name {
  font-weight: 600;
  color: #303133;
}

.pile-location {
  font-size: 13px;
  color: #909399;
}

.report-description {
  margin-bottom: 12px;
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap;
}

.report-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #909399;
}

.report-remark {
  padding: 12px 14px;
  color: #606266;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.report-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

.empty-state {
  padding: 48px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>