<template>
  <div class="asset-list">
    <div class="page-header">
      <h2>资产列表</h2>
      <p class="subtitle">管理所有数字资产</p>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="$router.push('/assets/upload')">
          <el-icon><Plus /></el-icon>
          上传资产
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索资产名称"
          clearable
          class="search-input"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchFileType" placeholder="文件类型" clearable class="filter-select" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="PDF" value="PDF" />
          <el-option label="Word/文档" value="DOC" />
          <el-option label="图片" value="IMAGE" />
          <el-option label="视频" value="VIDEO" />
          <el-option label="音频" value="AUDIO" />
          <el-option label="压缩包" value="ZIP" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select
          v-model="searchTagId"
          placeholder="选择标签"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="全部" :value="null" />
          <el-option
            v-for="tag in tagList"
            :key="tag.tagId"
            :label="tag.tagName"
            :value="tag.tagId"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe class="data-table" empty-text="暂无资产数据">
      <el-table-column prop="assetName" label="资产名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="文件类型" width="100">
        <template #default="{ row }">
          <el-tag :color="getFileTypeColor(row.fileType)" effect="dark" size="small">
            {{ row.fileType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="160">
        <template #default="{ row }">
          <span v-if="!row.tags || row.tags.length === 0" class="no-tags">-</span>
          <el-tag
            v-for="tag in row.tags"
            :key="tag.tagId"
            :color="getTagColor(tag.tagName)"
            effect="dark"
            size="small"
            class="asset-tag"
          >
            {{ tag.tagName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="当前版本" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" round>v{{ row.currentVersion || 1 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上传时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="$router.push(`/assets/${row.assetId}`)">
            <el-icon><View /></el-icon>
            详情
          </el-button>
          <el-button type="primary" link size="small" @click="handleDownload(row)">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <el-button type="warning" link size="small" @click="$router.push(`/assets/${row.assetId}`)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-popconfirm title="确定删除该资产？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete(row)">
            <template #reference>
              <el-button type="danger" link size="small">
                <el-icon><Delete /></el-icon>
                删除
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
        :page-sizes="[12, 24, 36]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Download, Edit, Delete } from '@element-plus/icons-vue'
import { getList, deleteAsset, download } from '../api/asset'
import { getList as getTagList } from '../api/tag'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const searchFileType = ref('')
const searchTagId = ref(null)
const tagList = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 12,
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

const tagColorPalette = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#9b59b6', '#1abc9c', '#e67e22', '#3498db', '#2ecc71',
  '#e74c3c', '#f39c12', '#00bcd4', '#8e44ad', '#27ae60',
  '#d35400', '#2980b9', '#c0392b', '#16a085', '#f1c40f'
]

const getTagColor = (tagName) => {
  if (!tagName) return '#909399'
  let hash = 0
  for (let i = 0; i < tagName.length; i++) {
    hash = tagName.charCodeAt(i) + ((hash << 5) - hash)
  }
  return tagColorPalette[Math.abs(hash) % tagColorPalette.length]
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

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchFileType.value) params.fileType = searchFileType.value
    if (searchTagId.value) params.tagIds = [searchTagId.value]

    const res = await getList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '加载资产列表失败')
  } finally {
    loading.value = false
  }
}

const handleDownload = async (row) => {
  try {
    const res = await download(row.assetId)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    const ext = row.fileType ? '.' + row.fileType.toLowerCase() : ''
    link.setAttribute('download', (row.assetName || 'download') + ext)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '下载失败')
  }
}

const handleDelete = async (row) => {
  try {
    const res = await deleteAsset(row.assetId)
    if (res.code === 200) {
      ElMessage.success(res.message || '删除成功')
      handleSearch()
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

onMounted(async () => {
  try {
    const res = await getTagList()
    if (res.code === 200) {
      tagList.value = res.data || []
    }
  } catch {
    tagList.value = []
  }
  handleSearch()
})
</script>

<style scoped>
.asset-list {
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

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.filter-select {
  width: 150px;
}

.asset-tag {
  margin-right: 4px;
  margin-bottom: 2px;
}

.no-tags {
  color: #c0c4cc;
  font-size: 13px;
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
