import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import AdminView from '../views/AdminView.vue'
import UserView from '../views/UserView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView,
      meta: { requiresAuth: true, role: 'ADMIN' } // 标记需要管理员权限
    },
    {
      path: '/user',
      name: 'user',
      component: UserView,
      meta: { requiresAuth: true } // 标记需要登录
    },
    {
      path: '/',
      redirect: () => {
        // 自动根据角色跳转首页
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        if (!user.id) return '/login'
        return (user.role === 'ADMIN_HR' || user.role === 'ADMIN_NORMAL') ? '/admin' : '/user'
      }
    }
  ]
})

// 🌟 核心逻辑：全局路由守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  
  // 1. 如果去往需要登录的页面
  if (to.meta.requiresAuth && !user.id) {
    next('/login')
  } 
  // 2. 如果已经登录了还想去登录页，直接送回首页
  else if (to.name === 'login' && user.id) {
    next('/')
  }
  // 3. 权限校验（可选）：防止普通员工直接输入 URL 进入管理员页面
  else if (to.path === '/admin' && user.role === 'EMPLOYEE') {
    next('/user')
  }
  else {
    next()
  }
})
export default router
