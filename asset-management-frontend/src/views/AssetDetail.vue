<template>
  <div class="asset-detail">
    <div class="page-header">
      <el-button link type="primary" @click="$router.push('/assets')">
        <el-icon><ArrowLeft /></el-icon>
        返回资产列表
      </el-button>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else-if="asset">
      <el-card shadow="hover" class="detail-card">
        <div class="asset-header">
          <div class="asset-icon">
            <el-icon :size="48">
              <Document v-if="isDocType" />
              <PictureFilled v-else-if="isImageType" />
              <VideoCameraFilled v-else-if="isVideoType" />
              <Document v-else />
            </el-icon>
          </div>
          <div class="asset-info">
            <h3 class="asset-name">{{ asset.assetName }}</h3>
            <div class="asset-meta">
              <el-tag :color="getFileTypeColor(asset.fileType)" effect="dark" size="small">
                {{ asset.fileType }}
              </el-tag>
              <span class="meta-item">{{ formatFileSize(asset.fileSize) }}</span>
              <span class="meta-item">v{{ asset.currentVersion || 1 }}</span>
              <span class="meta-item">{{ formatDate(asset.createdAt) }}</span>
            </div>
            <div class="asset-tags" v-if="tags.length > 0">
              <el-tag
                v-for="tag in tags"
                :key="tag.tagId"
                size="small"
                class="tag-item"
              >
                {{ tag.tagName }}
              </el-tag>
            </div>
          </div>
          <div class="asset-actions">
            <el-button type="primary" @click="handleDownload">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button @click="showVersionDialog = true">
              <el-icon><Upload /></el-icon>
              上传新版本
            </el-button>
            <el-button type="warning" @click="openEditDialog">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" @click="handleDelete">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>

        <el-divider />

        <el-descriptions :column="2" border>
          <el-descriptions-item label="资产名称">{{ asset.assetName }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">
            <el-tag :color="getFileTypeColor(asset.fileType)" effect="dark" size="small">
              {{ asset.fileType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(asset.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="当前版本">v{{ asset.currentVersion || 1 }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ categoryPath }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ formatDate(asset.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ asset.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="hover" class="version-card">
        <template #header>
          <span class="card-title">版本历史</span>
        </template>
        <el-table :data="versions" stripe v-loading="versionLoading" empty-text="暂无版本记录">
          <el-table-column label="版本号" width="100" align="center">
            <template #default="{ row, $index }">
              <el-tag size="small" round>v{{ row.versionNumber || versions.length - $index }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="版本说明" min-width="200" show-overflow-tooltip />
          <el-table-column label="文件大小" width="130">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="上传者" width="120">
            <template #default="{ row }">
              {{ row.uploadUserName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="上传时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleDownloadVersion(row)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <el-dialog v-model="editDialogVisible" title="编辑资产" width="520px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top">
        <el-form-item label="资产名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入资产名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-tree-select
            v-model="editForm.categoryId"
            :data="categoryTree"
            placeholder="请选择分类"
            check-strictly
            style="width: 100%"
            :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="editForm.tagIds"
            multiple
            placeholder="请选择标签（可多选）"
            style="width: 100%"
          >
            <el-option
              v-for="tag in tagList"
              :key="tag.tagId"
              :label="tag.tagName"
              :value="tag.tagId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入资产描述"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showVersionDialog" title="上传新版本" width="480px" destroy-on-close>
      <el-upload
        ref="versionUploadRef"
        class="version-upload"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleVersionFileChange"
        :before-upload="versionBeforeUpload"
        :limit="1"
        :accept="acceptTypes"
      >
        <el-icon class="upload-icon" :size="40"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p>将文件拖到此处，或<em>点击上传</em></p>
          <p class="upload-tip">单文件最大 10MB</p>
        </div>
      </el-upload>

      <div v-if="versionFile" class="version-file-info">
        <p><strong>文件：</strong>{{ versionFile.name }} ({{ formatFileSize(versionFile.size) }})</p>
      </div>

      <el-input
        v-model="versionDescription"
        type="textarea"
        :rows="3"
        placeholder="请输入版本说明（可选）"
        maxlength="200"
        show-word-limit
        style="margin-top: 16px;"
      />

      <template #footer>
        <el-button @click="showVersionDialog = false">取消</el-button>
        <el-button type="primary" :loading="versionSubmitting" @click="handleVersionSubmit">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Document, PictureFilled, VideoCameraFilled,
  Download, Upload, Edit, Delete, UploadFilled
} from '@element-plus/icons-vue'
import {
  getDetail, update, deleteAsset, download,
  getVersions, downloadVersion, uploadVersion
} from '../api/asset'
import { getTree } from '../api/category'
import { getList } from '../api/tag'

const route = useRoute()
const router = useRouter()
const assetId = route.params.id

const loading = ref(false)
const asset = ref(null)
const versions = ref([])
const tags = ref([])
const categoryPath = ref('-')
const versionLoading = ref(false)
const categoryTree = ref([])
const tagList = ref([])

const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editSubmitting = ref(false)
const editForm = reactive({
  name: '',
  categoryId: null,
  tagIds: [],
  description: ''
})
const editRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const showVersionDialog = ref(false)
const versionUploadRef = ref(null)
const versionFile = ref(null)
const versionDescription = ref('')
const versionSubmitting = ref(false)

const acceptTypes = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.jpg,.jpeg,.png,.gif,.bmp,.svg,.webp,.mp4,.avi,.mov,.mkv,.wmv,.flv'

const isDocType = computed(() => {
  const t = (asset.value?.fileType || '').toUpperCase()
  return ['PDF', 'DOC', 'DOCX', 'XLS', 'XLSX', 'PPT', 'PPTX', 'TXT'].includes(t)
})
const isImageType = computed(() => {
  const t = (asset.value?.fileType || '').toUpperCase()
  return ['JPG', 'JPEG', 'PNG', 'GIF', 'BMP', 'SVG', 'WEBP'].includes(t)
})
const isVideoType = computed(() => {
  const t = (asset.value?.fileType || '').toUpperCase()
  return ['MP4', 'AVI', 'MOV', 'MKV', 'WMV', 'FLV'].includes(t)
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

const loadAsset = async () => {
  loading.value = true
  try {
    const res = await getDetail(assetId)
    if (res.code === 200) {
      asset.value = res.data
      tags.value = res.data.tags || []
      if (res.data.categoryPath) {
        categoryPath.value = Array.isArray(res.data.categoryPath)
          ? res.data.categoryPath.join(' / ')
          : res.data.categoryPath
      } else {
        categoryPath.value = '-'
      }
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '加载资产详情失败')
  } finally {
    loading.value = false
  }
}

const loadVersions = async () => {
  versionLoading.value = true
  try {
    const res = await getVersions(assetId)
    if (res.code === 200) {
      versions.value = res.data || []
    }
  } catch {
    versions.value = []
  } finally {
    versionLoading.value = false
  }
}

const handleDownload = async () => {
  try {
    const res = await download(assetId)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    const ext = asset.value?.fileType ? '.' + asset.value.fileType.toLowerCase() : ''
    link.setAttribute('download', (asset.value?.assetName || 'download') + ext)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '下载失败')
  }
}

const handleDownloadVersion = async (row) => {
  try {
    const res = await downloadVersion(row.versionId)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    const ext = asset.value?.fileType ? '.' + asset.value.fileType.toLowerCase() : ''
    link.setAttribute('download', (asset.value?.assetName || 'download') + ext)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '下载失败')
  }
}

const openEditDialog = () => {
  editForm.name = asset.value?.assetName || ''
  editForm.categoryId = asset.value?.categoryId || null
  editForm.description = asset.value?.description || ''
  editForm.tagIds = (tags.value || []).map(t => t.tagId)
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  editSubmitting.value = true
  try {
    const data = {
      name: editForm.name,
      categoryId: editForm.categoryId,
      tagIds: editForm.tagIds,
      description: editForm.description
    }
    const res = await update(assetId, data)
    if (res.code === 200) {
      ElMessage.success(res.message || '更新成功')
      editDialogVisible.value = false
      loadAsset()
      loadVersions()
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '更新失败')
  } finally {
    editSubmitting.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定删除该资产？删除后将移入回收站。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteAsset(assetId)
    if (res.code === 200) {
      ElMessage.success(res.message || '删除成功')
      router.push('/assets')
    }
  } catch {
    // user cancelled
  }
}

const versionBeforeUpload = (file) => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

const handleVersionFileChange = (uploadFile) => {
  versionFile.value = uploadFile.raw
}

const handleVersionSubmit = async () => {
  if (!versionFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  versionSubmitting.value = true
  try {
    const formData = new FormData()
    formData.append('file', versionFile.value)
    formData.append('description', versionDescription.value)

    const res = await uploadVersion(assetId, formData)
    if (res.code === 200) {
      ElMessage.success(res.message || '新版本上传成功')
      showVersionDialog.value = false
      versionFile.value = null
      versionDescription.value = ''
      if (versionUploadRef.value) {
        versionUploadRef.value.clearFiles()
      }
      loadAsset()
      loadVersions()
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '版本上传失败')
  } finally {
    versionSubmitting.value = false
  }
}

onMounted(async () => {
  await loadAsset()
  await loadVersions()

  try {
    const res1 = await getTree()
    if (res1.code === 200) {
      categoryTree.value = res1.data || []
    }
  } catch {
    categoryTree.value = []
  }
  try {
    const res2 = await getList()
    if (res2.code === 200) {
      tagList.value = res2.data || []
    }
  } catch {
    tagList.value = []
  }
})
</script>

<style scoped>
.asset-detail {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.loading-wrapper {
  padding: 40px;
}

.detail-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.asset-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  flex-wrap: wrap;
}

.asset-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background: rgba(64, 158, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  flex-shrink: 0;
}

.asset-info {
  flex: 1;
  min-width: 200px;
}

.asset-name {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.asset-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.meta-item {
  font-size: 13px;
  color: #909399;
}

.asset-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-item {
  cursor: default;
}

.asset-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.version-card {
  border-radius: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.version-upload {
  width: 100%;
}

.version-upload :deep(.el-upload-dragger) {
  padding: 40px 20px;
}

.upload-icon {
  color: #c0c4cc;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin: 6px 0 0 0;
}

.version-file-info {
  margin-top: 16px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  color: #303133;
}

.version-file-info p {
  margin: 0;
}

.version-file-info strong {
  color: #606266;
}
</style>
