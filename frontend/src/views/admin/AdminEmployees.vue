<template>
  <div>
    <div class="card-header">
      <h2>员工管理</h2>
      <el-button type="primary" @click="openDialog()">新增员工</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.roleType === 'ADMIN' ? 'danger' : 'primary'">
            {{ row.roleType === 'ADMIN' ? '管理员' : '前台' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password :placeholder="editingId ? '留空则不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleType">
            <el-option label="前台" value="FRONT_DESK" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const form = reactive({ username: '', password: '', realName: '', phone: '', email: '', roleType: 'FRONT_DESK', status: 1 })

const dialogTitle = computed(() => editingId.value ? '编辑员工' : '新增员工')

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.getEmployees()
    list.value = res.data || []
  } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, { ...row, password: '' })
  } else {
    editingId.value = null
    Object.assign(form, { username: '', password: '', realName: '', phone: '', email: '', roleType: 'FRONT_DESK', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const data = { ...form }
    if (editingId.value && !data.password) delete data.password
    if (editingId.value) {
      await adminAPI.updateEmployee(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await adminAPI.createEmployee(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该员工？', '提示', { type: 'warning' })
    await adminAPI.deleteEmployee(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>
