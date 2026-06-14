<template>
  <div>
    <div class="card-header"><h2>入住办理</h2></div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="有预订入住" name="booking">
        <el-form :inline="true" style="margin-bottom:12px">
          <el-form-item label="搜索">
            <el-input v-model="bookingSearch" placeholder="姓名/手机号/订单号" clearable style="width:240px" @input="filterBookings" />
          </el-form-item>
          <el-form-item>
            <el-button @click="loadData" :loading="loadingArrivals">刷新</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="filteredPendingOrders" v-loading="loadingArrivals" row-key="id">
          <el-table-column prop="orderNo" label="订单编号" width="160" sortable />
          <el-table-column prop="guestName" label="入住人" width="100" />
          <el-table-column prop="guestPhone" label="手机号" width="130" />
          <el-table-column prop="guestIdCard" label="身份证号" width="180" />
          <el-table-column prop="checkInDate" label="入住日期" width="110" />
          <el-table-column prop="checkOutDate" label="离店日期" width="110" />
          <el-table-column prop="totalAmount" label="预付金额" width="100">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="showCheckInDialog(row)">办理入住</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingArrivals && filteredPendingOrders.length === 0" description="暂无待入住订单" />
      </el-tab-pane>

      <el-tab-pane label="散客登记" name="walkin">
        <el-form :model="walkinForm" :rules="walkinRules" ref="walkinFormRef" label-width="100px" style="max-width:500px">
          <el-form-item label="房间号" prop="roomId">
            <el-select v-model="walkinForm.roomId" placeholder="选择空闲房间" filterable style="width:100%">
              <el-option v-for="r in availableRooms" :key="r.id" :label="`${r.roomNumber} (${r.roomTypeName}, ${r.floor}楼)`" :value="r.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="入住人姓名" prop="guestName">
            <el-input v-model="walkinForm.guestName" placeholder="身份证姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="guestPhone">
            <el-input v-model="walkinForm.guestPhone" placeholder="11位手机号" />
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="walkinForm.guestIdCard" placeholder="选填" />
          </el-form-item>
          <el-form-item label="预计天数" prop="nights">
            <el-input-number v-model="walkinForm.nights" :min="1" :max="30" style="width:200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleWalkinCheckIn" :loading="submitting">办理入住</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="checkInDialogVisible" title="办理入住确认" width="500px">
      <el-descriptions :column="2" border v-if="checkInOrder">
        <el-descriptions-item label="订单编号">{{ checkInOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="入住人">{{ checkInOrder.guestName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ checkInOrder.guestPhone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ checkInOrder.guestIdCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ checkInOrder.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="离店日期">{{ checkInOrder.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="预付金额">¥{{ checkInOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="当前时间">{{ nowText }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="checkInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckIn" :loading="submitting">确认入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { frontDeskAPI } from '@/api'
import { ElMessage } from 'element-plus'

const activeTab = ref('booking')
const pendingOrders = ref([])
const availableRooms = ref([])
const loadingArrivals = ref(false)
const submitting = ref(false)
const walkinFormRef = ref(null)
const bookingSearch = ref('')
const checkInDialogVisible = ref(false)
const checkInOrder = ref(null)

const walkinForm = reactive({ roomId: null, guestName: '', guestPhone: '', guestIdCard: '', nights: 1 })

const walkinRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  guestName: [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }],
  guestPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const nowText = computed(() => new Date().toLocaleString('zh-CN'))

const filteredPendingOrders = computed(() => {
  if (!bookingSearch.value) return pendingOrders.value
  const kw = bookingSearch.value.toLowerCase()
  return pendingOrders.value.filter(o =>
    o.guestName?.toLowerCase().includes(kw) ||
    o.guestPhone?.includes(kw) ||
    o.orderNo?.toLowerCase().includes(kw)
  )
})

async function loadData() {
  loadingArrivals.value = true
  try {
    const [ordersRes, roomsRes] = await Promise.all([
      frontDeskAPI.getOrders({ status: 'PENDING', page: 1, pageSize: 100 }),
      frontDeskAPI.getAllRooms({ page: 1, pageSize: 50 })
    ])
    pendingOrders.value = ordersRes.data.records || []
    availableRooms.value = (roomsRes.data.records || [])
      .filter(r => r.status === 'AVAILABLE')
      .map(r => ({ ...r, roomTypeName: r.roomTypeName || '' }))
  } finally { loadingArrivals.value = false }
}

function filterBookings() {}

function showCheckInDialog(row) {
  checkInOrder.value = row
  checkInDialogVisible.value = true
}

async function confirmCheckIn() {
  submitting.value = true
  try {
    await frontDeskAPI.checkIn({ orderId: checkInOrder.value.id })
    ElMessage.success('入住办理成功')
    checkInDialogVisible.value = false
    checkInOrder.value = null
    loadData()
  } catch {} finally { submitting.value = false }
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
    Object.assign(walkinForm, { roomId: null, guestName: '', guestPhone: '', guestIdCard: '', nights: 1 })
    loadData()
  } catch {} finally { submitting.value = false }
}

onMounted(loadData)
</script>
