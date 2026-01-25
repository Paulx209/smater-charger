<template>
  <el-card class="announcement-carousel-card">
    <el-carousel
      v-if="announcements.length > 0"
      height="150px"
      :interval="4000"
      arrow="hover"
      indicator-position="outside"
    >
      <el-carousel-item v-for="announcement in announcements" :key="announcement.id">
        <div class="carousel-item-content" @click="handleClick(announcement.id)">
          <div class="announcement-icon">
            <el-icon :size="40" color="#409eff">
              <Bell />
            </el-icon>
          </div>
          <div class="announcement-text">
            <h3 class="announcement-title">{{ announcement.title }}</h3>
            <p class="announcement-summary">{{ stripHtmlAndTruncate(announcement.content, 80) }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div v-else class="no-announcement">
      <el-icon :size="40" color="#909399">
        <Bell />
      </el-icon>
      <p>暂无系统公告</p>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { useAnnouncementStore } from '@/stores/announcement'
import { stripHtmlAndTruncate, type AnnouncementClientInfo } from '@/types/announcement'

const router = useRouter()
const announcementStore = useAnnouncementStore()

const announcements = ref<AnnouncementClientInfo[]>([])

// 点击公告
const handleClick = (id: number) => {
  router.push(`/announcement/${id}`)
}

// 加载最新公告
const loadLatestAnnouncements = async () => {
  try {
    const result = await announcementStore.fetchLatestAnnouncements(3)
    announcements.value = result
  } catch (error) {
    console.error('加载最新公告失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadLatestAnnouncements()
})
</script>

<style scoped>
.announcement-carousel-card {
  margin-bottom: 30px;
}

.carousel-item-content {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
  padding: 20px 30px;
  cursor: pointer;
  transition: all 0.3s;
}

.carousel-item-content:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.announcement-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
}

.announcement-icon .el-icon {
  color: white;
}

.announcement-text {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-summary {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.no-announcement {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 150px;
  color: #909399;
}

.no-announcement p {
  margin: 10px 0 0 0;
  font-size: 14px;
}

:deep(.el-carousel__indicator) {
  background-color: rgba(0, 0, 0, 0.2);
}

:deep(.el-carousel__indicator.is-active) {
  background-color: #409eff;
}
</style>
