import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'assets',
        name: 'AssetList',
        component: () => import('../views/AssetList.vue')
      },
      {
        path: 'assets/upload',
        name: 'AssetUpload',
        component: () => import('../views/AssetUpload.vue')
      },
      {
        path: 'assets/:id',
        name: 'AssetDetail',
        component: () => import('../views/AssetDetail.vue')
      },
      {
        path: 'recycle',
        name: 'RecycleBin',
        component: () => import('../views/RecycleBin.vue')
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('../views/CategoryManage.vue')
      },
      {
        path: 'tags',
        name: 'TagManage',
        component: () => import('../views/TagManage.vue')
      },
      {
        path: 'logs/operation',
        name: 'OperationLog',
        component: () => import('../views/OperationLog.vue')
      },
      {
        path: 'logs/download',
        name: 'DownloadLog',
        component: () => import('../views/DownloadLog.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue')
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('../views/UserManage.vue')
      },
      {
        path: 'search',
        name: 'SearchPage',
        component: () => import('../views/SearchPage.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router