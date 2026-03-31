import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

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
      path: '/',
      name: 'home',
      redirect: '/price-config'
    },
    {
      path: '/price-config',
      name: 'price-config-list',
      component: () => import('../views/admin/PriceConfigList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/price-config/add',
      name: 'price-config-add',
      component: () => import('../views/admin/PriceConfigForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/price-config/:id/edit',
      name: 'price-config-edit',
      component: () => import('../views/admin/PriceConfigForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/reservations',
      name: 'reservation-list',
      component: () => import('../views/admin/ReservationList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/reservations/:id',
      name: 'reservation-detail',
      component: () => import('../views/admin/ReservationDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/charging-records',
      name: 'charging-record-list',
      component: () => import('../views/admin/ChargingRecordList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/charging-records/:id',
      name: 'charging-record-detail',
      component: () => import('../views/admin/ChargingRecordDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/announcement',
      name: 'announcement-list',
      component: () => import('../views/admin/AnnouncementList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/announcement/create',
      name: 'announcement-create',
      component: () => import('../views/admin/AnnouncementForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/announcement/edit/:id',
      name: 'announcement-edit',
      component: () => import('../views/admin/AnnouncementForm.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/fault-reports',
      name: 'fault-report-list',
      component: () => import('../views/admin/FaultReportList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/fault-reports/statistics',
      name: 'fault-report-statistics',
      component: () => import('../views/admin/FaultReportStatistics.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/fault-reports/:id',
      name: 'fault-report-detail',
      component: () => import('../views/admin/FaultReportDetail.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/users',
      name: 'user-management-list',
      component: () => import('../views/admin/UserManagementList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/statistics',
      name: 'statistics-dashboard',
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