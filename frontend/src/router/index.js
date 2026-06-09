import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/guest',
    name: 'GuestLayout',
    component: () => import('@/views/guest/GuestLayout.vue'),
    meta: { role: 'GUEST' },
    redirect: '/guest/rooms',
    children: [
      {
        path: 'rooms',
        name: 'GuestRooms',
        component: () => import('@/views/guest/GuestRooms.vue'),
        meta: { title: '客房查询' }
      },
      {
        path: 'my-orders',
        name: 'GuestOrders',
        component: () => import('@/views/guest/GuestOrders.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'profile',
        name: 'GuestProfile',
        component: () => import('@/views/guest/GuestProfile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'room-type/:id',
        name: 'GuestRoomTypeDetail',
        component: () => import('@/views/guest/GuestRoomTypeDetail.vue'),
        meta: { title: '房型详情' }
      }
    ]
  },
  {
    path: '/front-desk',
    name: 'FrontDeskLayout',
    component: () => import('@/views/frontdesk/FrontDeskLayout.vue'),
    meta: { role: 'FRONT_DESK' },
    redirect: '/front-desk/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'FrontDeskDashboard',
        component: () => import('@/views/frontdesk/FrontDeskDashboard.vue'),
        meta: { title: '房态看板' }
      },
      {
        path: 'orders',
        name: 'FrontDeskOrders',
        component: () => import('@/views/frontdesk/FrontDeskOrders.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'check-in',
        name: 'FrontDeskCheckIn',
        component: () => import('@/views/frontdesk/FrontDeskCheckIn.vue'),
        meta: { title: '入住办理' }
      },
      {
        path: 'check-out',
        name: 'FrontDeskCheckOut',
        component: () => import('@/views/frontdesk/FrontDeskCheckOut.vue'),
        meta: { title: '退房结算' }
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { role: 'ADMIN' },
    redirect: '/admin/room-types',
    children: [
      {
        path: 'room-types',
        name: 'AdminRoomTypes',
        component: () => import('@/views/admin/AdminRoomTypes.vue'),
        meta: { title: '房型管理' }
      },
      {
        path: 'rooms',
        name: 'AdminRooms',
        component: () => import('@/views/admin/AdminRooms.vue'),
        meta: { title: '房间管理' }
      },
      {
        path: 'employees',
        name: 'AdminEmployees',
        component: () => import('@/views/admin/AdminEmployees.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/AdminStatistics.vue'),
        meta: { title: '统计报表' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/AdminOrders.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/AdminLogs.vue'),
        meta: { title: '审计日志' }
      },
      {
        path: 'price-calendar',
        name: 'AdminPriceCalendar',
        component: () => import('@/views/admin/AdminPriceCalendar.vue'),
        meta: { title: '房价日历' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 酒店预订管理系统` : '酒店预订管理系统'

  const token = localStorage.getItem('token')

  if (to.path === '/login' || to.path === '/register') {
    if (token) {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
      const employeeInfo = JSON.parse(localStorage.getItem('employeeInfo') || 'null')
      const role = userInfo?.roleType || employeeInfo?.roleType
      if (role === 'GUEST') return next('/guest/rooms')
      if (role === 'FRONT_DESK') return next('/front-desk/dashboard')
      if (role === 'ADMIN') return next('/admin/room-types')
    }
    return next()
  }

  if (!token) {
    return next('/login')
  }

  if (to.meta.role) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
    const employeeInfo = JSON.parse(localStorage.getItem('employeeInfo') || 'null')
    const role = userInfo?.roleType || employeeInfo?.roleType
    if (role !== to.meta.role) {
      return next('/login')
    }
  }

  next()
})

export default router
