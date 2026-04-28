import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import UserList from '../views/UserList.vue'
import UserDetail from '../views/UserDetail.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import SeatBookingView from '../views/SeatBookingView.vue'
import AdminView from '../views/AdminView.vue'
import AAAView from '../views/AAA.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/aaa',
      name: 'aaa',
      component: AAAView
    },
    {
      path: '/seat-booking',
      name: 'seatBooking',
      component: SeatBookingView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/users',
      name: 'userList',
      component: UserList,
      meta: { requiresAuth: true }
    },
    {
      path: '/users/:id',
      name: 'userDetail',
      component: UserDetail,
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
  const isAuthenticated = !!userStr
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresAdmin) {
    try {
      const user = JSON.parse(userStr)
      if (user.role === 'ADMIN') {
        next()
      } else {
        next('/seat-booking')
      }
    } catch (e) {
      next('/login')
    }
  } else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    try {
      const user = JSON.parse(userStr)
      if (user.role === 'ADMIN') {
        next('/admin')
      } else {
        next('/seat-booking')
      }
    } catch (e) {
      next('/seat-booking')
    }
  } else {
    next()
  }
})

export default router
