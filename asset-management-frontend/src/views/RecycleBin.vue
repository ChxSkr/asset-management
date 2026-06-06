<template>
  <div class="recycle-bin">
    <div class="page-header">
      <h2>回收站</h2>
      <p class="subtitle">管理已删除的资产，可恢复或彻底删除</p>
    </div>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe empty-text="回收站为空">
        <el-table-column prop="assetName" label="资产名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="文件类型" width="120">
          <template #default="{ row }">
            <el-tag :color="getFileTypeColor(row.fileType)" effect="dark" size="small">
              {{ row.fileType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="删除时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleRestore(row)">
              <el-icon><RefreshLeft /></el-icon>
              恢复
            </el-button>
            <el-popconfirm title="彻底删除后不可恢复，确定继续？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handlePermanentDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">
                  <el-icon><DeleteFilled /></el-icon>
                  彻底删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 30]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, DeleteFilled } from '@element-plus/icons-vue'
import { getRecycleList, restore, permanentDelete } from '../api/asset'

const loading = ref(false)
const tableData = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const getFileTypeColor = (fileType) => {
  const type = (fileType || '').toUpperCase()
  if (type === 'PDF') return '#e74c3c'
  if (['DOC', 'DOCX', 'XLS', 'XLSX', 'PPT', 'PPTX', 'TXT'].includes(type)) return '#3498db'
  if (['JPG', 'JPEG', 'PNG', 'GIF', 'BMP', 'SVG', 'WEBP'].includes(type)) return '#2ecc71'
  if (['MP4', 'AVI', 'MOV', 'MKV', 'WMV', 'FLV'].includes(type)) return '#9b59b6'
  return '#95a5a6'
}

const formatFileSize = (size) => {
  if (!size && size !== 0) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

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

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRecycleList({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '加载回收站数据失败')
  } finally {
    loading.value = false
  }
}

const handleRestore = async (row) => {
  try {
    const res = await restore(row.assetId)
    if (res.code === 200) {
      ElMessage.success(res.message || '恢复成功')
      loadData()
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '恢复失败')
  }
}

const handlePermanentDelete = async (row) => {
  try {
    const res = await permanentDelete(row.assetId)
    if (res.code === 200) {
      ElMessage.success(res.message || '彻底删除成功')
      loadData()
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.recycle-bin {
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

.table-card {
  border-radius: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
