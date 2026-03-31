<template>
  <div class="fault-report-detail-container">
    <el-card v-loading="faultReportStore.loading">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
          <span class="header-title">报修详情</span>
        </div>
      </template>

      <div v-if="faultReportStore.currentReport" class="detail-content">
        <div class="section">
          <div class="status-banner" :class="`status-${faultReportStore.currentReport.status.toLowerCase()}`">
            <el-icon :size="32">
              <component :is="getStatusIcon(faultReportStore.currentReport.status)" />
            </el-icon>
            <div class="status-text">
              <h3>{{ FaultReportStatusText[faultReportStore.currentReport.status] }}</h3>
              <p>{{ getStatusDescription(faultReportStore.currentReport.status) }}</p>
            </div>
          </div>
        </div>

        <div class="section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="报修编号">
              <span class="report-id">#{{ faultReportStore.currentReport.id }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="处理状态">
              <el-tag :type="FaultReportStatusColor[faultReportStore.currentReport.status]">
                {{ FaultReportStatusText[faultReportStore.currentReport.status] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="充电桩" :span="2">
              <div class="pile-info">
                <span class="pile-name">{{ faultReportStore.currentReport.pileName || `充电桩 ${faultReportStore.currentReport.chargingPileId}` }}</span>
                <span v-if="faultReportStore.currentReport.pileLocation" class="pile-location">
                  {{ faultReportStore.currentReport.pileLocation }}
                </span>
                <span v-if="faultReportStore.currentReport.pileType" class="pile-type">
                  {{ formatPileType(faultReportStore.currentReport.pileType) }}
                </span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(faultReportStore.currentReport.createdTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDateTime(faultReportStore.currentReport.updatedTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="处理人" :span="2">
              {{ faultReportStore.currentReport.handlerName || '未处理' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>故障描述</span>
          </div>
          <div class="description-content">{{ faultReportStore.currentReport.description }}</div>
        </div>

        <div v-if="faultReportStore.currentReport.handleRemark" class="section">
          <div class="section-title">
            <el-icon><ChatLineSquare /></el-icon>
            <span>处理备注</span>
          </div>
          <div class="description-content remark-content">{{ faultReportStore.currentReport.handleRemark }}</div>
        </div>

        <div class="section">
          <div class="action-buttons">
            <el-button
              v-if="faultReportStore.currentReport.status === FaultReportStatus.PENDING"
              type="danger"
              @click="handleDelete"
            >
              取消报修
            </el-button>
            <el-button @click="handleBack">返回列表</el-button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="报修信息不存在" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ChatLineSquare,
  CircleCheck,
  Clock,
  Document,
  InfoFilled,
  Tools
} from '@element-plus/icons-vue'
import { useFaultReportStore } from '@/stores/faultReport'
import {
  FaultReportStatus,
  FaultReportStatusColor,
  FaultReportStatusText,
  formatPileType
} from '@/types/faultReport'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const route = useRoute()
const faultReportStore = useFaultReportStore()

const handleBack = () => {
  navigateBack(router, '/fault-reports')
}

const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getStatusIcon = (status: FaultReportStatus) => {
  const iconMap = {
    [FaultReportStatus.PENDING]: Clock,
    [FaultReportStatus.PROCESSING]: Tools,
    [FaultReportStatus.RESOLVED]: CircleCheck
  }
  return iconMap[status] || Clock
}

const getStatusDescription = (status: FaultReportStatus) => {
  const descMap = {
    [FaultReportStatus.PENDING]: '报修已提交，系统正在安排处理。',
    [FaultReportStatus.PROCESSING]: '报修正在处理中，请留意最新备注。',
    [FaultReportStatus.RESOLVED]: '报修已解决，感谢你的反馈。'
  }
  return descMap[status] || ''
}

const handleDelete = async () => {
  if (!faultReportStore.currentReport) return

  try {
    await ElMessageBox.confirm('确认取消这条报修记录吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await faultReportStore.removeReport(faultReportStore.currentReport.id)
    ElMessage.success('取消报修成功')
    router.push('/fault-reports')
  } catch (error) {
    if (error === 'cancel') return
    console.error('取消报修失败:', error)
  }
}

onMounted(async () => {
  const id = Number(route.params.id)

  if (!id || Number.isNaN(id)) {
    ElMessage.error('报修 ID 无效')
    router.push('/fault-reports')
    return
  }

  try {
    await faultReportStore.fetchReportDetail(id)
  } catch (error) {
    console.error('获取报修详情失败:', error)
    router.push('/fault-reports')
  }
})
</script>

<style scoped>
.fault-report-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
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
  padding: 20px 0;
}

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  color: #409eff;
}

.status-banner {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-radius: 12px;
  color: #fff;
}

.status-pending {
  background: linear-gradient(135deg, #e6a23c, #f3c46a);
}

.status-processing {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.status-resolved {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.status-text h3 {
  margin: 0 0 6px;
  font-size: 20px;
}

.status-text p {
  margin: 0;
  opacity: 0.92;
}

.report-id {
  font-weight: 600;
  color: #409eff;
}

.pile-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pile-name {
  font-weight: 600;
  color: #303133;
}

.pile-location,
.pile-type {
  color: #909399;
}

.description-content {
  padding: 18px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.remark-content {
  border-left: 4px solid #409eff;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.empty-state {
  padding: 60px 0;
}
</style>
