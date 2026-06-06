<template>
  <div class="log-page">
    <div class="page-header">
      <h2>下载日志</h2>
      <p class="subtitle">查看资产下载记录</p>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe class="data-table" empty-text="暂无下载日志">
      <el-table-column prop="assetName" label="资产名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userName" label="下载用户" width="140" />
      <el-table-column label="下载时间" width="200">
        <template #default="{ row }">
          {{ formatTime(row.downloadTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" width="160" />
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
import { getDownloadLogs } from '../api/log'

const loading = ref(false)
const tableData = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 15,
  total: 0
})

const formatTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.toString().substring(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDownloadLogs({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '加载下载日志失败')
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
