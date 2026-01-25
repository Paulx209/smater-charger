import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/charging-piles',
      name: 'charging-piles',
      component: () => import('../views/ChargingPileList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/charging-piles/:id',
      name: 'charging-pile-detail',
      component: () => import('../views/ChargingPileDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vehicles',
      name: 'vehicle-list',
      component: () => import('../views/VehicleList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vehicles/add',
      name: 'vehicle-add',
      component: () => import('../views/VehicleForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vehicles/:id/edit',
      name: 'vehicle-edit',
      component: () => import('../views/VehicleForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/reservations',
      name: 'reservation-list',
      component: () => import('../views/ReservationList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/reservations/create/:pileId?',
      name: 'reservation-create',
      component: () => import('../views/ReservationCreate.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/reservations/:id',
      name: 'reservation-detail',
      component: () => import('../views/ReservationDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/price-config',
      name: 'admin-price-config-list',
      component: () => import('../views/admin/PriceConfigList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/price-config/add',
      name: 'admin-price-config-add',
      component: () => import('../views/admin/PriceConfigForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/price-config/:id/edit',
      name: 'admin-price-config-edit',
      component: () => import('../views/admin/PriceConfigForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/charging-record',
      name: 'charging-record-list',
      component: () => import('../views/ChargingRecordList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/charging-record/:id',
      name: 'charging-record-detail',
      component: () => import('../views/ChargingRecordDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/charging-record/statistics',
      name: 'charging-record-statistics',
      component: () => import('../views/ChargingRecordStatistics.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/warning-notice',
      name: 'warning-notice-list',
      component: () => import('../views/WarningNoticeList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/warning-notice/settings',
      name: 'warning-notice-settings',
      component: () => import('../views/WarningNoticeSettings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/fault-reports',
      name: 'fault-report-list',
      component: () => import('../views/FaultReportList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/fault-reports/:id',
      name: 'fault-report-detail',
      component: () => import('../views/FaultReportDetail.vue'),
      meta: { requiresAuth: true }
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const hasToken = isAuthenticated()

  if (to.meta.requiresAuth && !hasToken) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && hasToken) {
    // 已登录访问登录页或注册页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router
