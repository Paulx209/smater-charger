<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useWarningNoticeStore } from '@/stores/warningNotice'
import { isAuthenticated } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()
const warningNoticeStore = useWarningNoticeStore()

const isLoggedIn = computed(() => isAuthenticated())
const userInfo = computed(() => userStore.userInfo)
const unreadCount = computed(() => warningNoticeStore.unreadCount)

// 轮询定时器
let pollingTimer: number | null = null

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

// 启动轮询
const startPolling = () => {
  if (isLoggedIn.value) {
    // 立即获取一次未读数量
    warningNoticeStore.fetchUnreadCount()
    // 每30秒更新一次未读数量
    pollingTimer = window.setInterval(() => {
      if (isLoggedIn.value) {
        warningNoticeStore.fetchUnreadCount()
      }
    }, 30000)
  }
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 组件挂载时启动轮询
onMounted(() => {
  startPolling()
})

// 组件卸载时停止轮询
onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="app-container">
    <header v-if="isLoggedIn" class="app-header">
      <div class="header-content">
        <div class="logo">智能充电桩管理系统</div>
        <nav class="nav">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/charging-piles">充电桩查询</RouterLink>
          <RouterLink to="/charging-record">充电记录</RouterLink>
          <RouterLink to="/reservations">我的预约</RouterLink>
          <RouterLink to="/vehicles">车辆管理</RouterLink>
          <RouterLink to="/about">关于</RouterLink>
          <RouterLink to="/profile">个人中心</RouterLink>
        </nav>
        <div class="header-right">
          <RouterLink to="/warning-notice" class="notice-icon">
            <el-badge :value="unreadCount > 99 ? '99+' : unreadCount" :hidden="unreadCount === 0">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </RouterLink>
          <div class="user-info">
            <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav a {
  text-decoration: none;
  color: #333;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav a:hover {
  background-color: #f5f5f5;
}

.nav a.router-link-active {
  color: #409eff;
  background-color: #ecf5ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notice-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
  cursor: pointer;
  color: #606266;
}

.notice-icon:hover {
  background-color: #f5f5f5;
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  color: #666;
  font-size: 14px;
}

.app-main {
  flex: 1;
}
</style>
