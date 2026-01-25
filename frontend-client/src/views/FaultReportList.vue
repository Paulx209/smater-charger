<template>
  <div class="fault-report-list-container">
    <el-card class="list-card" v-loading="faultReportStore.loading">
      <template #header>
        <div class="card-header">
          <span>我的报修</span>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="故障类型">
            <el-select
              v-model="filterForm.faultType"
              placeholder="全部类型"
              clearable
              style="width: 180px"
              @change="handleFilter"
            >
              <el-option
                v-for="(text, type) in FaultTypeText"
                :key="type"
                :label="text"
                :value="type"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="处理状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
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

      <!-- 报修列表 -->
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
          <div class="report-icon">
            <el-icon :size="24" :color="getReportColor(report.status)">
              <component :is="getReportIcon(report.faultType)" />
            </el-icon>
          </div>

          <div class="report-content">
            <div class="report-header">
              <el-tag :type="FaultTypeColor[report.faultType]" size="small">
                {{ FaultTypeText[report.faultType] }}
              </el-tag>
              <el-tag :type="FaultReportStatusColor[report.status]" size="small">
                {{ FaultReportStatusText[report.status] }}
              </el-tag>
              <span class="report-time">{{ formatRelativeTime(report.reportTime) }}</span>
            </div>

            <div class="report-pile">
              <el-icon><Location /></el-icon>
              <span class="pile-name">{{ report.pileName }}</span>
              <span v-if="report.pileLocation" class="pile-location">{{ report.pileLocation }}</span>
            </div>

            <div class="report-description">{{ report.description }}</div>

            <div v-if="report.images && report.images.length > 0" class="report-images">
              <el-image
                v-for="(image, index) in report.images.slice(0, 3)"
                :key="index"
                :src="image"
                :preview-src-list="report.images"
                :initial-index="index"
                fit="cover"
                class="report-image"
                @click.stop
              />
            </div>

            <div v-if="report.processNote || report.resolveNote" class="report-notes">
              <div v-if="report.processNote" class="note-item">
                <strong>处理说明：</strong>{{ report.processNote }}
              </div>
              <div v-if="report.resolveNote" class="note-item">
                <strong>解决说明：</strong>{{ report.resolveNote }}
              </div>
            </div>
          </div>

          <div class="report-actions">
            <el-button
              type="primary"
              size="small"
              @click.stop="handleViewDetail(report.id)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="report.status === FaultReportStatus.PENDING"
              type="danger"
              size="small"
              @click.stop="handleDelete(report.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
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
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Warning,
  Tools,
  Monitor,
  Connection,
  CreditCard,
  QuestionFilled,
  Location
} from '@element-plus/icons-vue'
import { useFaultReportStore } from '@/stores/faultReport'
import {
  FaultType,
  FaultTypeText,
  FaultTypeColor,
  FaultReportStatus,
  FaultReportStatusText,
  FaultReportStatusColor,
  formatRelativeTime
} from '@/types/faultReport'

const router = useRouter()
const faultReportStore = useFaultReportStore()

// 筛选表单
const filterForm = reactive({
  faultType: undefined as FaultType | undefined,
  status: undefined as FaultReportStatus | undefined
})

// 获取报修图标
const getReportIcon = (type: FaultType) => {
  const iconMap = {
    [FaultType.CANNOT_CHARGE]: Warning,
    [FaultType.SLOW_CHARGING]: Tools,
    [FaultType.DISPLAY_ERROR]: Monitor,
    [FaultType.CONNECTOR_DAMAGED]: Connection,
    [FaultType.PAYMENT_FAILED]: CreditCard,
    [FaultType.OTHER]: QuestionFilled
  }
  return iconMap[type] || QuestionFilled
}

// 获取报修颜色
const getReportColor = (status: FaultReportStatus) => {
  const colorMap = {
    [FaultReportStatus.PENDING]: '#909399',
    [FaultReportStatus.PROCESSING]: '#e6a23c',
    [FaultReportStatus.RESOLVED]: '#67c23a',
    [FaultReportStatus.CLOSED]: '#909399'
  }
  return colorMap[status] || '#909399'
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/fault-reports/${id}`)
}

// 删除报修
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条报修记录吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await faultReportStore.removeReport(id)
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('删除报修失败:', error)
  }
}

// 筛选
const handleFilter = async () => {
  try {
    await faultReportStore.fetchMyReportList({
      faultType: filterForm.faultType,
      status: filterForm.status
    })
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// 重置
const handleReset = async () => {
  filterForm.faultType = undefined
  filterForm.status = undefined
  faultReportStore.currentPage = 1
  await handleFilter()
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  faultReportStore.pageSize = size
  await handleFilter()
}

// 页码改变
const handlePageChange = async (page: number) => {
  faultReportStore.currentPage = page
  await handleFilter()
}

// 组件挂载时加载数据
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

.empty-state {
  padding: 60px 0;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.report-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.report-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.report-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background-color: #f5f7fa;
  border-radius: 50%;
}

.report-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.report-time {
  font-size: 12px;
  color: #909399;
}

.report-pile {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.pile-name {
  font-weight: 600;
}

.pile-location {
  color: #909399;
}

.report-description {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.report-images {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.report-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}

.report-notes {
  margin-top: 8px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}

.note-item {
  margin-bottom: 8px;
  line-height: 1.6;
}

.note-item:last-child {
  margin-bottom: 0;
}

.note-item strong {
  color: #303133;
}

.report-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
