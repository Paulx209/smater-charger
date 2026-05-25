<script setup lang="ts">
import { computed, onUnmounted, type Component, watch } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import {
  Bell,
  Document,
  Headset,
  House,
  Lightning,
  Tickets,
  User,
  Van
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useWarningNoticeStore } from '@/stores/warningNotice'

interface NavItem {
  to: string
  label: string
  icon: Component
  badge?: 'warning'
}

const router = useRouter()
const userStore = useUserStore()
const warningNoticeStore = useWarningNoticeStore()

const navItems: NavItem[] = [
  { to: '/', label: '首页', icon: House },
  { to: '/charging-piles', label: '充电地图', icon: Lightning },
  { to: '/charging-record', label: '充电记录', icon: Document },
  { to: '/reservations', label: '我的预约', icon: Tickets },
  { to: '/vehicles', label: '车辆管理', icon: Van },
  { to: '/warning-notice', label: '消息通知', icon: Bell, badge: 'warning' },
  { to: '/profile', label: '个人中心', icon: User }
]

const isLoggedIn = computed(() => Boolean(userStore.token))
const userInfo = computed(() => userStore.userInfo)
const unreadCount = computed(() => warningNoticeStore.unreadCount)
const displayName = computed(() => {
  const nickname = userInfo.value?.nickname?.trim()

  if (nickname && !['?', '??', '???', '锛?', '锛燂紵', '锛燂紵锛?'].includes(nickname)) {
    return nickname
  }

  return userInfo.value?.username || '用户'
})
const avatarInitial = computed(() => displayName.value.trim().charAt(0).toUpperCase() || 'U')

let pollingTimer: number | null = null

const stopPolling = () => {
  if (pollingTimer) {
    window.clearInterval(pollingTimer)
    pollingTimer = null
  }
}

const startPolling = () => {
  stopPolling()

  if (!isLoggedIn.value) return

  void warningNoticeStore.fetchUnreadCount()
  pollingTimer = window.setInterval(() => {
    if (isLoggedIn.value) {
      void warningNoticeStore.fetchUnreadCount()
    }
  }, 30000)
}

watch(
  () => userStore.token,
  (token) => {
    if (!token) {
      stopPolling()
      return
    }

    startPolling()
  },
  { immediate: true }
)

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div v-if="isLoggedIn" class="owner-shell">
    <aside class="owner-sidebar">
      <RouterLink to="/" class="owner-brand">
        <span class="owner-brand__mark">
          <el-icon><Lightning /></el-icon>
        </span>
        <span>
          <strong>智能充电桩</strong>
          <small>SMART CHARGER</small>
        </span>
      </RouterLink>

      <nav class="owner-nav" aria-label="车主端导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          v-slot="{ href, navigate, isActive, isExactActive }"
          :to="item.to"
          custom
        >
          <a
            :href="href"
            class="owner-nav__item"
            :class="{ 'is-active': item.to === '/' ? isExactActive : isActive }"
            @click="navigate"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
            <em v-if="item.badge === 'warning' && unreadCount > 0" class="owner-nav__badge">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </em>
          </a>
        </RouterLink>
      </nav>

      <div class="sidebar-spacer" />

      <section class="sidebar-promo" aria-label="智慧充电宣传">
        <div class="sidebar-promo__image" />
        <strong>智慧充电，温暖相伴</strong>
        <span>让充电更简单</span>
      </section>

      <section class="support-card" aria-label="客服热线">
        <span class="support-card__icon">
          <el-icon><Headset /></el-icon>
        </span>
        <div>
          <span>客服热线</span>
          <strong>400-888-8888</strong>
          <small>服务时间 08:00-22:00</small>
        </div>
      </section>
    </aside>

    <section class="owner-workspace">
      <header class="owner-topbar">
        <div>
          <h1>Hello，欢迎回来！ <span aria-hidden="true">👋</span></h1>
          <p>愿今天的每一次充电，都为美好出行助力</p>
        </div>

        <div class="owner-actions">
          <RouterLink to="/warning-notice" class="notice-button" aria-label="查看通知">
            <el-badge :value="unreadCount > 99 ? '99+' : unreadCount" :hidden="unreadCount === 0">
              <el-icon :size="19"><Bell /></el-icon>
            </el-badge>
          </RouterLink>

          <div class="account-chip">
            <span class="account-chip__avatar">{{ avatarInitial }}</span>
            <span>
              <small>当前账号</small>
              <strong>{{ displayName }}</strong>
            </span>
          </div>

          <el-button plain class="logout-button" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="owner-main">
        <RouterView />
      </main>
    </section>
  </div>

  <RouterView v-else />
