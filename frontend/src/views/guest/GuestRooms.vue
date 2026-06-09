<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="入住日期">
          <el-date-picker v-model="queryForm.checkInDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="离店日期">
          <el-date-picker v-model="queryForm.checkOutDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="queryForm.roomTypeId" placeholder="全部房型" clearable>
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchRooms" :loading="loading">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="roomTypes.length > 0" class="room-types-section">
      <h3>房型介绍</h3>
      <el-row :gutter="20">
        <el-col :span="8" v-for="rt in roomTypes" :key="rt.id">
          <el-card class="room-type-card" shadow="hover" style="cursor:pointer" @click="$router.push(`/guest/room-type/${rt.id}`)">
            <template #header><strong>{{ rt.name }}</strong></template>
            <div style="margin-bottom:10px">
              <el-image v-if="rt.imageUrl" :src="rt.imageUrl" style="width:100%;height:160px;border-radius:8px" fit="cover" />
              <div v-else style="width:100%;height:160px;border-radius:8px;background:linear-gradient(135deg,#E8ECF1,#F5F6FA);display:flex;align-items:center;justify-content:center">
                <span style="color:#AAB7C4;font-size:14px">暂无图片</span>
              </div>
            </div>
            <p><el-tag>{{ rt.bedType }}</el-tag>  <el-tag type="info">{{ rt.area }}㎡</el-tag></p>
            <p class="price">¥{{ rt.basePrice }} <span class="unit">/晚</span></p>
            <p class="facilities">{{ rt.facilities }}</p>
            <p class="desc">{{ rt.description }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="rooms-section" v-if="rooms.length > 0">
      <h3>可订房间</h3>
      <el-row :gutter="16">
        <el-col :span="6" v-for="room in rooms" :key="room.id">
          <el-card class="room-card" shadow="hover">
            <p><strong>{{ room.roomNumber }}</strong> ({{ room.floor }}楼)</p>
            <p>房型: {{ room.roomTypeName }}</p>
            <p class="price">¥{{ room.basePrice }} /晚</p>
            <p>状态: <el-tag :type="room.status === 'AVAILABLE' ? 'success' : 'warning'">{{ statusMap[room.status] || room.status }}</el-tag></p>
            <el-button type="primary" size="small" @click="openBookingDialog(room)" :disabled="room.status !== 'AVAILABLE'">预订</el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-if="searched && rooms.length === 0" description="暂无符合条件的房间" />

    <el-dialog v-model="bookingDialog.visible" title="预订客房" width="550px">
      <el-form :model="bookingDialog.form" :rules="bookingRules" ref="bookingFormRef" label-width="100px">
        <el-form-item label="房间号">
          <el-input :model-value="bookingDialog.room?.roomNumber" disabled />
        </el-form-item>
        <el-form-item label="入住日期">
          <el-input :model-value="bookingDialog.form.checkInDate" disabled />
        </el-form-item>
        <el-form-item label="离店日期">
          <el-input :model-value="bookingDialog.form.checkOutDate" disabled />
        </el-form-item>
        <el-divider v-if="bookingDialog.priceDetail" />
        <div v-if="bookingDialog.priceDetail" style="margin-bottom:16px">
          <p v-for="dp in bookingDialog.priceDetail.dailyPrices" :key="dp.date" style="margin:4px 0;font-size:13px">
            <span>{{ dp.date }}</span>
            <span style="color:#909399;margin:0 8px">¥{{ dp.basePrice }} × {{ dp.factor }}</span>
            <span style="color:var(--color-primary,#C8A25C);font-weight:bold">¥{{ dp.price }}</span>
          </p>
          <el-divider />
          <p style="text-align:right;font-size:16px">
            共{{ bookingDialog.priceDetail.nights }}晚，
            总价: <span style="color:var(--color-primary,#C8A25C);font-size:22px;font-weight:bold">¥{{ bookingDialog.priceDetail.totalPrice }}</span>
          </p>
        </div>
        <el-form-item label="入住人" prop="guestName">
          <el-input v-model="bookingDialog.form.guestName" placeholder="请输入入住人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="guestPhone">
          <el-input v-model="bookingDialog.form.guestPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="bookingDialog.form.guestIdCard" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmBooking" :loading="bookingDialog.submitting">确认预订</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { roomAPI, guestAPI } from '@/api'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const queryForm = reactive({ checkInDate: '', checkOutDate: '', roomTypeId: null })
const roomTypes = ref([])
const rooms = ref([])
const loading = ref(false)
const searched = ref(false)
const bookingFormRef = ref(null)

const statusMap = { AVAILABLE: '空闲', BOOKED: '已预订', OCCUPIED: '已入住', CLEANING: '待清洁', MAINTENANCE: '维修中' }

const bookingDialog = reactive({
  visible: false,
  room: null,
  submitting: false,
  priceDetail: null,
  form: {
    checkInDate: '', checkOutDate: '', guestName: '', guestPhone: '', guestIdCard: ''
  }
})

const bookingRules = {
  guestName: [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }],
  guestPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function loadRoomTypes() {
  const res = await roomAPI.getRoomTypes()
  roomTypes.value = res.data || []
}

async function searchRooms() {
  loading.value = true
  searched.value = true
  try {
    const params = {}
    if (queryForm.checkInDate) params.checkInDate = queryForm.checkInDate
    if (queryForm.checkOutDate) params.checkOutDate = queryForm.checkOutDate
    if (queryForm.roomTypeId) params.roomTypeId = queryForm.roomTypeId
    params.status = 'AVAILABLE'
    params.page = 1
    params.pageSize = 50
    const res = await roomAPI.queryRooms(params)
    rooms.value = res.data.records || []
  } finally {
    loading.value = false
  }
}

async function openBookingDialog(room) {
  bookingDialog.room = room
  bookingDialog.priceDetail = null
  bookingDialog.form = {
    checkInDate: queryForm.checkInDate || '',
    checkOutDate: queryForm.checkOutDate || '',
    guestName: '', guestPhone: '', guestIdCard: ''
  }
  bookingDialog.visible = true
  if (queryForm.checkInDate && queryForm.checkOutDate && room.roomTypeId) {
    try {
      const res = await request.get(`/room-types/${room.roomTypeId}/price`, {
        params: { checkInDate: queryForm.checkInDate, checkOutDate: queryForm.checkOutDate }
      })
      bookingDialog.priceDetail = res.data
    } catch {}
  }
}

async function confirmBooking() {
  const valid = await bookingFormRef.value.validate().catch(() => false)
  if (!valid) return
  bookingDialog.submitting = true
  try {
    await guestAPI.createBooking({
      roomId: bookingDialog.room.id,
      checkInDate: bookingDialog.form.checkInDate,
      checkOutDate: bookingDialog.form.checkOutDate,
      guestName: bookingDialog.form.guestName,
      guestPhone: bookingDialog.form.guestPhone,
      guestIdCard: bookingDialog.form.guestIdCard
    })
    ElMessage.success('预订成功')
    bookingDialog.visible = false
    searchRooms()
  } catch {
  } finally {
    bookingDialog.submitting = false
  }
}

onMounted(() => {
  loadRoomTypes()
  searchRooms()
})
</script>

<style scoped>
.room-types-section { margin-bottom: 30px; }
.room-types-section h3, .rooms-section h3 { margin-bottom: 16px; color: #303133; }
.room-type-card p, .room-card p { margin-bottom: 8px; }
.price { color: var(--color-primary, #C8A25C); font-size: 18px; font-weight: bold; }
.unit { font-size: 12px; color: var(--color-text-secondary, #5D6D7E); }
.facilities { color: var(--color-text-secondary, #5D6D7E); font-size: 13px; }
.desc { color: #606266; font-size: 13px; }
.room-card { margin-bottom: 16px; border-radius: var(--radius-md, 12px); }
</style>
