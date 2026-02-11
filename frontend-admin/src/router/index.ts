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
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const hasToken = isAuthenticated()

  if (to.meta.requiresAuth && !hasToken) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && hasToken) {
    // 已登录访问登录页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router
