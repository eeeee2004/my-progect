<template>
  <div>
    <div class="card-header">
      <h2>房态实时看板</h2>
      <el-button type="primary" @click="loadRooms" :loading="loading">
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
        v-for="room in rooms"
        :key="room.id"
        class="room-item"
        :class="'room-' + room.status.toLowerCase()"
        @click="showRoomActions(room)"
      >
        <p class="room-num">{{ room.roomNumber }}</p>
        <p class="room-floor">{{ room.floor }}楼</p>
        <p class="room-status">{{ statusMap[room.status] }}</p>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="'房间 ' + currentRoom?.roomNumber + ' 操作'" width="400px">
      <p>当前状态: <el-tag :type="statusTagType(currentRoom?.status)">{{ statusMap[currentRoom?.status] }}</el-tag></p>
      <p style="margin-top:16px">修改状态:</p>
      <el-select v-model="newStatus" style="width:100%;margin-top:8px">
        <el-option label="空闲" value="AVAILABLE" />
        <el-option label="已预订" value="BOOKED" />
        <el-option label="已入住" value="OCCUPIED" />
        <el-option label="待清洁" value="CLEANING" />
        <el-option label="维修中" value="MAINTENANCE" />
      </el-select>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusChange" :loading="updating">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { frontDeskAPI } from '@/api'
import { ElMessage } from 'element-plus'

const rooms = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentRoom = ref(null)
const newStatus = ref('')
const updating = ref(false)

const statusMap = { AVAILABLE: '空闲', BOOKED: '已预订', OCCUPIED: '已入住', CLEANING: '待清洁', MAINTENANCE: '维修中' }

function statusTagType(s) {
  return { AVAILABLE: 'success', BOOKED: 'warning', OCCUPIED: 'danger', CLEANING: 'info', MAINTENANCE: 'danger' }[s] || 'info'
}

async function loadRooms() {
  loading.value = true
  try {
    const res = await frontDeskAPI.getAllRooms({ page: 1, pageSize: 50 })
    rooms.value = res.data.records || []
  } finally { loading.value = false }
}

function showRoomActions(room) {
  currentRoom.value = room
  newStatus.value = room.status
  dialogVisible.value = true
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
    loadRooms()
  } catch {} finally { updating.value = false }
}

onMounted(loadRooms)
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
</style>
