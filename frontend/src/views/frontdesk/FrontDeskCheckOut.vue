<template>
  <div>
    <div class="card-header"><h2>退房结算</h2></div>

    <h4 style="margin-bottom:12px">今日预离客人</h4>
    <el-table :data="todayDepartures" @row-click="selectDeparture" highlight-current-row v-loading="loading">
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="guestName" label="入住人" width="100" />
      <el-table-column prop="guestPhone" label="手机号" width="130" />
      <el-table-column prop="checkInDate" label="入住日期" width="120" />
      <el-table-column prop="nights" label="已住天数" width="80" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="warning" size="small" @click.stop="openCheckOut(row)">退房结算</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="checkOutDialog.visible" title="退房结算" width="500px" @close="resetDialog">
      <el-descriptions :column="2" border v-if="checkOutDialog.bill">
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
      <el-form v-if="!checkOutDialog.bill" :model="checkOutDialog.form" label-width="100px">
        <el-form-item label="额外费用">
          <el-input-number v-model="checkOutDialog.form.extraCharge" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer v-if="checkOutDialog.bill">
        <el-button @click="checkOutDialog.visible = false">关闭</el-button>
      </template>
      <template #footer v-else>
        <el-button @click="checkOutDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="handleCheckOut" :loading="checkOutDialog.submitting">确认退房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { frontDeskAPI } from '@/api'
import { ElMessage } from 'element-plus'

const todayDepartures = ref([])
const loading = ref(false)

const checkOutDialog = reactive({
  visible: false,
  order: null,
  bill: null,
  submitting: false,
  form: { extraCharge: 0 }
})

async function loadData() {
  loading.value = true
  try {
    const res = await frontDeskAPI.getTodayDepartures()
    todayDepartures.value = res.data || []
  } finally { loading.value = false }
}

function selectDeparture(row) {}

function openCheckOut(row) {
  checkOutDialog.order = row
  checkOutDialog.bill = null
  checkOutDialog.form = { extraCharge: 0 }
  checkOutDialog.visible = true
}

async function handleCheckOut() {
  checkOutDialog.submitting = true
  try {
    const res = await frontDeskAPI.checkOut(checkOutDialog.order.id, checkOutDialog.form)
    checkOutDialog.bill = res.data
    ElMessage.success('退房结算完成')
    loadData()
  } catch {} finally {
    checkOutDialog.submitting = false
  }
}

function resetDialog() {
  checkOutDialog.order = null
  checkOutDialog.bill = null
}

onMounted(loadData)
</script>