</template>

<style scoped>
.owner-shell {
  --owner-orange: #ff6f1a;
  --owner-orange-dark: #e95a00;
  --owner-orange-soft: #fff1e5;
  --owner-border: rgba(238, 188, 145, 0.52);
  --owner-text: #17202f;
  --owner-muted: #697384;

  display: grid;
  grid-template-columns: clamp(238px, 18vw, 270px) minmax(0, 1fr);
  min-height: 100vh;
  min-width: 1180px;
  background:
    radial-gradient(circle at 84% 8%, rgba(255, 188, 122, 0.2), transparent 30%),
    linear-gradient(135deg, #fffdf9 0%, #fff8ef 48%, #fffaf5 100%);
  color: var(--owner-text);
}

.owner-sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: clamp(18px, 2.2vh, 28px) 26px;
  border-right: 1px solid var(--owner-border);
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px);
}

.owner-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
}

.owner-brand__mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 50% 50% 50% 12px;
  color: #fff;
  background: linear-gradient(135deg, #ff8a2a 0%, #ff5a00 100%);
  box-shadow: 0 12px 28px rgba(255, 103, 17, 0.24);
  transform: rotate(-12deg);
}

.owner-brand__mark .el-icon {
  transform: rotate(12deg);
}

.owner-brand strong,
.owner-brand small {
  display: block;
}

.owner-brand strong {
  font-size: 19px;
  font-weight: 900;
  letter-spacing: 0;
}

.owner-brand small {
  margin-top: 2px;
  color: var(--owner-muted);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.owner-nav {
  display: flex;
  flex-direction: column;
  gap: clamp(8px, 1.1vh, 12px);
  margin-top: clamp(24px, 3vh, 38px);
}

.owner-nav__item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 13px;
  min-height: clamp(44px, 5.2vh, 54px);
  padding: 0 18px;
  border-radius: 12px;
  color: #4d5563;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.owner-nav__item:hover {
  color: var(--owner-orange-dark);
  background: rgba(255, 242, 228, 0.74);
  transform: translateX(2px);
}

.owner-nav__item.is-active {
  color: var(--owner-orange-dark);
  background: linear-gradient(135deg, #fff0e3 0%, #fff7ef 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 111, 26, 0.12);
}

.owner-nav__item .el-icon {
  font-size: 20px;
}

.owner-nav__badge {
  display: grid;
  min-width: 20px;
  height: 20px;
  place-items: center;
  margin-left: auto;
  padding: 0 5px;
  border-radius: 999px;
  color: #fff;
  background: #ff4d5a;
  font-size: 12px;
  font-style: normal;
  line-height: 1;
}

.sidebar-spacer {
  flex: 1;
}

.sidebar-promo,
.support-card {
  border: 1px solid var(--owner-border);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 38px rgba(124, 72, 32, 0.08);
}

.sidebar-promo {
  overflow: hidden;
  padding: clamp(14px, 1.8vh, 18px);
  color: var(--owner-orange-dark);
}

.sidebar-promo__image {
  height: clamp(58px, 8vh, 86px);
  margin: 4px 0 clamp(8px, 1.2vh, 12px);
  border-radius: 12px;
  background:
    radial-gradient(circle at 20% 40%, rgba(255, 150, 56, 0.24), transparent 28%),
    linear-gradient(135deg, rgba(255, 247, 237, 0.9), rgba(255, 224, 190, 0.85));
}

.sidebar-promo strong,
.sidebar-promo span {
  display: block;
}

.sidebar-promo strong {
  font-size: 15px;
  font-weight: 900;
}

.sidebar-promo span {
  margin-top: 6px;
  color: var(--owner-muted);
  font-size: 13px;
}

.support-card {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding: clamp(14px, 1.8vh, 18px);
}

.support-card__icon {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 12px;
  color: var(--owner-orange-dark);
  background: #fff1e5;
}

.support-card span,
.support-card strong,
.support-card small {
  display: block;
}

.support-card span {
  color: var(--owner-muted);
  font-size: 13px;
}

.support-card strong {
  margin-top: 4px;
  color: var(--owner-orange-dark);
  font-size: 16px;
  font-weight: 900;
}

.support-card small {
  margin-top: 6px;
  color: var(--owner-muted);
  font-size: 12px;
}

.owner-workspace {
  min-width: 0;
  padding: clamp(14px, 2.1vh, 28px) clamp(22px, 2.8vw, 38px);
}

.owner-topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 28px;
  margin-bottom: clamp(8px, 1.4vh, 22px);
}

