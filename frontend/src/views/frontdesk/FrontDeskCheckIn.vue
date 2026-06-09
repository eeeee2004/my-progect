<template>
  <div>
    <div class="card-header"><h2>入住办理</h2></div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="有预订入住" name="booking">
        <h4 style="margin-bottom:12px">今日预抵客人</h4>
        <el-table :data="todayArrivals" @row-click="selectArrival" highlight-current-row v-loading="loadingArrivals">
          <el-table-column prop="orderNo" label="订单编号" width="160" />
          <el-table-column prop="guestName" label="入住人" width="100" />
          <el-table-column prop="guestPhone" label="手机号" width="130" />
          <el-table-column prop="checkInDate" label="入住日期" width="120" />
          <el-table-column prop="checkOutDate" label="离店日期" width="120" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click.stop="quickCheckIn(row)">一键入住</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="散客登记" name="walkin">
        <el-form :model="walkinForm" :rules="walkinRules" ref="walkinFormRef" label-width="100px" style="max-width:500px">
          <el-form-item label="房间号" prop="roomId">
            <el-select v-model="walkinForm.roomId" placeholder="选择房间">
              <el-option v-for="r in availableRooms" :key="r.id" :label="r.roomNumber" :value="r.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="入住人姓名" prop="guestName">
            <el-input v-model="walkinForm.guestName" />
          </el-form-item>
          <el-form-item label="手机号" prop="guestPhone">
            <el-input v-model="walkinForm.guestPhone" />
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="walkinForm.guestIdCard" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleWalkinCheckIn" :loading="submitting">办理入住</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { frontDeskAPI } from '@/api'
import { ElMessage } from 'element-plus'

const activeTab = ref('booking')
const todayArrivals = ref([])
const availableRooms = ref([])
const loadingArrivals = ref(false)
const submitting = ref(false)
const walkinFormRef = ref(null)

const walkinForm = reactive({ roomId: null, guestName: '', guestPhone: '', guestIdCard: '' })

const walkinRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  guestName: [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

async function loadData() {
  loadingArrivals.value = true
  try {
    const [arrivalsRes, roomsRes] = await Promise.all([frontDeskAPI.getTodayArrivals(), frontDeskAPI.getAllRooms({ page: 1, pageSize: 50 })])
    todayArrivals.value = arrivalsRes.data || []
    availableRooms.value = (roomsRes.data.records || []).filter(r => r.status === 'AVAILABLE')
  } finally { loadingArrivals.value = false }
}

function selectArrival(row) {}

async function quickCheckIn(row) {
  try {
    await frontDeskAPI.checkIn({ orderId: row.id })
    ElMessage.success('入住办理成功')
    loadData()
  } catch {}
}

async function handleWalkinCheckIn() {
  const valid = await walkinFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await frontDeskAPI.checkIn({
      roomId: walkinForm.roomId,
      guestName: walkinForm.guestName,
      guestPhone: walkinForm.guestPhone,
      guestIdCard: walkinForm.guestIdCard
    })
    ElMessage.success('散客入住办理成功')
    Object.assign(walkinForm, { roomId: null, guestName: '', guestPhone: '', guestIdCard: '' })
    loadData()
  } catch {} finally { submitting.value = false }
}

onMounted(loadData)
</script>
