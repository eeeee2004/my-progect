<template>
  <div>
    <div class="card-header">
      <h2>订单管理</h2>
      <div style="display:flex;gap:12px;align-items:center">
        <el-input v-model="orderSearch" placeholder="姓名/手机号/订单号" clearable style="width:220px" size="small" />
        <el-radio-group v-model="statusFilter" @change="loadOrders" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="PENDING">待入住</el-radio-button>
          <el-radio-button value="CHECKED_IN">已入住</el-radio-button>
          <el-radio-button value="COMPLETED">已完成</el-radio-button>
          <el-radio-button value="CANCELLED">已取消</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    <el-table :data="filteredOrders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单编号" width="160" sortable />
      <el-table-column prop="guestName" label="入住人" width="100" />
      <el-table-column prop="guestPhone" label="手机号" width="130" />
      <el-table-column prop="guestIdCard" label="身份证号" width="180" />
      <el-table-column prop="checkInDate" label="入住日期" width="120" />
      <el-table-column prop="checkOutDate" label="离店日期" width="120" />
      <el-table-column prop="nights" label="天数" width="60" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadOrders"
      @current-change="loadOrders"
      style="margin-top:16px;justify-content:flex-end"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { frontDeskAPI } from '@/api'

const orders = ref([])
const loading = ref(false)
const statusFilter = ref('')
const orderSearch = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap = { PENDING: '待入住', CHECKED_IN: '已入住', COMPLETED: '已完成', CANCELLED: '已取消' }

function statusType(s) {
  return { PENDING: 'warning', CHECKED_IN: 'primary', COMPLETED: 'success', CANCELLED: 'danger' }[s] || 'info'
}

const filteredOrders = computed(() => {
  if (!orderSearch.value) return orders.value
  const kw = orderSearch.value.toLowerCase()
  return orders.value.filter(o =>
    o.guestName?.toLowerCase().includes(kw) ||
    o.guestPhone?.includes(kw) ||
    o.orderNo?.toLowerCase().includes(kw)
  )
})

async function loadOrders() {
  loading.value = true
  try {
    const res = await frontDeskAPI.getOrders({
      status: statusFilter.value || undefined,
      page: page.value,
      pageSize: pageSize.value
    })
    const d = res.data
    orders.value = d.records || []
    total.value = d.total || 0
  } finally { loading.value = false }
}

onMounted(loadOrders)
</script>
