<template>
  <div class="announcement-list-container">
    <el-card class="list-card" v-loading="announcementStore.loading">
      <template #header>
        <div class="card-header">
          <span>系统公告</span>
        </div>
      </template>

      <!-- 公告列表 -->
      <div v-if="announcementStore.clientAnnouncements.length === 0 && !announcementStore.loading" class="empty-state">
        <el-empty description="暂无公告" />
      </div>

      <div v-else class="announcement-list">
        <div
          v-for="announcement in announcementStore.clientAnnouncements"
          :key="announcement.id"
          class="announcement-item"
          @click="handleViewDetail(announcement.id)"
        >
          <div class="announcement-header">
            <h3 class="announcement-title">{{ announcement.title }}</h3>
            <span class="announcement-time">{{ formatRelativeTime(announcement.createdTime) }}</span>
          </div>

          <div class="announcement-summary">
            {{ stripHtmlAndTruncate(announcement.content, 100) }}
          </div>

          <div v-if="announcement.startTime || announcement.endTime" class="announcement-validity">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTimeRange(announcement.startTime, announcement.endTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="announcementStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="announcementStore.currentPage"
          v-model:page-size="announcementStore.pageSize"
          :total="announcementStore.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Clock } from '@element-plus/icons-vue'
import { useAnnouncementStore } from '@/stores/announcement'
import { formatTimeRange, stripHtmlAndTruncate } from '@/types/announcement'

const router = useRouter()
const announcementStore = useAnnouncementStore()

// 格式化相对时间
const formatRelativeTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/announcement/${id}`)
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  announcementStore.pageSize = size
  await announcementStore.fetchClientAnnouncementList()
}

// 页码改变
const handlePageChange = async (page: number) => {
  announcementStore.currentPage = page
  await announcementStore.fetchClientAnnouncementList()
}

// 组件挂载时加载数据
onMounted(async () => {
  await announcementStore.fetchClientAnnouncementList()
})
</script>

<style scoped>
.announcement-list-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.empty-state {
  padding: 60px 0;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  padding: 20px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.announcement-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.announcement-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.announcement-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.announcement-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 12px;
}

.announcement-validity {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
