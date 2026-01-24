<template>
  <div class="warning-notice-list-container">
    <el-card class="list-card" v-loading="warningNoticeStore.loading">
      <template #header>
        <div class="card-header">
          <span>预警通知</span>
          <el-button
            v-if="warningNoticeStore.unreadCount > 0"
            type="primary"
            @click="handleMarkAllAsRead"
          >
            全部标记为已读
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="通知类型">
            <el-select
              v-model="filterForm.type"
              placeholder="全部类型"
              clearable
              style="width: 180px"
              @change="handleFilter"
            >
              <el-option
                v-for="(text, type) in WarningNoticeTypeText"
                :key="type"
                :label="text"
                :value="type"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="阅读状态">
            <el-select
              v-model="filterForm.isRead"
              placeholder="全部状态"
              clearable
              style="width: 150px"
              @change="handleFilter"
            >
              <el-option label="未读" :value="0" />
              <el-option label="已读" :value="1" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 通知列表 -->
      <div v-if="warningNoticeStore.notices.length === 0 && !warningNoticeStore.loading" class="empty-state">
        <el-empty description="暂无通知" />
      </div>

      <div v-else class="notice-list">
        <div
          v-for="notice in warningNoticeStore.notices"
          :key="notice.id"
          class="notice-item"
          :class="{ 'unread': notice.isRead === 0 }"
          @click="handleNoticeClick(notice)"
        >
          <div class="notice-icon">
            <el-icon :size="24" :color="getNoticeColor(notice.type)">
              <component :is="getNoticeIcon(notice.type)" />
            </el-icon>
          </div>

          <div class="notice-content">
            <div class="notice-header">
              <el-tag :type="WarningNoticeTypeColor[notice.type]" size="small">
                {{ WarningNoticeTypeText[notice.type] }}
              </el-tag>
              <span class="notice-time">{{ formatRelativeTime(notice.createdTime) }}</span>
            </div>

            <div class="notice-text">{{ notice.content }}</div>

            <div v-if="notice.pileName" class="notice-location">
              <el-icon><Location /></el-icon>
              <span>{{ notice.pileName }}</span>
              <span v-if="notice.pileLocation" class="location-detail">{{ notice.pileLocation }}</span>
            </div>
          </div>

          <div class="notice-actions">
            <el-button
              v-if="notice.isRead === 0"
              type="primary"
              size="small"
              @click.stop="handleMarkAsRead(notice.id)"
            >
              标记已读
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click.stop="handleDelete(notice.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="warningNoticeStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="warningNoticeStore.currentPage"
          v-model:page-size="warningNoticeStore.pageSize"
          :total="warningNoticeStore.total"
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
import { reactive, onMounted, onUnmounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import {
  Warning,
  Bell,
  InfoFilled,
  CircleClose,
  Location
} from '@element-plus/icons-vue'
import { useWarningNoticeStore } from '@/stores/warningNotice'
import {
  WarningNoticeTypeText,
  WarningNoticeTypeColor,
  formatRelativeTime,
  type WarningNoticeType,
  type WarningNoticeInfo
} from '@/types/warningNotice'

const warningNoticeStore = useWarningNoticeStore()

// 筛选表单
const filterForm = reactive({
  type: undefined as WarningNoticeType | undefined,
  isRead: undefined as number | undefined
})

// 轮询定时器
let pollingTimer: number | null = null

// 获取通知图标
const getNoticeIcon = (type: WarningNoticeType) => {
  const iconMap = {
    IDLE_REMINDER: InfoFilled,
    OVERTIME_WARNING: Warning,
    FAULT_NOTICE: CircleClose,
    RESERVATION_REMINDER: Bell
  }
  return iconMap[type] || InfoFilled
}

// 获取通知颜色
const getNoticeColor = (type: WarningNoticeType) => {
  const colorMap = {
    IDLE_REMINDER: '#909399',
    OVERTIME_WARNING: '#e6a23c',
    FAULT_NOTICE: '#f56c6c',
    RESERVATION_REMINDER: '#67c23a'
  }
  return colorMap[type] || '#909399'
}

// 通知点击
const handleNoticeClick = async (notice: WarningNoticeInfo) => {
  // 如果未读，标记为已读
  if (notice.isRead === 0) {
    await warningNoticeStore.markNoticeAsRead(notice.id)
  }
}

// 标记已读
const handleMarkAsRead = async (id: number) => {
  try {
    await warningNoticeStore.markNoticeAsRead(id)
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要将所有通知标记为已读吗？',
      '确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await warningNoticeStore.markAllNoticesAsRead()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('全部标记为已读失败:', error)
  }
}

// 删除通知
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条通知吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await warningNoticeStore.removeNotice(id)
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('删除通知失败:', error)
  }
}

// 筛选
const handleFilter = async () => {
  try {
    await warningNoticeStore.fetchNoticeList({
      type: filterForm.type,
      isRead: filterForm.isRead
    })
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// 重置
const handleReset = async () => {
  filterForm.type = undefined
  filterForm.isRead = undefined
  warningNoticeStore.currentPage = 1
  await handleFilter()
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  warningNoticeStore.pageSize = size
  await handleFilter()
}

// 页码改变
const handlePageChange = async (page: number) => {
  warningNoticeStore.currentPage = page
  await handleFilter()
}

// 启动轮询
const startPolling = () => {
  // 每30秒更新一次未读数量
  pollingTimer = window.setInterval(() => {
    warningNoticeStore.fetchUnreadCount()
  }, 30000)
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  await warningNoticeStore.fetchNoticeList()
  await warningNoticeStore.fetchUnreadCount()
  startPolling()
})

// 组件卸载时停止轮询
onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.warning-notice-list-container {
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

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.notice-item.unread {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.notice-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background-color: #f5f7fa;
  border-radius: 50%;
}

.notice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.notice-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.notice-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.location-detail {
  color: #909399;
}

.notice-actions {
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
