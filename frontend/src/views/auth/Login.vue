<template>
  <div class="login-container">
    <div class="login-card">
      <h1>酒店预订管理系统</h1>
      <div class="login-tabs">
        <span class="tab" :class="{ active: loginType === 'guest' }" @click="loginType = 'guest'">🏨 住客</span>
        <span class="tab" :class="{ active: loginType === 'employee' }" @click="loginType = 'employee'">🛎️ 员工</span>
        <span class="tab" :class="{ active: loginType === 'admin' }" @click="loginType = 'admin'">⚙️ 管理员</span>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" style="margin-top:24px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :placeholder="usernamePlaceholder" @input="onUsernameChange" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" :disabled="locked" style="width:100%">
            {{ locked ? countdownText : '登 录' }}
          </el-button>
        </el-form-item>
        <el-alert v-if="locked" :title="lockMessage" type="error" show-icon :closable="false" style="margin-bottom:12px" />
        <el-alert v-if="!locked && remainingAttempts < 5 && remainingAttempts > 0" :title="'剩余尝试次数: ' + remainingAttempts + '/' + 5" type="warning" show-icon :closable="false" style="margin-bottom:12px" />
        <el-form-item v-if="loginType === 'guest'">
          <el-button link @click="$router.push('/register')">没有账号？去注册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '@/api'
import { useUserStore } from '@/store/user'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const loginType = ref('guest')
const locked = ref(false)
const lockMessage = ref('')
const countdownText = ref('')
const remainingAttempts = ref(5)
let countdownTimer = null

const usernamePlaceholder = computed(() => {
  if (loginType.value === 'admin') return '管理员账号'
  if (loginType.value === 'employee') return '员工工号'
  return '住客账号'
})

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onUsernameChange() {
  if (!form.username) return
  try {
    const res = await request.get('/auth/login-status', { params: { username: form.username } })
    const d = res.data
    locked.value = d.locked
    remainingAttempts.value = d.remainingAttempts
    if (d.locked && d.remainingSeconds > 0) {
      lockMessage.value = `登录过于频繁，请在 ${Math.floor(d.remainingSeconds / 60)}分${String(d.remainingSeconds % 60).padStart(2, '0')}秒 后重试`
      startCountdown(d.remainingSeconds)
    }
  } catch {}
}

function startCountdown(seconds) {
  clearInterval(countdownTimer)
  let remaining = seconds
  updateCountdownText(remaining)
  countdownTimer = setInterval(() => {
    remaining--
    if (remaining <= 0) {
      clearInterval(countdownTimer)
      locked.value = false
      lockMessage.value = ''
      countdownText.value = ''
      remainingAttempts.value = 5
      return
    }
    updateCountdownText(remaining)
  }, 1000)
}

function updateCountdownText(s) {
  countdownText.value = `${Math.floor(s / 60)}分${String(s % 60).padStart(2, '0')}秒`
}

onUnmounted(() => clearInterval(countdownTimer))

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const isEmployee = loginType.value === 'employee'
    const res = await authAPI.login({
      username: form.username,
      password: form.password,
      loginType: isEmployee ? 'EMPLOYEE' : 'GUEST'
    })
    const { token, user, employee } = res.data
    userStore.setAuth(token, user || employee, isEmployee)
    const role = user?.roleType || employee?.roleType
    if (role === 'GUEST') router.push('/guest/rooms')
    else if (role === 'FRONT_DESK') router.push('/front-desk/dashboard')
    else if (role === 'ADMIN') router.push('/admin/room-types')
  } catch {
    remainingAttempts.value = Math.max(0, remainingAttempts.value - 1)
    if (remainingAttempts.value <= 0) {
      locked.value = true
      lockMessage.value = '登录过于频繁，请在 15分钟 后重试'
      startCountdown(900)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: var(--radius-lg, 16px);
  box-shadow: var(--shadow-lg);
}
.login-card h1 {
  text-align: center;
  color: #303133;
  margin-bottom: 20px;
  font-size: 24px;
}
.login-tabs {
  display: flex;
  justify-content: center;
  gap: 0;
  background: #F5F6FA;
  border-radius: 8px;
  padding: 3px;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
  color: #909399;
  transition: all 0.2s;
  user-select: none;
}
.tab:hover {
  color: #606266;
}
.tab.active {
  background: #fff;
  color: var(--color-primary, #C8A25C);
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
</style>
