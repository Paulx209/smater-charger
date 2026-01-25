<template>
  <div class="fault-report-detail-container">
    <el-card v-loading="faultReportStore.loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span class="header-title">报修详情</span>
        </div>
      </template>

      <div v-if="faultReportStore.currentReport" class="detail-content">
        <!-- 状态信息 -->
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

        <!-- 基本信息 -->
        <div class="section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="报修编号">
              <span class="report-id">#{{ faultReportStore.currentReport.id }}</span>
            </el-descriptions-item>

            <el-descriptions-item label="故障类型">
              <el-tag :type="FaultTypeColor[faultReportStore.currentReport.faultType]">
                {{ FaultTypeText[faultReportStore.currentReport.faultType] }}
              </el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="报修时间" :span="2">
              {{ formatDateTime(faultReportStore.currentReport.reportTime) }}
            </el-descriptions-item>

            <el-descriptions-item label="充电桩" :span="2">
              <div class="pile-info">
                <el-icon><Location /></el-icon>
                <span class="pile-name">{{ faultReportStore.currentReport.pileName }}</span>
                <span v-if="faultReportStore.currentReport.pileLocation" class="pile-location">
                  {{ faultReportStore.currentReport.pileLocation }}
                </span>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 故障描述 -->
        <div class="section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>故障描述</span>
          </div>

          <div class="description-content">
            {{ faultReportStore.currentReport.description }}
          </div>
        </div>

        <!-- 故障图片 -->
        <div v-if="faultReportStore.currentReport.images && faultReportStore.currentReport.images.length > 0" class="section">
          <div class="section-title">
            <el-icon><Picture /></el-icon>
            <span>故障图片</span>
          </div>

          <div class="images-grid">
            <el-image
              v-for="(image, index) in faultReportStore.currentReport.images"
              :key="index"
              :src="image"
              :preview-src-list="faultReportStore.currentReport.images"
              :initial-index="index"
              fit="cover"
              class="fault-image"
            />
          </div>
        </div>

        <!-- 处理信息 -->
        <div v-if="faultReportStore.currentReport.processNote || faultReportStore.currentReport.resolveNote" class="section">
          <div class="section-title">
            <el-icon><ChatLineSquare /></el-icon>
            <span>处理信息</span>
          </div>

          <el-timeline>
            <el-timeline-item
              v-if="faultReportStore.currentReport.processNote"
              :timestamp="formatDateTime(faultReportStore.currentReport.processTime!)"
              placement="top"
            >
              <el-card>
                <h4>处理中</h4>
                <p>{{ faultReportStore.currentReport.processNote }}</p>
                <p v-if="faultReportStore.currentReport.processorName" class="processor">
                  处理人：{{ faultReportStore.currentReport.processorName }}
                </p>
              </el-card>
            </el-timeline-item>

            <el-timeline-item
              v-if="faultReportStore.currentReport.resolveNote"
              :timestamp="formatDateTime(faultReportStore.currentReport.resolveTime!)"
              placement="top"
            >
              <el-card>
                <h4>已解决</h4>
                <p>{{ faultReportStore.currentReport.resolveNote }}</p>
                <p v-if="faultReportStore.currentReport.processorName" class="processor">
                  处理人：{{ faultReportStore.currentReport.processorName }}
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 操作按钮 -->
        <div class="section">
          <div class="action-buttons">
            <el-button
              v-if="faultReportStore.currentReport.status === FaultReportStatus.PENDING"
              type="danger"
              @click="handleDelete"
            >
              删除报修
            </el-button>
            <el-button @click="handleBack">
              返回列表
            </el-button>
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  InfoFilled,
  Location,
  Document,
  Picture,
  ChatLineSquare,
  Clock,
  Tools,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import { useFaultReportStore } from '@/stores/faultReport'
import {
  FaultType,
  FaultTypeText,
  FaultTypeColor,
  FaultReportStatus,
  FaultReportStatusText
} from '@/types/faultReport'

const router = useRouter()
const route = useRoute()
const faultReportStore = useFaultReportStore()

// 返回列表
const handleBack = () => {
  router.back()
}

// 格式化日期时间
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

// 获取状态图标
const getStatusIcon = (status: FaultReportStatus) => {
  const iconMap = {
    [FaultReportStatus.PENDING]: Clock,
    [FaultReportStatus.PROCESSING]: Tools,
    [FaultReportStatus.RESOLVED]: CircleCheck,
    [FaultReportStatus.CLOSED]: CircleClose
  }
  return iconMap[status] || Clock
}

// 获取状态描述
const getStatusDescription = (status: FaultReportStatus) => {
  const descMap = {
    [FaultReportStatus.PENDING]: '您的报修已提交，我们会尽快处理',
    [FaultReportStatus.PROCESSING]: '维修人员正在处理您的报修',
    [FaultReportStatus.RESOLVED]: '故障已解决，感谢您的反馈',
    [FaultReportStatus.CLOSED]: '报修已关闭'
  }
  return descMap[status] || ''
}

// 删除报修
const handleDelete = async () => {
  if (!faultReportStore.currentReport) return

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

    await faultReportStore.removeReport(faultReportStore.currentReport.id)
    ElMessage.success('删除成功')
    router.push('/fault-reports')
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('删除报修失败:', error)
  }
}

// 组件挂载时获取报修详情
onMounted(async () => {
  const id = Number(route.params.id)

  if (!id || isNaN(id)) {
    ElMessage.error('报修ID无效')
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
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.status-banner.status-pending {
  background: linear-gradient(135deg, #909399 0%, #606266 100%);
}

.status-banner.status-processing {
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
}

.status-banner.status-resolved {
  background: linear-gradient(135deg, #67c23a 0%, #409eff 100%);
}

.status-banner.status-closed {
  background: linear-gradient(135deg, #909399 0%, #606266 100%);
}

.status-text h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.status-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.report-id {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.pile-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pile-name {
  font-weight: 600;
  color: #303133;
}

.pile-location {
  color: #909399;
}

.description-content {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.fault-image {
  width: 100%;
  height: 200px;
  border-radius: 4px;
  cursor: pointer;
}

.el-timeline {
  padding-left: 0;
}

.el-timeline-item h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.el-timeline-item p {
  margin: 0 0 8px 0;
  line-height: 1.6;
  color: #606266;
}

.el-timeline-item p:last-child {
  margin-bottom: 0;
}

.processor {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.empty-state {
  padding: 60px 0;
}
</style>
