<template>
  <div class="user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <p class="subtitle">管理系统用户信息</p>
      <el-button type="primary" @click="openCreateDialog" :icon="Plus">新增用户</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe class="data-table" empty-text="暂无用户数据">
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :color="getRoleColor(row.role)" effect="dark" size="small">
            {{ row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            active-text="正常"
            inactive-text="禁用"
            disabled
          />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="200">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button type="warning" link @click="openResetPasswordDialog(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[15, 30, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 新增用户弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增用户" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
      <el-form :model="editFormData" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editFormData.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editFormData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editFormData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editFormData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editFormData.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editFormData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEdit" :loading="editing">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetPasswordDialogVisible" title="重置密码" width="500px">
      <el-form :model="resetPasswordFormData" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="resetPasswordFormData.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordFormData.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordFormData.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword" :loading="resetting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserList, createUser, getUserById, updateUser, resetPassword } from '../api/user'

const loading = ref(false)
const creating = ref(false)
const editing = ref(false)
const resetting = ref(false)
const dialogVisible = ref(false)
const editDialogVisible = ref(false)
const resetPasswordDialogVisible = ref(false)
const formRef = ref(null)
const editFormRef = ref(null)
const resetPasswordFormRef = ref(null)
const tableData = ref([])
const currentEditUserId = ref(null)
const currentResetUserId = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 15,
  total: 0
})

const formData = reactive({
  username: '',
  password: '',
  realName: '',
  role: 'user',
  email: '',
  phone: ''
})

const editFormData = reactive({
  userId: null,
  username: '',
  realName: '',
  role: 'user',
  email: '',
  phone: '',
  status: 1
})

const resetPasswordFormData = reactive({
  userId: null,
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const editRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetPasswordFormData.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const roleColors = {
  admin: '#F56C6C',
  user: '#409EFF'
}

const getRoleColor = (role) => {
  return roleColors[role] || '#909399'
}

const formatTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.toString().substring(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  Object.assign(formData, {
    username: '',
    password: '',
    realName: '',
    role: 'user',
    email: '',
    phone: ''
  })
  dialogVisible.value = true
}

const handleCreate = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      creating.value = true
      try {
        const res = await createUser(formData)
        if (res.code === 200) {
          ElMessage.success('新增用户成功')
          dialogVisible.value = false
          fetchData()
        } else {
          ElMessage.error(res.message || '新增用户失败')
        }
      } catch (err) {
        ElMessage.error(err?.response?.data?.message || '新增用户失败')
      } finally {
        creating.value = false
      }
    }
  })
}

const openEditDialog = (row) => {
  currentEditUserId.value = row.userId
  Object.assign(editFormData, {
    userId: row.userId,
    username: row.username,
    realName: row.realName,
    role: row.role,
    email: row.email,
    phone: row.phone,
    status: row.status
  })
  editDialogVisible.value = true
}

const handleEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editing.value = true
      try {
        const res = await updateUser(currentEditUserId.value, editFormData)
        if (res.code === 200) {
          ElMessage.success('更新用户成功')
          editDialogVisible.value = false
          fetchData()
        } else {
          ElMessage.error(res.message || '更新用户失败')
        }
      } catch (err) {
        ElMessage.error(err?.response?.data?.message || '更新用户失败')
      } finally {
        editing.value = false
      }
    }
  })
}

const openResetPasswordDialog = (row) => {
  currentResetUserId.value = row.userId
  Object.assign(resetPasswordFormData, {
    userId: row.userId,
    username: row.username,
    newPassword: '',
    confirmPassword: ''
  })
  resetPasswordDialogVisible.value = true
}

const handleResetPassword = async () => {
  if (!resetPasswordFormRef.value) return
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      resetting.value = true
      try {
        const res = await resetPassword(currentResetUserId.value, resetPasswordFormData.newPassword)
        if (res.code === 200) {
          ElMessage.success('重置密码成功')
          resetPasswordDialogVisible.value = false
        } else {
          ElMessage.error(res.message || '重置密码失败')
        }
      } catch (err) {
        ElMessage.error(err?.response?.data?.message || '重置密码失败')
      } finally {
        resetting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.data-table {
  border-radius: 8px;
  overflow: hidden;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
