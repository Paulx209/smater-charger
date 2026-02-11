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
        <div class="logo">智能充电桩管理系统 - 管理端</div>
        <nav class="nav">
          <RouterLink to="/price-config">费用配置</RouterLink>
          <RouterLink to="/announcement">系统公告</RouterLink>
        </nav>
        <div class="header-right">
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
