<template>
  <div class="tag-manage">
    <div class="page-header">
      <h2>标签管理</h2>
      <p class="subtitle">管理资产标签</p>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="tagName"
          placeholder="请输入标签名称"
          class="tag-input"
          @keyup.enter="handleAdd"
        />
        <el-button type="primary" @click="handleAdd" :loading="adding">
          <el-icon><Plus /></el-icon>
          新增标签
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe class="data-table" empty-text="暂无标签数据">
      <el-table-column prop="tagName" label="标签名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="创建时间" width="200">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="assetCount" label="关联资产数" width="120" align="center">
        <template #default="{ row }">
          {{ row.assetCount ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="danger" link @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="tableData.length === 0 && !loading" class="empty-hint">
      <el-empty description="暂无标签，请添加" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import * as tagApi from '../api/tag'

const loading = ref(false)
const adding = ref(false)
const tagName = ref('')
const tableData = ref([])

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}:${s}`
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await tagApi.getList()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch {
    ElMessage.error('加载标签列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = async () => {
  const name = tagName.value.trim()
  if (!name) {
    ElMessage.warning('请输入标签名称')
    return
  }
  adding.value = true
  try {
    const res = await tagApi.add({ tagName: name })
    if (res.code === 200) {
      ElMessage.success(res.message || '新增成功')
      tagName.value = ''
      loadList()
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '新增失败')
  } finally {
    adding.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除标签"${row.tagName}"？`,
    '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const res = await tagApi.remove(row.tagId)
      if (res.code === 200) {
        ElMessage.success(res.message || '删除成功')
        loadList()
      }
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.tag-manage {
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tag-input {
  width: 280px;
}

.data-table {
  margin-top: 0;
  border-radius: 8px;
  overflow: hidden;
}

.empty-hint {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
</style>
