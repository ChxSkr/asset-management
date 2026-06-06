<template>
  <el-container class="main-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo" @click="$router.push('/dashboard')">
        <el-icon :size="22"><Box /></el-icon>
        <span v-show="!isCollapse" class="logo-text">资产管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="var(--bg-sidebar)"
        text-color="var(--text-sidebar)"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/assets">
          <el-icon><FolderOpened /></el-icon>
          <span>资产管理</span>
        </el-menu-item>
        <el-menu-item index="/assets/upload">
          <el-icon><Upload /></el-icon>
          <span>上传资产</span>
        </el-menu-item>
        <el-menu-item index="/recycle">
          <el-icon><Delete /></el-icon>
          <span>回收站</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Collection /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
        <el-sub-menu index="logs">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>日志管理</span>
          </template>
          <el-menu-item index="/logs/operation">操作日志</el-menu-item>
          <el-menu-item index="/logs/download">下载日志</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/search">
          <el-icon><Search /></el-icon>
          <span>全局搜索</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.userInfo && userStore.userInfo.role === 'admin'" index="/users">
          <el-icon><Setting /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer" @click="isCollapse = !isCollapse">
        <el-icon :size="18"><component :is="isCollapse ? 'Expand' : 'Fold'" /></el-icon>
      </div>
    </el-aside>
    <el-container class="right-container">
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip :content="themeStore.isDark ? '切换浅色' : '切换深色'" placement="bottom">
            <span class="theme-toggle" @click="themeStore.toggleTheme()">
              <el-icon :size="20">
                <Sunny v-if="themeStore.isDark" />
                <Moon v-else />
              </el-icon>
            </span>
          </el-tooltip>
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo ? userStore.userInfo.username : '用户' }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { useThemeStore } from '../store/theme'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumbMap = {
  '/dashboard': '仪表盘',
  '/assets': '资产管理',
  '/assets/upload': '上传资产',
  '/recycle': '回收站',
  '/categories': '分类管理',
  '/tags': '标签管理',
  '/logs/operation': '操作日志',
  '/logs/download': '下载日志',
  '/search': '全局搜索',
  '/profile': '个人中心',
  '/users': '用户管理'
}

const breadcrumbTitle = computed(() => {
  if (route.path.startsWith('/assets/') && route.path !== '/assets' && route.path !== '/assets/upload') {
    return '资产详情'
  }
  return breadcrumbMap[route.path] || ''
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-container {
  height: 100vh;
}

.sidebar {
  background-color: var(--bg-sidebar);
  display: flex;
  flex-direction: column;
  transition: width var(--transition);
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  cursor: pointer;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  font-weight: 700;
}

.logo-text {
  font-size: 17px;
  white-space: nowrap;
}

.sidebar .el-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar .el-menu--collapse {
  width: 64px;
}

.sidebar-footer {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sidebar);
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  transition: color var(--transition);
}

.sidebar-footer:hover {
  color: #409EFF;
}

.right-container {
  flex-direction: column;
}

.header {
  background-color: var(--bg-header);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.theme-toggle {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  transition: all var(--transition);
}

.theme-toggle:hover {
  background-color: var(--bg-hover);
  color: var(--text-primary);
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background-color var(--transition);
}

.user-info:hover {
  background-color: var(--bg-hover);
}

.username {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.dropdown-arrow {
  color: var(--text-muted);
  font-size: 12px;
  transition: transform var(--transition);
}

.main-content {
  background-color: var(--bg-base);
  padding: 24px;
  overflow-y: auto;
}
</style>
