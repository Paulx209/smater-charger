<template>
  <div class="warning-notice-detail-container" v-loading="warningNoticeStore.loading">
    <el-card v-if="notice" class="detail-card">
      <template #header>
        <div class="card-header">
          <span>预警通知详情</span>
          <div class="header-actions">
            <el-button @click="handleBack">返回列表</el-button>
            <el-button type="danger" @click="handleDelete">删除通知</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="通知 ID">{{ notice.id }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ getWarningNoticeUserLabel(notice) }}</el-descriptions-item>
        <el-descriptions-item label="通知类型">
          <el-tag :type="getWarningNoticeTypeTagType(notice.type)">
            {{ getWarningNoticeTypeText(notice.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="读取状态">
          <el-tag :type="getReadStatusTagType(notice.isRead)">
            {{ formatReadStatus(notice.isRead) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送状态">
          <el-tag :type="getSendStatusTagType(notice.sendStatus)">
            {{ getSendStatusText(notice.sendStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(notice.createdTime) }}</el-descriptions-item>
        <el-descriptions-item label="关联充电桩">
          {{ notice.pileName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="充电桩位置">
          {{ notice.pileLocation || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="关联充电记录">
          {{ notice.chargingRecordId ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="超时分钟数">
          {{ notice.overtimeMinutes ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="通知内容" :span="2">
          <div class="content-box">{{ notice.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-empty v-else description="未找到预警通知详情" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useWarningNoticeStore } from '@/stores/warningNotice'
import {
  formatDateTime,
  formatReadStatus,
  getReadStatusTagType,
  getSendStatusTagType,
  getSendStatusText,
  getWarningNoticeTypeTagType,
  getWarningNoticeTypeText,
  getWarningNoticeUserLabel
} from '@/types/warningNotice'
import { navigateBack } from '@/utils/navigation'

const route = useRoute()
const router = useRouter()
const warningNoticeStore = useWarningNoticeStore()

const noticeId = computed(() => Number(route.params.id))
const notice = computed(() => warningNoticeStore.currentNotice)

const loadDetail = async () => {
  await warningNoticeStore.fetchWarningNoticeDetail(noticeId.value)
}

const handleBack = () => {
  navigateBack(router, '/warning-notices')
}

const handleDelete = async () => {
  if (!notice.value) return

  await ElMessageBox.confirm('删除后不可恢复，确认继续吗？', '删除预警通知', {
    type: 'warning'
  })
  await warningNoticeStore.removeWarningNotice(notice.value.id)
  router.push('/warning-notices')
}

onMounted(loadDetail)
</script>

<style scoped>
.warning-notice-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-card {
  min-height: 420px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.content-box {
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
