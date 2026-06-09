<template>
  <div>
    <div class="card-header">
      <h2>房间管理</h2>
      <el-button type="primary" @click="openDialog()">新增房间</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="roomNumber" label="房间号" width="100" />
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column label="房型" width="150">
        <template #default="{ row }">{{ getRoomTypeName(row.roomTypeId) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="房间号">
          <el-input v-model="form.roomNumber" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input-number v-model="form.floor" :min="1" />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="form.roomTypeId" placeholder="选择房型">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="空闲" value="AVAILABLE" />
            <el-option label="已预订" value="BOOKED" />
            <el-option label="已入住" value="OCCUPIED" />
            <el-option label="待清洁" value="CLEANING" />
            <el-option label="维修中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
const roomTypes = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const statusMap = { AVAILABLE: '空闲', BOOKED: '已预订', OCCUPIED: '已入住', CLEANING: '待清洁', MAINTENANCE: '维修中' }

const form = reactive({ roomNumber: '', floor: 1, roomTypeId: null, status: 'AVAILABLE', remark: '' })

const dialogTitle = computed(() => editingId.value ? '编辑房间' : '新增房间')

function statusType(s) {
  return { AVAILABLE: 'success', BOOKED: 'warning', OCCUPIED: 'danger', CLEANING: 'info', MAINTENANCE: 'danger' }[s] || 'info'
}

function getRoomTypeName(id) {
  return roomTypes.value.find(t => t.id === id)?.name || ''
}

async function loadData() {
  loading.value = true
  try {
    const [roomsRes, typesRes] = await Promise.all([adminAPI.getAllRooms({ page: 1, pageSize: 50 }), adminAPI.getRoomTypes()])
    list.value = roomsRes.data.records || []
    roomTypes.value = typesRes.data || []
  } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, row)
  } else {
    editingId.value = null
    Object.assign(form, { roomNumber: '', floor: 1, roomTypeId: null, status: 'AVAILABLE', remark: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editingId.value) {
      await adminAPI.updateRoom(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await adminAPI.createRoom(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该房间？', '提示', { type: 'warning' })
    await adminAPI.deleteRoom(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>
