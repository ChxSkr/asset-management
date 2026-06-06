<template>
  <div class="category-manage">
    <div class="page-header">
      <h2>分类管理</h2>
      <p class="subtitle">管理资产分类树结构</p>
    </div>

    <div class="content-wrapper">
      <div class="tree-panel">
        <div class="panel-header">
          <el-button type="primary" @click="handleAddRoot">
            <el-icon><Plus /></el-icon>
            新增根分类
          </el-button>
          <el-button
            v-if="selectedNode"
            type="success"
            @click="handleAddChild"
          >
            <el-icon><Plus /></el-icon>
            添加子分类
          </el-button>
          <el-button
            v-if="selectedNode"
            type="warning"
            @click="handleEdit"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            v-if="selectedNode"
            type="danger"
            @click="handleDelete"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>

        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="treeProps"
          node-key="categoryId"
          highlight-current
          default-expand-all
          @node-click="handleNodeClick"
          @node-contextmenu="handleContextMenu"
        >
          <template #default="{ node, data }">
            <span class="tree-node-label">
              <el-icon><Folder /></el-icon>
              <span>{{ node.label }}</span>
              <span class="sort-tag">排序: {{ data.sortOrder }}</span>
            </span>
          </template>
        </el-tree>
      </div>
    </div>

    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
    >
      <div class="context-item" @click="handleContextAddChild">
        <el-icon><Plus /></el-icon> 添加子分类
      </div>
      <div class="context-item" @click="handleContextEdit">
        <el-icon><Edit /></el-icon> 编辑
      </div>
      <div class="context-item danger" @click="handleContextDelete">
        <el-icon><Delete /></el-icon> 删除
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" placeholder="排序号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Folder } from '@element-plus/icons-vue'
import * as categoryApi from '../api/category'

const treeRef = ref(null)
const treeData = ref([])
const selectedNode = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref('add')
const formRef = ref(null)
const submitting = ref(false)
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuData = ref(null)

const treeProps = {
  children: 'children',
  label: 'categoryName'
}

const form = reactive({
  categoryName: '',
  sortOrder: 0
})

const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

const loadTree = async () => {
  try {
    const res = await categoryApi.getTree()
    if (res.code === 200) {
      treeData.value = res.data || []
    }
  } catch {
    ElMessage.error('加载分类树失败')
  }
}

const handleNodeClick = (data, node) => {
  selectedNode.value = node
}

const handleContextMenu = (event, data, node) => {
  event.preventDefault()
  contextMenuData.value = { data, node }
  selectedNode.value = node
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

const handleContextAddChild = () => {
  closeContextMenu()
  handleAddChild()
}

const handleContextEdit = () => {
  closeContextMenu()
  handleEdit()
}

const handleContextDelete = () => {
  closeContextMenu()
  handleDelete()
}

const handleAddRoot = () => {
  dialogMode.value = 'addRoot'
  dialogTitle.value = '新增根分类'
  form.categoryName = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const handleAddChild = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个父分类')
    return
  }
  dialogMode.value = 'addChild'
  dialogTitle.value = '添加子分类'
  form.categoryName = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const handleEdit = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择要编辑的分类')
    return
  }
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑分类'
  form.categoryName = selectedNode.value.data.categoryName
  form.sortOrder = selectedNode.value.data.sortOrder || 0
  dialogVisible.value = true
}

const handleDelete = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择要删除的分类')
    return
  }
  const data = selectedNode.value.data
  ElMessageBox.confirm(
    `确定删除分类"${data.categoryName}"？` + (data.children && data.children.length > 0 ? '（注意：含有子分类时删除可能失败）' : ''),
    '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const res = await categoryApi.remove(data.categoryId)
      if (res.code === 200) {
        ElMessage.success(res.message || '删除成功')
        selectedNode.value = null
        loadTree()
      }
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || '删除失败，请确认该分类下无子分类')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      categoryName: form.categoryName,
      sortOrder: form.sortOrder
    }
    if (dialogMode.value === 'edit') {
      payload.parentId = selectedNode.value.parent?.data?.categoryId || null
      const res = await categoryApi.update(selectedNode.value.data.categoryId, payload)
      if (res.code === 200) {
        ElMessage.success(res.message || '编辑成功')
        dialogVisible.value = false
        selectedNode.value = null
        loadTree()
      }
    } else if (dialogMode.value === 'addRoot') {
      const res = await categoryApi.add(payload)
      if (res.code === 200) {
        ElMessage.success(res.message || '新增成功')
        dialogVisible.value = false
        loadTree()
      }
    } else if (dialogMode.value === 'addChild') {
      payload.parentId = selectedNode.value.data.categoryId
      const res = await categoryApi.add(payload)
      if (res.code === 200) {
        ElMessage.success(res.message || '新增成功')
        dialogVisible.value = false
        loadTree()
      }
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadTree()
  document.addEventListener('click', closeContextMenu)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeContextMenu)
})
</script>

<style scoped>
.category-manage {
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

.content-wrapper {
  display: flex;
  gap: 20px;
}

.tree-panel {
  flex: 1;
  min-width: 360px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 16px;
}

.panel-header {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tree-node-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.sort-tag {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.context-menu {
  position: fixed;
  z-index: 3000;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 160px;
}

.context-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
  transition: background 0.2s;
}

.context-item:hover {
  background: #f5f7fa;
}

.context-item.danger {
  color: #f56c6c;
}

.context-item.danger:hover {
  background: #fef0f0;
}
</style>
