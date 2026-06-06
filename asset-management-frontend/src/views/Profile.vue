<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人中心</h2>
      <p class="subtitle">查看和编辑个人信息</p>
    </div>

    <div class="profile-card">
      <div class="avatar-section">
        <el-avatar :size="80" class="avatar">
          {{ userInfo?.realName?.charAt(0) || userInfo?.username?.charAt(0) || 'U' }}
        </el-avatar>
        <h3 class="avatar-name">{{ userInfo?.realName || userInfo?.username || '-' }}</h3>
        <el-tag type="info" size="small">{{ roleMap[userInfo?.role] || userInfo?.role || '未知' }}</el-tag>
      </div>

      <el-divider />

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="profile-form"
        :disabled="!editing"
      >
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input :model-value="roleMap[form.role] || form.role" disabled />
        </el-form-item>
      </el-form>

      <div class="action-buttons">
        <template v-if="!editing">
          <el-button type="primary" @click="startEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </template>
        <template v-else>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">
            <el-icon><Check /></el-icon>
            保存
          </el-button>
        </template>
        <el-button type="danger" plain @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Check, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import * as userApi from '../api/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const editing = ref(false)
const saving = ref(false)
const loading = ref(false)

const roleMap = {
  ADMIN: '管理员',
  USER: '普通用户',
  MANAGER: '经理'
}

const form = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  loading.value = true
  try {
    await userStore.fetchProfile()
    const info = userStore.userInfo
    if (info) {
      form.username = info.username || ''
      form.realName = info.realName || ''
      form.email = info.email || ''
      form.phone = info.phone || ''
      form.role = info.role || ''
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '加载用户信息失败')
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  editing.value = true
}

const cancelEdit = () => {
  editing.value = false
  const info = userStore.userInfo
  if (info) {
    form.realName = info.realName || ''
    form.email = info.email || ''
    form.phone = info.phone || ''
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = await userApi.updateProfile({
      realName: form.realName,
      email: form.email,
      phone: form.phone
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '保存成功')
      editing.value = false
      await userStore.fetchProfile()
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '退出确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.profile-card {
  max-width: 640px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 32px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar {
  background: #409eff;
  font-size: 32px;
  font-weight: 600;
}

.avatar-name {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.profile-form {
  margin-top: 16px;
}

.profile-form .el-form-item {
  margin-bottom: 18px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
