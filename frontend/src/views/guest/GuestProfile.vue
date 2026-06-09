<template>
  <div>
    <div class="card-header"><h2>个人中心</h2></div>
    <el-card class="profile-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width:500px">
        <el-form-item label="用户名">
          <el-input :model-value="userStore.userInfo?.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { guestAPI } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref(null)
const saving = ref(false)

const form = reactive({ realName: '', phone: '', email: '', idCard: '' })

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const res = await guestAPI.getProfile()
    if (res.data) {
      form.realName = res.data.realName || ''
      form.phone = res.data.phone || ''
      form.email = res.data.email || ''
      form.idCard = res.data.idCard || ''
    }
  } catch {}
})

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await guestAPI.updateProfile(form)
    ElMessage.success('修改成功')
  } catch {} finally {
    saving.value = false
  }
}
</script>
