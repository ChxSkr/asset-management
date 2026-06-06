<template>
  <div class="not-found">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-desc">页面不存在</p>
      <p class="error-hint">您访问的页面可能已被移除或地址输入有误</p>
      <el-button type="primary" size="large" @click="$router.push('/')">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { HomeFilled } from '@element-plus/icons-vue'
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: #409eff;
  margin: 0;
  line-height: 1;
  letter-spacing: 8px;
}

.error-desc {
  font-size: 24px;
  color: #303133;
  margin: 24px 0 12px 0;
  font-weight: 500;
}

.error-hint {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px 0;
}
</style>
