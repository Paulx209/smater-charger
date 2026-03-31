import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      alias: ['/admin/login'],
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register-blocked',
      alias: ['/admin/register'],
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'home',
      alias: ['/admin'],
      redirect: '/statistics'
    },
    {
      path: '/price-config',
      name: 'price-config-list',
      alias: ['/admin/price-config'],
      component: () => import('../views/admin/PriceConfigList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/price-config/add',
      name: 'price-config-add',
      alias: ['/admin/price-config/add'],
      component: () => import('../views/admin/PriceConfigForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/price-config/:id/edit',
      name: 'price-config-edit',
      alias: ['/admin/price-config/:id/edit'],
      component: () => import('../views/admin/PriceConfigForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/reservations',
      name: 'reservation-list',
      alias: ['/admin/reservations'],
      component: () => import('../views/admin/ReservationList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/reservations/:id',
      name: 'reservation-detail',
      alias: ['/admin/reservations/:id'],
      component: () => import('../views/admin/ReservationDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/charging-records',
      name: 'charging-record-list',
      alias: ['/admin/charging-records'],
      component: () => import('../views/admin/ChargingRecordList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/charging-records/:id',
      name: 'charging-record-detail',
      alias: ['/admin/charging-records/:id'],
      component: () => import('../views/admin/ChargingRecordDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/announcement',
      name: 'announcement-list',
      alias: ['/admin/announcement'],
      component: () => import('../views/admin/AnnouncementList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/announcement/create',
      name: 'announcement-create',
      alias: ['/admin/announcement/create'],
      component: () => import('../views/admin/AnnouncementForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/announcement/edit/:id',
      name: 'announcement-edit',
      alias: ['/admin/announcement/edit/:id'],
      component: () => import('../views/admin/AnnouncementForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/fault-reports',
      name: 'fault-report-list',
      alias: ['/admin/fault-reports'],
      component: () => import('../views/admin/FaultReportList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/fault-reports/statistics',
      name: 'fault-report-statistics',
      alias: ['/admin/fault-reports/statistics'],
      component: () => import('../views/admin/FaultReportStatistics.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/fault-reports/:id',
      name: 'fault-report-detail',
      alias: ['/admin/fault-reports/:id'],
      component: () => import('../views/admin/FaultReportDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/warning-notices',
      name: 'warning-notice-list',
      alias: ['/admin/warning-notices'],
      component: () => import('../views/admin/WarningNoticeList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/warning-notices/settings',
      name: 'warning-notice-settings',
      alias: ['/admin/warning-notices/settings'],
      component: () => import('../views/admin/WarningNoticeSettings.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/warning-notices/:id',
      name: 'warning-notice-detail',
      alias: ['/admin/warning-notices/:id'],
      component: () => import('../views/admin/WarningNoticeDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/users',
      name: 'user-management-list',
      alias: ['/admin/users'],
      component: () => import('../views/admin/UserManagementList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/vehicles',
      name: 'vehicle-list',
      alias: ['/admin/vehicles'],
      component: () => import('../views/admin/VehicleList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/vehicles/:id',
      name: 'vehicle-detail',
      alias: ['/admin/vehicles/:id'],
      component: () => import('../views/admin/VehicleDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/statistics',
      name: 'statistics-dashboard',
      alias: ['/admin/statistics'],
      component: () => import('../views/admin/StatisticsDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const hasToken = isAuthenticated()

  if (to.meta.requiresAuth && !hasToken) {
    next('/login')
  } else if (to.path === '/login' && hasToken) {
    next('/')
  } else {
    next()
  }
})

export default router
