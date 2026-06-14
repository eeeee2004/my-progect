<template>
  <el-container>
    <el-header class="hotel-header">
      <div class="header-left">
        <span class="header-brand">🏨 酒店预订</span>
        <el-menu mode="horizontal" :default-active="activeMenu" router background-color="transparent" text-color="rgba(255,255,255,0.75)" active-text-color="#C8A25C">
          <el-menu-item index="/guest/rooms">客房查询</el-menu-item>
          <el-menu-item index="/guest/my-orders">我的订单</el-menu-item>
          <el-menu-item index="/guest/profile">个人中心</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <span class="user-name">{{ userStore.userInfo?.realName }}</span>
        <el-button type="danger" size="small" plain @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <div class="page-container">
        <router-view :key="$route.fullPath" />
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.hotel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1A1A2E 0%, #2C3E50 100%);
  height: 60px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}
.header-left { display: flex; align-items: center; gap: 20px; }
.header-brand {
  color: var(--color-primary, #C8A25C);
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.5px;
}
.header-right { display: flex; align-items: center; gap: 12px; }
.user-name { color: rgba(255,255,255,0.85); }
</style>
