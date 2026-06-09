<template>
  <div>
    <div class="card-header"><h2>操作审计日志</h2></div>
    <el-table :data="logs" v-loading="loading" stripe size="small">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="createTime" label="操作时间" width="175" />
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="action" label="操作" width="100" />
      <el-table-column prop="description" label="描述" min-width="150" />
      <el-table-column label="结果" width="80">
        <template #default="{ row }">
          <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.result === 'SUCCESS' ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operatorType" label="操作角色" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ roleMap[row.operatorType] || row.operatorType || '未登录' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" width="130" />
    </el-table>
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadLogs"
      @current-change="loadLogs"
      style="margin-top:16px;justify-content:flex-end"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const logs = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const roleMap = { GUEST: '住客', FRONT_DESK: '前台', ADMIN: '管理员' }

async function loadLogs() {
  loading.value = true
  try {
    const res = await request.get('/admin/logs', { params: { page: page.value, pageSize: pageSize.value } })
    const d = res.data
    logs.value = d.records || []
    total.value = d.total || 0
  } catch {} finally { loading.value = false }
}

onMounted(loadLogs)
</script>
