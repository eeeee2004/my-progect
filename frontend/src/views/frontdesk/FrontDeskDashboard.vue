<template>
  <div>
    <div class="card-header">
      <h2>房态实时看板</h2>
      <el-button type="primary" @click="loadAllData" :loading="loading">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-row :gutter="12">
      <el-col :span="4">
        <el-card class="stat-card">
          <p class="stat-num gc">{{ rooms.filter(r => r.status === 'AVAILABLE').length }}</p>
          <p>空闲</p>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <p class="stat-num yc">{{ rooms.filter(r => r.status === 'BOOKED').length }}</p>
          <p>已预订</p>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <p class="stat-num rc">{{ rooms.filter(r => r.status === 'OCCUPIED').length }}</p>
          <p>已入住</p>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <p class="stat-num gc2">{{ rooms.filter(r => r.status === 'CLEANING').length }}</p>
          <p>待清洁</p>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <p class="stat-num rc">{{ rooms.filter(r => r.status === 'MAINTENANCE').length }}</p>
          <p>维修中</p>
        </el-card>
      </el-col>
    </el-row>

    <div class="room-grid">
      <div
        v-for="room in roomsWithGuests"
        :key="room.id"
        class="room-item"
        :class="'room-' + room.status.toLowerCase()"
        @click="showRoomActions(room)"
      >
        <p class="room-num">{{ room.roomNumber }}</p>
        <p class="room-floor">{{ room.floor }}楼</p>
        <p class="room-status">{{ statusMap[room.status] }}</p>
        <p class="room-guest" v-if="room.guestName">{{ room.guestName }}</p>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="'房间 ' + currentRoom?.roomNumber + ' 操作'" width="450px">
      <div v-if="currentRoom">
        <p>当前状态: <el-tag :type="statusTagType(currentRoom.status)">{{ statusMap[currentRoom.status] }}</el-tag></p>
        <p v-if="currentRoom.guestName" style="margin-top:8px">
          住客: <strong>{{ currentRoom.guestName }}</strong>  {{ currentRoom.guestPhone }}
        </p>
        <el-divider v-if="currentRoom.status === 'BOOKED' || currentRoom.status === 'OCCUPIED'" />
        <div v-if="currentRoom.status === 'BOOKED' && currentRoom.orderId" style="margin-bottom:12px">
          <el-button type="primary" size="small" @click="quickCheckIn">一键入住</el-button>
        </div>
        <div v-if="currentRoom.status === 'OCCUPIED' && currentRoom.orderId" style="margin-bottom:12px">
          <el-button type="warning" size="small" @click="quickCheckOut">退房结算</el-button>
        </div>
        <p style="margin-top:16px">修改状态:</p>
        <el-select v-model="newStatus" style="width:100%;margin-top:8px">
          <el-option label="空闲" value="AVAILABLE" />
          <el-option label="已预订" value="BOOKED" />
          <el-option label="已入住" value="OCCUPIED" />
          <el-option label="待清洁" value="CLEANING" />
          <el-option label="维修中" value="MAINTENANCE" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusChange" :loading="updating">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { frontDeskAPI } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const rooms = ref([])
const occupiedData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentRoom = ref(null)
const newStatus = ref('')
const updating = ref(false)

const statusMap = { AVAILABLE: '空闲', BOOKED: '已预订', OCCUPIED: '已入住', CLEANING: '待清洁', MAINTENANCE: '维修中' }

function statusTagType(s) {
  return { AVAILABLE: 'success', BOOKED: 'warning', OCCUPIED: 'danger', CLEANING: 'info', MAINTENANCE: 'danger' }[s] || 'info'
}

const roomsWithGuests = computed(() => {
  return rooms.value.map(r => {
    const od = occupiedData.value.find(o => o.roomId === r.id)
    return od ? { ...r, guestName: od.guestName, guestPhone: od.guestPhone, orderId: od.orderId } : r
  })
})

async function loadAllData() {
  loading.value = true
  try {
    const [roomsRes, occupiedRes] = await Promise.all([
      frontDeskAPI.getAllRooms({ page: 1, pageSize: 50 }),
      frontDeskAPI.getOccupiedRooms()
    ])
    rooms.value = roomsRes.data.records || []
    occupiedData.value = occupiedRes.data || []
  } finally { loading.value = false }
}

function showRoomActions(room) {
  currentRoom.value = room
  newStatus.value = room.status
  dialogVisible.value = true
}

async function quickCheckIn() {
  try {
    await frontDeskAPI.checkIn({ orderId: currentRoom.value.orderId })
    ElMessage.success('入住办理成功')
    dialogVisible.value = false
    loadAllData()
  } catch {}
}

async function quickCheckOut() {
  try {
    const res = await frontDeskAPI.checkOut(currentRoom.value.orderId, { extraCharge: 0 })
    ElMessage.success('退房完成，费用 ¥' + (res.data?.totalAmount || 0))
    dialogVisible.value = false
    loadAllData()
  } catch {}
}

async function handleStatusChange() {
  if (newStatus.value === currentRoom.value.status) {
    dialogVisible.value = false
    return
  }
  updating.value = true
  try {
    await frontDeskAPI.updateRoomStatus(currentRoom.value.id, newStatus.value)
    ElMessage.success('状态更新成功')
    dialogVisible.value = false
    loadAllData()
  } catch {} finally { updating.value = false }
}

onMounted(loadAllData)
</script>

<style scoped>
.stat-card { text-align: center; cursor: default; background: var(--color-bg-card, #fff); border-radius: var(--radius-md, 12px); box-shadow: var(--shadow-sm); }
.stat-num { font-size: 32px; font-weight: bold; }
.gc { color: #27AE60; } .yc { color: #E67E22; } .rc { color: #C0392B; } .gc2 { color: #7F8C8D; }
.room-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 12px; margin-top: 20px; }
.room-item { padding: 14px; border-radius: 8px; text-align: center; cursor: pointer; transition: all 0.3s; font-weight: 500; }
.room-item:hover { transform: scale(1.03); box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
.room-available { background: #E8F8F0; color: #27AE60; border-left: 3px solid #27AE60; }
.room-booked { background: #FEF5E7; color: #E67E22; border-left: 3px solid #E67E22; }
.room-occupied { background: #FDEDEC; color: #C0392B; border-left: 3px solid #C0392B; }
.room-cleaning { background: #F2F3F4; color: #5D6D7E; border-left: 3px solid #7F8C8D; }
.room-maintenance { background: #1A1A2E; color: #fff; border-left: 3px solid #C0392B; }
.room-num { font-size: 20px; font-weight: bold; }
.room-floor { font-size: 12px; opacity: 0.7; }
.room-status { font-size: 13px; margin-top: 4px; }
.room-guest { font-size: 12px; margin-top: 2px; opacity: 0.8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
