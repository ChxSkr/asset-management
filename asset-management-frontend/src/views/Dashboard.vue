<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>仪表盘</h2>
      <p class="subtitle">欢迎使用数字资产管理系统</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.total }}</div>
              <div class="stat-label">总资产数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon doc">
              <el-icon :size="32"><CopyDocument /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.docCount }}</div>
              <div class="stat-label">文档数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon image">
              <el-icon :size="32"><PictureFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.imageCount }}</div>
              <div class="stat-label">图片数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon video">
              <el-icon :size="32"><VideoCameraFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.videoCount }}</div>
              <div class="stat-label">视频数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="recent-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">最近上传的资产</span>
          <el-button type="primary" link @click="$router.push('/assets')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentAssets" stripe v-loading="loading" empty-text="暂无资产数据">
        <el-table-column prop="assetName" label="资产名称" min-width="200" show-overflow-tooltip />
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
        <el-table-column label="当前版本" width="100">
          <template #default="{ row }">
            v{{ row.currentVersion || 1 }}
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Document, CopyDocument, PictureFilled, VideoCameraFilled } from '@element-plus/icons-vue'
import { getList } from '../api/asset'

const loading = ref(false)
const stats = reactive({
  total: 0,
  docCount: 0,
  imageCount: 0,
  videoCount: 0
})
const recentAssets = ref([])

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

onMounted(async () => {
  loading.value = true
  try {
    const res = await getList({ page: 1, pageSize: 1 })
    if (res.code === 200) {
      stats.total = res.data.total || 0
    }
  } catch {
    stats.total = 0
  }

  try {
    const res = await getList({ page: 1, pageSize: 10 })
    if (res.code === 200) {
      recentAssets.value = res.data.records || []
      stats.docCount = recentAssets.value.filter(item => {
        const t = (item.fileType || '').toUpperCase()
        return ['PDF', 'DOC', 'DOCX', 'XLS', 'XLSX', 'PPT', 'PPTX', 'TXT'].includes(t)
      }).length
      stats.imageCount = recentAssets.value.filter(item => {
        const t = (item.fileType || '').toUpperCase()
        return ['JPG', 'JPEG', 'PNG', 'GIF', 'BMP', 'SVG', 'WEBP'].includes(t)
      }).length
      stats.videoCount = recentAssets.value.filter(item => {
        const t = (item.fileType || '').toUpperCase()
        return ['MP4', 'AVI', 'MOV', 'MKV', 'WMV', 'FLV'].includes(t)
      }).length
    }
  } catch {
    recentAssets.value = []
  }
  loading.value = false
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
  color: var(--text-primary);
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: var(--radius-md);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.total {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.stat-icon.doc {
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
}

.stat-icon.image {
  background: rgba(46, 204, 113, 0.1);
  color: #2ecc71;
}

.stat-icon.video {
  background: rgba(155, 89, 182, 0.1);
  color: #9b59b6;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.recent-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
