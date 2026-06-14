<template>
  <div>
    <div class="card-header"><h2>退房结算</h2></div>

    <el-form :inline="true" style="margin-bottom:12px">
      <el-form-item label="搜索">
        <el-input v-model="departureSearch" placeholder="姓名/手机号/房间号" clearable style="width:240px" @input="filterDepartures" />
      </el-form-item>
      <el-form-item>
        <el-button @click="loadData" :loading="loading">刷新</el-button>
      </el-form-item>
    </el-form>

    <h4>已入住房客 ({{ filteredOccupiedRooms.length }})</h4>
    <el-table :data="filteredOccupiedRooms" v-loading="loading">
      <el-table-column label="房间号" width="90" prop="roomNumber" sortable />
      <el-table-column label="楼层" width="60" prop="floor" />
      <el-table-column label="房型" width="100" prop="roomTypeName" />
      <el-table-column label="住客姓名" width="100" prop="guestName" />
      <el-table-column label="手机号" width="130" prop="guestPhone" />
      <el-table-column label="入住日期" width="110" prop="checkInDate" />
      <el-table-column label="基础房价" width="90">
        <template #default="{ row }">¥{{ row.basePrice }}/晚</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" size="small" @click="openCheckOut(row)" :disabled="row.status !== 'OCCUPIED'">退房结算</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && filteredOccupiedRooms.length === 0" description="暂无在住客人" />

    <el-dialog v-model="checkOutDialog.visible" title="退房结算" width="520px" @close="resetDialog">
      <div v-if="checkOutDialog.bill">
        <el-alert title="退房成功" type="success" :closable="false" show-icon style="margin-bottom:16px">
          已办理退房，房间变更为待清洁状态。
        </el-alert>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ checkOutDialog.bill.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="入住人">{{ checkOutDialog.bill.guestName }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ checkOutDialog.bill.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ checkOutDialog.bill.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="退房日期">{{ checkOutDialog.bill.checkOutDate }}</el-descriptions-item>
          <el-descriptions-item label="住宿天数">{{ checkOutDialog.bill.nights }}天</el-descriptions-item>
          <el-descriptions-item label="房费">¥{{ checkOutDialog.bill.roomCharge }}</el-descriptions-item>
          <el-descriptions-item label="额外费用">¥{{ checkOutDialog.bill.extraCharge }}</el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span style="color:var(--color-primary,#C8A25C);font-size:18px;font-weight:bold">¥{{ checkOutDialog.bill.totalAmount }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else>
        <el-alert v-if="checkOutDialog.occupiedInfo" title="确认退房信息" type="warning" :closable="false" show-icon style="margin-bottom:16px">
          请确认住客信息无误后点击确认退房。
        </el-alert>
        <el-descriptions :column="2" border v-if="checkOutDialog.occupiedInfo">
          <el-descriptions-item label="房间号">{{ checkOutDialog.occupiedInfo.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ checkOutDialog.occupiedInfo.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="住客姓名">{{ checkOutDialog.occupiedInfo.guestName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ checkOutDialog.occupiedInfo.guestPhone }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ checkOutDialog.occupiedInfo.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="基础房价">¥{{ checkOutDialog.occupiedInfo.basePrice }}/晚</el-descriptions-item>
        </el-descriptions>
        <el-form :model="checkOutDialog.form" label-width="100px" style="margin-top:16px">
          <el-form-item label="额外费用">
            <el-input-number v-model="checkOutDialog.form.extraCharge" :min="0" :precision="2" style="width:200px" />
            <span style="margin-left:8px;color:#AAB7C4;font-size:12px">如损坏赔偿、迷你吧消费等</span>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="checkOutDialog.visible = false">关闭</el-button>
        <el-button v-if="!checkOutDialog.bill" type="warning" @click="handleCheckOut" :loading="checkOutDialog.submitting">确认退房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { frontDeskAPI } from '@/api'
import { ElMessage } from 'element-plus'

const occupiedRooms = ref([])
const loading = ref(false)
const departureSearch = ref('')

const checkOutDialog = reactive({
  visible: false,
  occupiedInfo: null,
  bill: null,
  submitting: false,
  form: { extraCharge: 0 }
})

const filteredOccupiedRooms = computed(() => {
  if (!departureSearch.value) return occupiedRooms.value
  const kw = departureSearch.value.toLowerCase()
  return occupiedRooms.value.filter(r =>
    r.guestName?.toLowerCase().includes(kw) ||
    r.guestPhone?.includes(kw) ||
    r.roomNumber?.toLowerCase().includes(kw)
  )
})

async function loadData() {
  loading.value = true
  try {
    const res = await frontDeskAPI.getOccupiedRooms()
    occupiedRooms.value = res.data || []
  } finally { loading.value = false }
}

function filterDepartures() {}

function openCheckOut(row) {
  checkOutDialog.occupiedInfo = row
  checkOutDialog.bill = null
  checkOutDialog.form = { extraCharge: 0 }
  checkOutDialog.visible = true
}

async function handleCheckOut() {
  if (!checkOutDialog.occupiedInfo.orderId) {
    ElMessage.warning('未找到关联订单')
    return
  }
  checkOutDialog.submitting = true
  try {
    const res = await frontDeskAPI.checkOut(checkOutDialog.occupiedInfo.orderId, checkOutDialog.form)
    checkOutDialog.bill = res.data
    ElMessage.success('退房结算完成')
    loadData()
  } catch {} finally { checkOutDialog.submitting = false }
}

function resetDialog() {
  checkOutDialog.occupiedInfo = null
  checkOutDialog.bill = null
}

onMounted(loadData)
</script>
