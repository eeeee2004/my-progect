<template>
  <div v-if="data">
    <el-button link @click="$router.back()" style="margin-bottom:16px">
      ← 返回房型列表
    </el-button>

    <el-row :gutter="24">
      <el-col :span="10">
        <el-image v-if="data.imageUrl" :src="data.imageUrl" style="width:100%;height:340px;border-radius:12px" fit="cover" :preview-src-list="[data.imageUrl]" />
        <div v-else style="width:100%;height:340px;border-radius:12px;background:linear-gradient(135deg,#E8ECF1,#F5F6FA);display:flex;align-items:center;justify-content:center">
          <span style="color:#AAB7C4;font-size:16px">暂无图片</span>
        </div>
      </el-col>

      <el-col :span="14">
        <h1 style="margin:0 0 12px 0;font-size:26px">{{ data.name }}</h1>
        <el-tag>{{ data.bedType }}</el-tag>
        <el-tag type="info" style="margin-left:8px">{{ data.area }}㎡</el-tag>
        <el-tag type="warning" style="margin-left:8px">{{ data.windowType || '有窗' }}</el-tag>
        <p style="margin-top:16px;font-size:32px;font-weight:700;color:var(--color-primary,#C8A25C)">
          ¥{{ data.basePrice }}<span style="font-size:14px;color:#AAB7C4;font-weight:400"> /晚起</span>
        </p>

        <el-descriptions :column="2" border style="margin-top:16px">
          <el-descriptions-item label="容纳人数">{{ data.capacity || 2 }}人</el-descriptions-item>
          <el-descriptions-item label="早餐">{{ data.breakfast || '不含早' }}</el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ data.checkInTime || '14:00' }}</el-descriptions-item>
          <el-descriptions-item label="退房时间">{{ data.checkOutTime || '12:00' }}</el-descriptions-item>
          <el-descriptions-item label="楼层分布">{{ data.floorInfo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="窗型">{{ data.windowType || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div style="display:flex;gap:12px;margin-top:20px">
          <el-button type="primary" size="large" @click="quickBook" :disabled="availableRooms.length === 0">
            {{ availableRooms.length > 0 ? '立即预订' : '暂无可用房间' }}
          </el-button>
        </div>
      </el-col>
    </el-row>

    <el-divider />

    <el-row :gutter="24">
      <el-col :span="14">
        <h3>房型介绍</h3>
        <p style="color:var(--color-text-secondary);line-height:1.8;font-size:15px">{{ data.description }}</p>

        <h3 style="margin-top:24px">配套设施</h3>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          <el-tag v-for="(f, i) in facilityList" :key="i" size="large" effect="plain">{{ f }}</el-tag>
        </div>

        <h3 style="margin-top:24px">取消政策</h3>
        <p style="color:var(--color-text-secondary);line-height:1.8;font-size:14px">{{ data.cancelPolicy || '暂未设置取消政策' }}</p>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header>
            <span>可选房间 ({{ availableRooms.length }}/{{ roomList.length }})</span>
          </template>
          <div v-if="roomList.length === 0" style="color:#AAB7C4;text-align:center;padding:20px">
            暂无该房型的房间
          </div>
          <div v-for="r in roomList" :key="r.id"
            style="display:flex;justify-content:space-between;align-items:center;padding:12px;margin-bottom:8px;border-radius:8px;border:1px solid #E8ECF1"
            :style="{ background: r.status === 'AVAILABLE' ? '#E8F8F0' : '#FEF5E7' }">
            <div>
              <strong>{{ r.roomNumber }}</strong>
              <span style="color:#AAB7C4;margin-left:8px;font-size:12px">{{ r.floor }}楼</span>
            </div>
            <div>
              <el-tag :type="r.status === 'AVAILABLE' ? 'success' : 'warning'" size="small">{{ statusMap[r.status] }}</el-tag>
              <el-button v-if="r.status === 'AVAILABLE'" type="primary" size="small" style="margin-left:8px" @click="openBooking(r)">预订</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="bookingDialog.visible" title="预订客房" width="550px">
      <el-form :model="bookingDialog.form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="房型">
          <el-input :model-value="data.name" disabled />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input :model-value="bookingDialog.room?.roomNumber" disabled />
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker v-model="bookingDialog.form.checkInDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="离店日期">
          <el-date-picker v-model="bookingDialog.form.checkOutDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
  <div v-else style="text-align:center;padding:60px">
    <el-empty description="房型不存在" />
    <el-button link @click="$router.push('/guest/rooms')">返回房型列表</el-button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { roomAPI, guestAPI } from '@/api'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const data = ref(null)
const roomList = ref([])
const formRef = ref(null)

const statusMap = { AVAILABLE: '空闲', BOOKED: '已预订', OCCUPIED: '已入住', CLEANING: '待清洁', MAINTENANCE: '维修中' }
const availableRooms = computed(() => roomList.value.filter(r => r.status === 'AVAILABLE'))
const facilityList = computed(() => data.value?.facilities ? data.value.facilities.split(',').map(f => f.trim()) : [])

const bookingDialog = reactive({
  visible: false, room: null, submitting: false, priceDetail: null,
  form: { checkInDate: '', checkOutDate: '', guestName: '', guestPhone: '', guestIdCard: '' }
})

const rules = {
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }],
  guestName: [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }],
  guestPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function loadData() {
  const id = route.params.id
  try {
    const typesRes = await roomAPI.getRoomTypes()
    data.value = (typesRes.data || []).find(t => t.id == id)
  } catch {}

  if (data.value) {
    try {
      const res = await roomAPI.queryRooms({ roomTypeId: id, page: 1, pageSize: 50 })
      roomList.value = res.data.records || []
    } catch {}
  }
}

function quickBook() {
  if (availableRooms.value.length > 0) {
    openBooking(availableRooms.value[0])
  }
}

function openBooking(room) {
  bookingDialog.room = room
  bookingDialog.priceDetail = null
  bookingDialog.form = { checkInDate: '', checkOutDate: '', guestName: '', guestPhone: '', guestIdCard: '' }
  bookingDialog.visible = true
}

async function confirmBooking() {
  const valid = await formRef.value.validate().catch(() => false)
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
    loadData()
  } catch {} finally { bookingDialog.submitting = false }
}

onMounted(loadData)
</script>
