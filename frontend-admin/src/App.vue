<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => Boolean(userStore.token))
const userInfo = computed(() => userStore.userInfo)

const navItems = [
  { to: '/statistics', label: '统计分析' },
  { to: '/price-config', label: '费用配置' },
  { to: '/reservations', label: '预约管理' },
  { to: '/charging-records', label: '充电记录' },
  { to: '/users', label: '用户管理' },
  { to: '/vehicles', label: '车辆管理' },
  { to: '/fault-reports', label: '故障报修' },
  { to: '/warning-notices', label: '预警通知' },
  { to: '/announcement', label: '公告管理' }
]

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('初始化管理端登录态失败:', error)
    }
  }
})
</script>

<template>
  <div class="app-container">
    <header v-if="isLoggedIn" class="app-header">
      <div class="header-content">
        <div class="brand-block">
          <div class="logo-mark">SC</div>
          <div class="brand-copy">
            <div class="brand-title">智能充电管理后台</div>
            <div class="brand-subtitle">Smart Charger Admin Console</div>
          </div>
        </div>

        <div class="nav-shell">
          <nav class="nav">
            <RouterLink
              v-for="item in navItems"
              :key="item.to"
              :to="item.to"
              class="nav-link"
            >
              {{ item.label }}
            </RouterLink>
          </nav>
        </div>

        <div class="header-right">
          <div class="user-chip">
            <span class="user-label">当前账号</span>
            <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
          </div>
          <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
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
  background: #f3f6fb;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid #e7edf5;
  backdrop-filter: blur(10px);
}

.header-content {
  width: min(100%, 1680px);
  margin: 0 auto;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.logo-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #409eff, #1f6bff);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.brand-title {
  color: #18222f;
  font-size: 18px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 2px;
  color: #7d8da1;
  font-size: 12px;
}

.nav-shell {
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: thin;
}

.nav {
  display: flex;
  align-items: center;
  gap: 10px;
  width: max-content;
  min-width: 100%;
}

.nav-link {
  padding: 9px 14px;
  border-radius: 999px;
  color: #425466;
  text-decoration: none;
  white-space: nowrap;
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.nav-link:hover {
  background: #eef4ff;
  color: #1f6bff;
}

.nav-link.router-link-active {
  background: linear-gradient(135deg, #eff6ff, #e4f0ff);
  color: #1f6bff;
  box-shadow: inset 0 0 0 1px #d7e6ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.user-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-label {
  color: #8b99aa;
  font-size: 12px;
}

.username {
  color: #1c2733;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

.app-main {
  flex: 1;
  width: 100%;
  overflow-x: hidden;
}

.app-main :deep(.el-pagination) {
  flex-wrap: wrap;
  row-gap: 8px;
}

.app-main :deep(.el-table) {
  max-width: 100%;
}

.app-main :deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

@media (max-width: 1180px) {
  .header-content {
    flex-wrap: wrap;
  }

  .nav-shell {
    order: 3;
    width: 100%;
  }

  .nav {
    min-width: max-content;
  }
}

@media (max-width: 640px) {
  .header-content {
    padding: 12px 14px;
  }

  .brand-subtitle {
    display: none;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
