<template>
  <div class="search-page">
    <div class="page-header">
      <h2>全局搜索</h2>
      <p class="subtitle">搜索所有资产文件</p>
    </div>

    <div class="search-bar">
      <el-input
        ref="searchInputRef"
        v-model="params.keyword"
        size="large"
        placeholder="输入关键词搜索资产..."
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="filter-bar">
      <el-select
        v-model="params.fileType"
        placeholder="文件类型"
        clearable
        class="filter-item"
      >
        <el-option label="全部" value="" />
        <el-option label="PDF" value="PDF" />
        <el-option label="DOC/DOCX" value="DOC" />
        <el-option label="图片" value="IMAGE" />
        <el-option label="视频" value="VIDEO" />
        <el-option label="其他" value="OTHER" />
      </el-select>

      <el-tree-select
        v-model="params.categoryId"
        :data="categoryTree"
        placeholder="选择分类"
        clearable
        check-strictly
        class="filter-item"
        :props="treeSelectProps"
      />

      <el-select
        v-model="params.tagIds"
        placeholder="选择标签"
        multiple
        clearable
        class="filter-item"
      >
        <el-option
          v-for="tag in tagList"
          :key="tag.id"
          :label="tag.tagName"
          :value="tag.id"
        />
      </el-select>

      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="filter-item"
        value-format="YYYY-MM-DD"
      />

      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe class="data-table" empty-text="暂无搜索结果">
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
      <el-table-column prop="categoryName" label="分类" width="120">
        <template #default="{ row }">
          {{ row.categoryName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="上传时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/assets/${row.assetId}`)">
            <el-icon><View /></el-icon>
            详情
          </el-button>
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
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import * as searchApi from '../api/search'
import * as categoryApi from '../api/category'
import * as tagApi from '../api/tag'

const searchInputRef = ref(null)
const loading = ref(false)
const tableData = ref([])
const categoryTree = ref([])
const tagList = ref([])
const dateRange = ref(null)

const treeSelectProps = {
  label: 'categoryName',
  value: 'categoryId',
  children: 'children'
}

const params = reactive({
  keyword: '',
  fileType: '',
  categoryId: null,
  tagIds: [],
  startDate: '',
  endDate: ''
})

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

const handleSearch = async () => {
  loading.value = true
  try {
    const queryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (params.keyword) queryParams.keyword = params.keyword
    if (params.fileType) queryParams.fileType = params.fileType
    if (params.categoryId) queryParams.categoryId = params.categoryId
    if (params.tagIds && params.tagIds.length > 0) queryParams.tagIds = params.tagIds.join(',')
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startDate = dateRange.value[0]
      queryParams.endDate = dateRange.value[1]
    }

    const res = await searchApi.search(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  params.keyword = ''
  params.fileType = ''
  params.categoryId = null
  params.tagIds = []
  dateRange.value = null
  pagination.page = 1
  handleSearch()
}

const loadFilters = async () => {
  try {
    const [catRes, tagRes] = await Promise.all([
      categoryApi.getTree(),
      tagApi.getList()
    ])
    if (catRes.code === 200) {
      categoryTree.value = catRes.data || []
    }
    if (tagRes.code === 200) {
      tagList.value = tagRes.data || []
    }
  } catch {
    // ignore
  }
}

onMounted(async () => {
  await loadFilters()
  handleSearch()
  await nextTick()
  searchInputRef.value?.focus()
})
</script>

<style scoped>
.search-page {
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

.search-bar {
  margin-bottom: 16px;
}

.search-bar .el-input {
  max-width: 640px;
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.filter-item {
  width: 180px;
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