.owner-topbar h1 {
  margin: 0;
  color: var(--owner-text);
  font-size: clamp(24px, 3vh, 28px);
  font-weight: 900;
  letter-spacing: 0;
}

.owner-topbar p {
  margin: clamp(6px, 1vh, 12px) 0 0;
  color: #5f6878;
  font-size: 15px;
}

.owner-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notice-button,
.account-chip,
.logout-button {
  border: 1px solid var(--owner-border);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 12px 28px rgba(124, 72, 32, 0.08);
}

.notice-button {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 15px;
  color: #303846;
  text-decoration: none;
}

.notice-button:hover {
  color: var(--owner-orange-dark);
}

.account-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 144px;
  height: 48px;
  padding: 0 14px 0 8px;
  border-radius: 999px;
}

.account-chip__avatar {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, #d7a174, #7b4b2d);
  font-weight: 900;
}

.account-chip small,
.account-chip strong {
  display: block;
}

.account-chip small {
  color: var(--owner-muted);
  font-size: 11px;
}

.account-chip strong {
  margin-top: 2px;
  color: var(--owner-text);
  font-size: 14px;
}

.logout-button {
  height: 44px;
  padding: 0 18px;
  border-radius: 12px;
  border-color: rgba(255, 111, 26, 0.26);
  color: var(--owner-orange-dark);
  font-weight: 800;
}

.owner-main {
  min-width: 0;
}

@media (max-height: 820px) {
  .owner-sidebar {
    padding-top: 14px;
    padding-bottom: 14px;
  }

  .owner-brand__mark {
    width: 34px;
    height: 34px;
  }

  .owner-brand strong {
    font-size: 17px;
  }

  .owner-brand small {
    font-size: 11px;
  }

  .owner-nav {
    gap: 6px;
    margin-top: 18px;
  }

  .owner-nav__item {
    min-height: 38px;
    border-radius: 10px;
    font-size: 14px;
  }

  .sidebar-promo {
    padding: 12px;
  }

  .sidebar-promo__image {
    height: 44px;
    margin: 2px 0 8px;
  }

  .sidebar-promo strong {
    font-size: 14px;
  }

  .sidebar-promo span,
  .support-card span {
    font-size: 12px;
  }

  .support-card {
    gap: 10px;
    margin-top: 8px;
    padding: 12px;
  }

  .support-card__icon {
    width: 30px;
    height: 30px;
  }

  .support-card strong {
    margin-top: 2px;
    font-size: 14px;
  }

  .support-card small {
    margin-top: 4px;
    font-size: 11px;
  }

  .owner-topbar h1 {
    font-size: 22px;
  }

  .owner-topbar p {
    font-size: 13px;
  }

  .notice-button,
  .logout-button {
    height: 38px;
  }

  .notice-button {
    width: 38px;
    border-radius: 12px;
  }

  .account-chip {
    height: 40px;
  }

  .account-chip__avatar {
    width: 32px;
    height: 32px;
  }
}
</style>
