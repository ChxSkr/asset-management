<template>
  <div class="asset-upload">
    <div class="page-header">
      <h2>上传资产</h2>
      <p class="subtitle">将文件上传到数字资产管理系统</p>
    </div>

    <el-card shadow="hover" class="upload-card">
      <template v-if="!selectedFile">
        <el-upload
          ref="uploadRef"
          class="upload-area"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :before-upload="beforeUpload"
          :limit="1"
          :accept="acceptTypes"
        >
          <el-icon class="upload-icon" :size="48"><UploadFilled /></el-icon>
          <div class="upload-text">
            <p class="upload-text-primary">将文件拖到此处，或<em>点击上传</em></p>
            <p class="upload-text-secondary">支持 PDF、Word、Excel、图片、视频等格式，单文件最大 10MB</p>
          </div>
        </el-upload>
      </template>

      <template v-else>
        <div class="file-info-box">
          <div class="file-info-icon">
            <el-icon :size="36"><Document /></el-icon>
          </div>
          <div class="file-info-detail">
            <p class="file-info-name">{{ selectedFile.name }}</p>
            <p class="file-info-size">{{ formatFileSize(selectedFile.size) }}</p>
          </div>
          <el-button type="danger" link @click="clearFile">
            <el-icon><Delete /></el-icon>
            移除
          </el-button>
        </div>

        <el-divider />

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="default">
          <el-form-item label="资产名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入资产名称" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item label="分类" prop="categoryId">
            <el-tree-select
              v-model="form.categoryId"
              :data="categoryTree"
              placeholder="请选择分类"
              check-strictly
              style="width: 100%"
              :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
            />
          </el-form-item>

          <el-form-item label="标签">
            <el-select
              v-model="form.tagIds"
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

          <el-form-item label="描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入资产描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="submitting" size="large" @click="handleSubmit">
              {{ submitting ? '上传中...' : '提交上传' }}
            </el-button>
            <el-button size="large" @click="clearFile">取消</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document, Delete } from '@element-plus/icons-vue'
import { upload } from '../api/asset'
import { getTree } from '../api/category'
import { getList } from '../api/tag'

const router = useRouter()
const formRef = ref(null)
const uploadRef = ref(null)
const selectedFile = ref(null)
const submitting = ref(false)
const categoryTree = ref([])
const tagList = ref([])

const acceptTypes = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.jpg,.jpeg,.png,.gif,.bmp,.svg,.webp,.mp4,.avi,.mov,.mkv,.wmv,.flv'

const form = reactive({
  name: '',
  categoryId: null,
  tagIds: [],
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入资产名称', trigger: 'blur' },
    { min: 1, max: 100, message: '名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

const formatFileSize = (size) => {
  if (!size && size !== 0) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const beforeUpload = (file) => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  const allowedExtensions = acceptTypes.split(',').map(ext => ext.trim().toLowerCase())
  const fileExt = '.' + file.name.split('.').pop().toLowerCase()
  if (!allowedExtensions.includes(fileExt)) {
    ElMessage.error('不支持的文件类型')
    return false
  }
  return false
}

const handleFileChange = (uploadFile) => {
  selectedFile.value = uploadFile.raw
  if (!form.name) {
    const dotIndex = uploadFile.name.lastIndexOf('.')
    form.name = dotIndex > 0 ? uploadFile.name.substring(0, dotIndex) : uploadFile.name
  }
}

const clearFile = () => {
  selectedFile.value = null
  form.name = ''
  form.categoryId = null
  form.tagIds = []
  form.description = ''
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('name', form.name)
    formData.append('categoryId', form.categoryId)
    formData.append('tagIds', form.tagIds.join(','))
    formData.append('description', form.description)

    const res = await upload(formData)
    if (res.code === 200) {
      ElMessage.success(res.message || '上传成功')
      router.push('/assets')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '上传失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
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
.asset-upload {
  padding: 20px;
  max-width: 720px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
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

.upload-card {
  border-radius: 12px;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  padding: 60px 20px;
  border-radius: 12px;
  border: 2px dashed #dcdfe6;
  transition: border-color 0.3s;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
}

.upload-icon {
  color: #c0c4cc;
  margin-bottom: 12px;
}

.upload-text-primary {
  font-size: 16px;
  color: #606266;
  margin: 0 0 8px 0;
}

.upload-text-primary em {
  color: #409eff;
  font-style: normal;
}

.upload-text-secondary {
  font-size: 13px;
  color: #c0c4cc;
  margin: 0;
}

.file-info-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.file-info-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-info-detail {
  flex: 1;
}

.file-info-name {
  margin: 0;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.file-info-size {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #909399;
}
</style>
