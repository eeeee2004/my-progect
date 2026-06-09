<template>
  <div>
    <div class="card-header">
      <h2>房价日历</h2>
      <el-button type="primary" @click="openDialog()">新增规则</el-button>
    </div>
    <el-table :data="list" v-loading="loading" size="small">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="房型" width="120">
        <template #default="{ row }">{{ getRoomTypeName(row.roomTypeId) }}</template>
      </el-table-column>
      <el-table-column label="规则类型" width="100">
        <template #default="{ row }">
          <el-tag :type="ruleTypeTag(row.ruleType)">{{ ruleTypeMap[row.ruleType] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column label="价格系数" width="90">
        <template #default="{ row }">
          <span :style="{ color: row.priceFactor > 1 ? '#E67E22' : row.priceFactor < 1 ? '#27AE60' : '#5D6D7E' }">×{{ row.priceFactor }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="70" />
      <el-table-column label="状态" width="70">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="房型">
          <el-select v-model="form.roomTypeId">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="form.ruleType">
            <el-option label="周末溢价" value="WEEKEND" />
            <el-option label="节假日" value="HOLIDAY" />
            <el-option label="促销折扣" value="PROMOTION" />
            <el-option label="特殊定价" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="价格系数">
          <el-input-number v-model="form.priceFactor" :min="0.1" :max="5" :step="0.05" :precision="2" />
          <span style="margin-left:8px;color:#909399;font-size:12px">1.0=原价, 1.2=上浮20%, 0.85=85折</span>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" />
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
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const roomTypes = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const ruleTypeMap = { WEEKEND: '周末溢价', HOLIDAY: '节假日', PROMOTION: '促销折扣', SPECIAL: '特殊定价' }
function ruleTypeTag(t) { return { WEEKEND: 'warning', HOLIDAY: 'danger', PROMOTION: 'success', SPECIAL: 'primary' }[t] || 'info' }
function getRoomTypeName(id) { return roomTypes.value.find(t => t.id === id)?.name || '' }

const form = reactive({ roomTypeId: null, ruleType: 'WEEKEND', startDate: '', endDate: '', priceFactor: 1.0, priority: 0, status: 1 })
const dialogTitle = computed(() => editingId.value ? '编辑房价规则' : '新增房价规则')

async function loadData() {
  loading.value = true
  try {
    const [rulesRes, typesRes] = await Promise.all([
      request.get('/admin/price-calendar', { params: { page: 1, pageSize: 50 } }),
      request.get('/admin/room-types')
    ])
    list.value = rulesRes.data.records || []
    roomTypes.value = typesRes.data || []
  } catch {} finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, row)
  } else {
    editingId.value = null
    Object.assign(form, { roomTypeId: roomTypes.value[0]?.id || null, ruleType: 'WEEKEND', startDate: '', endDate: '', priceFactor: 1.0, priority: 0, status: 1 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editingId.value) {
      await request.put(`/admin/price-calendar/${editingId.value}`, form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/price-calendar', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await request.delete(`/admin/price-calendar/${row.id}`); ElMessage.success('已删除'); loadData() } catch {}
}

onMounted(loadData)
</script>
