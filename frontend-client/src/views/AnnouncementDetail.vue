<template>
  <div class="announcement-detail-container">
    <el-card v-loading="announcementStore.loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span class="header-title">公告详情</span>
        </div>
      </template>

      <div v-if="announcement" class="detail-content">
        <!-- 公告标题 -->
        <h1 class="announcement-title">{{ announcement.title }}</h1>

        <!-- 公告元信息 -->
        <div class="announcement-meta">
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span>发布时间：{{ formatDateTime(announcement.createdTime) }}</span>
          </div>
          <div v-if="announcement.startTime || announcement.endTime" class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>有效期：{{ formatTimeRange(announcement.startTime, announcement.endTime) }}</span>
          </div>
        </div>

        <!-- 分隔线 -->
        <el-divider />

        <!-- 公告内容 -->
        <div class="announcement-content" v-html="announcement.content"></div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="公告不存在或已下线" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, Calendar } from '@element-plus/icons-vue'
import { useAnnouncementStore } from '@/stores/announcement'
import { formatTimeRange, type AnnouncementClientInfo } from '@/types/announcement'

const router = useRouter()
const route = useRoute()
const announcementStore = useAnnouncementStore()

// 当前公告
const announcement = computed(() => announcementStore.currentAnnouncement as AnnouncementClientInfo | null)

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
    minute: '2-digit'
  })
}

// 组件挂载时获取公告详情
onMounted(async () => {
  const id = Number(route.params.id)

  if (!id || isNaN(id)) {
    ElMessage.error('公告ID无效')
    router.push('/announcement')
    return
  }

  try {
    await announcementStore.fetchClientAnnouncementDetail(id)
  } catch (error) {
    console.error('获取公告详情失败:', error)
    router.push('/announcement')
  }
})
</script>

<style scoped>
.announcement-detail-container {
  padding: 20px;
  max-width: 900px;
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

.announcement-title {
  margin: 0 0 20px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.announcement-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.meta-item .el-icon {
  color: #909399;
}

.announcement-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

.announcement-content :deep(h1),
.announcement-content :deep(h2),
.announcement-content :deep(h3),
.announcement-content :deep(h4),
.announcement-content :deep(h5),
.announcement-content :deep(h6) {
  margin: 20px 0 10px 0;
  font-weight: 600;
}

.announcement-content :deep(p) {
  margin: 10px 0;
}

.announcement-content :deep(ul),
.announcement-content :deep(ol) {
  margin: 10px 0;
  padding-left: 30px;
}

.announcement-content :deep(li) {
  margin: 5px 0;
}

.announcement-content :deep(blockquote) {
  margin: 15px 0;
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-left: 4px solid #409eff;
}

.announcement-content :deep(code) {
  padding: 2px 6px;
  background-color: #f5f7fa;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.announcement-content :deep(pre) {
  margin: 15px 0;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow-x: auto;
}

.announcement-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 15px 0;
  border-radius: 4px;
}

.announcement-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.announcement-content :deep(a:hover) {
  text-decoration: underline;
}

.empty-state {
  padding: 60px 0;
}
</style>
