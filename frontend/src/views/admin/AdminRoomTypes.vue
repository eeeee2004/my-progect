<template>
  <div>
    <div class="card-header">
      <h2>房型管理</h2>
      <el-button type="primary" @click="openDialog()">新增房型</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width:50px;height:50px;border-radius:6px" fit="cover" :preview-src-list="[row.imageUrl]" />
          <span v-else style="color:#AAB7C4;font-size:12px">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="50" />
      <el-table-column prop="name" label="房型名称" width="100" />
      <el-table-column prop="bedType" label="床型" width="70" />
      <el-table-column prop="area" label="面积(㎡)" width="80" />
      <el-table-column prop="basePrice" label="基础价格" width="100">
        <template #default="{ row }">¥{{ row.basePrice }}</template>
      </el-table-column>
      <el-table-column prop="facilities" label="设施" min-width="140" />
      <el-table-column prop="description" label="描述" min-width="140" />
      <el-table-column prop="sortOrder" label="排序" width="60" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="房型图片">
          <div style="display:flex;align-items:center;gap:12px">
            <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:100px;height:75px;border-radius:8px;border:1px solid #E8ECF1" fit="cover" />
            <div>
              <el-upload
                :before-upload="beforeUpload"
                :http-request="handleUpload"
                :show-file-list="false"
                accept="image/jpeg,image/png,image/gif,image/webp"
              >
                <el-button size="small" :loading="uploading">
                  {{ form.imageUrl ? '更换图片' : '上传图片' }}
                </el-button>
              </el-upload>
              <p style="color:#AAB7C4;font-size:11px;margin-top:6px">JPG/PNG/GIF/WEBP, ≤5MB</p>
              <el-button v-if="form.imageUrl" size="small" type="danger" text @click="form.imageUrl = ''">清除</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="房型名称">
          <el-input v-model="form.name" placeholder="如：标准间、豪华套房" />
        </el-form-item>
        <el-form-item label="床型">
          <el-select v-model="form.bedType" placeholder="选择床型" style="width:100%">
            <el-option label="大床" value="大床" />
            <el-option label="双床" value="双床" />
            <el-option label="单人床" value="单人床" />
            <el-option label="三床" value="三床" />
            <el-option label="圆床" value="圆床" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积(㎡)">
          <el-input-number v-model="form.area" :min="0" :precision="2" style="width:200px" />
        </el-form-item>
        <el-form-item label="基础价格">
          <el-input-number v-model="form.basePrice" :min="0" :precision="2" style="width:200px" />
          <span style="margin-left:8px;color:#AAB7C4;font-size:12px">元/晚</span>
        </el-form-item>
        <el-form-item label="容纳人数">
          <el-input-number v-model="form.capacity" :min="1" :max="10" style="width:120px" />
          <span style="margin-left:8px;color:#AAB7C4;font-size:12px">人</span>
        </el-form-item>
        <el-form-item label="入住时间">
          <el-time-picker v-model="form.checkInTime" format="HH:mm" value-format="HH:mm" placeholder="入住时间" />
        </el-form-item>
        <el-form-item label="退房时间">
          <el-time-picker v-model="form.checkOutTime" format="HH:mm" value-format="HH:mm" placeholder="退房时间" />
        </el-form-item>
        <el-form-item label="早餐">
          <el-select v-model="form.breakfast" style="width:100%">
            <el-option label="不含早" value="不含早" />
            <el-option label="含单早" value="含单早" />
            <el-option label="含双早" value="含双早" />
            <el-option label="含三早" value="含三早" />
          </el-select>
        </el-form-item>
        <el-form-item label="窗型">
          <el-select v-model="form.windowType" style="width:100%">
            <el-option label="有窗" value="有窗" />
            <el-option label="落地窗" value="落地窗" />
            <el-option label="无窗" value="无窗" />
            <el-option label="飘窗" value="飘窗" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层分布">
          <el-input v-model="form.floorInfo" placeholder="如：1-3层、3层" />
        </el-form-item>
        <el-form-item label="取消政策">
          <el-input v-model="form.cancelPolicy" type="textarea" :rows="2" placeholder="如：入住前24小时可免费取消" />
        </el-form-item>
        <el-form-item label="设施">
          <el-input v-model="form.facilities" placeholder="以逗号分隔，如：WiFi,空调,电视" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="简明描述房型特色" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width:120px" />
          <span style="margin-left:8px;color:#AAB7C4;font-size:12px">数值越小越靠前</span>
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
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '', bedType: '', area: 0, basePrice: 0, capacity: 2,
  checkInTime: '14:00', checkOutTime: '12:00', breakfast: '不含早',
  windowType: '', floorInfo: '', cancelPolicy: '',
  facilities: '', description: '', sortOrder: 0, imageUrl: ''
})

const dialogTitle = computed(() => editingId.value ? '编辑房型' : '新增房型')

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.getRoomTypes()
    list.value = res.data || []
  } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, { ...row })
  } else {
    editingId.value = null
    Object.assign(form, { name: '', bedType: '', area: 0, basePrice: 0, capacity: 2,
      checkInTime: '14:00', checkOutTime: '12:00', breakfast: '不含早',
      windowType: '', floorInfo: '', cancelPolicy: '',
      facilities: '', description: '', sortOrder: 0, imageUrl: '' })
  }
  dialogVisible.value = true
}

function beforeUpload(file) {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/pjpeg', 'image/png', 'image/x-png', 'image/gif', 'image/webp']
  const validExts = ['.jpg', '.jpeg', '.png', '.gif', '.webp']
  const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()
  const typeOk = validTypes.includes(file.type)
  const extOk = validExts.includes(ext)
  if (!typeOk && !extOk) {
    ElMessage.error('仅支持 JPG/PNG/GIF/WEBP 格式')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const res = await request.post('/admin/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.imageUrl = res.data.url
    ElMessage.success('上传成功')
  } catch (err) {
    const msg = err?.response?.data?.message || err?.message || '上传失败'
    ElMessage.error(msg)
  } finally { uploading.value = false }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await adminAPI.updateRoomType(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await adminAPI.createRoomType(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}

async function toggleStatus(row) {
  await adminAPI.updateRoomType(row.id, { ...row, status: row.status === 1 ? 0 : 1 })
  loadData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该房型？', '提示', { type: 'warning' })
    await adminAPI.deleteRoomType(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>
