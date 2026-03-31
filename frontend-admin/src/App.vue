<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { isAuthenticated } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => isAuthenticated())
const userInfo = computed(() => userStore.userInfo)

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-container">
    <header v-if="isLoggedIn" class="app-header">
      <div class="header-content">
        <div class="logo">智能充电后台管理系统</div>
        <nav class="nav">
          <RouterLink to="/price-config">价格配置</RouterLink>
          <RouterLink to="/reservations">预约管理</RouterLink>
          <RouterLink to="/charging-records">充电记录</RouterLink>
          <RouterLink to="/users">用户管理</RouterLink>
          <RouterLink to="/fault-reports">故障报修</RouterLink>
          <RouterLink to="/warning-notices">预警通知</RouterLink>
          <RouterLink to="/announcement">公告管理</RouterLink>
          <RouterLink to="/statistics">统计分析</RouterLink>
        </nav>
        <div class="header-right">
          <div class="user-info">
            <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
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
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 20px;
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  white-space: nowrap;
}

.nav {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.nav a {
  padding: 8px 14px;
  color: #333;
  text-decoration: none;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.app-main {
  flex: 1;
}
</style>