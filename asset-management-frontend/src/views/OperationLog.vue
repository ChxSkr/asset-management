<template>
  <div class="log-page">
    <div class="page-header">
      <h2>操作日志</h2>
      <p class="subtitle">查看系统操作记录</p>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe class="data-table" empty-text="暂无操作日志">
      <el-table-column label="操作类型" width="150">
        <template #default="{ row }">
          <el-tag :color="getOpTypeColor(row.operationType)" effect="dark" size="small">
            {{ getOpTypeName(row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="操作用户" width="150" />
      <el-table-column prop="description" label="操作描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="操作IP" width="160" />
      <el-table-column label="操作时间" width="200">
        <template #default="{ row }">
          {{ formatTime(row.operationTime) }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOperationLogs } from '../api/log'

const loading = ref(false)
const tableData = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 15,
  total: 0
})

const opTypeColors = {
  LOGIN: '#409EFF',
  REGISTER: '#67C23A',
  UPLOAD: '#E6A23C',
  UPDATE: '#909399',
  DELETE: '#F56C6C',
  RESTORE: '#67C23A',
  PERMANENT_DELETE: '#C00000',
  VERSION_UPLOAD: '#9B59B6',
  DOWNLOAD: '#67C23A'
}

const opTypeNames = {
  LOGIN: '登录',
  REGISTER: '注册',
  UPLOAD: '上传',
  UPDATE: '更新',
  DELETE: '删除',
  RESTORE: '恢复',
  PERMANENT_DELETE: '永久删除',
  VERSION_UPLOAD: '上传版本',
  DOWNLOAD: '下载'
}

const getOpTypeColor = (type) => {
  return opTypeColors[type] || '#909399'
}

const getOpTypeName = (type) => {
  return opTypeNames[type] || type
}

const formatTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.toString().substring(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOperationLogs({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '加载操作日志失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.log-page {
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
