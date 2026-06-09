<template>
  <div>
    <div class="card-header">
      <h2>统计报表</h2>
      <el-radio-group v-model="trendDays" @change="loadCharts" size="small">
        <el-radio-button :value="7">近7天</el-radio-button>
        <el-radio-button :value="30">近30天</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card"><p class="stat-num">{{ stats.totalRooms || 0 }}</p><p>总房间数</p></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card"><p class="stat-num gc">{{ stats.occupiedRooms || 0 }}</p><p>已入住</p></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card"><p class="stat-num">{{ stats.availableRooms || 0 }}</p><p>可用房间</p></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card"><p class="stat-num yc">{{ stats.occupancyRate || 0 }}%</p><p>入住率</p></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="12">
        <el-card>
          <template #header>入住率趋势</template>
          <div ref="occupancyChart" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>每日营收趋势 (元)</template>
          <div ref="revenueChart" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header>各房型营收占比</template>
          <div ref="pieChart" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card"><p class="stat-num">{{ stats.totalOrders || 0 }}</p><p>总订单数</p></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card"><p class="stat-num rc">¥{{ formatAmount(stats.totalRevenue || 0) }}</p><p>总营业额</p></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { adminAPI } from '@/api'
import request from '@/api/request'

const stats = ref({})
const trendDays = ref(7)

const occupancyChart = ref(null)
const revenueChart = ref(null)
const pieChart = ref(null)
let oChart = null, rChart = null, pChart = null

function formatAmount(v) { return Number(v || 0).toFixed(2) }

async function loadStats() {
  try {
    const res = await adminAPI.getStatistics({ type: 'daily' })
    stats.value = res.data || {}
  } catch {}
}

async function loadCharts() {
  try {
    const res = await request.get('/admin/statistics/trend', { params: { days: trendDays.value } })
    const d = res.data
    await nextTick()
    drawOccupancyChart(d.occupancyTrend || [])
    drawRevenueChart(d.revenueTrend || [])
    drawPieChart(d.roomTypeRevenue || [])
  } catch {}
}

function drawOccupancyChart(data) {
  if (!occupancyChart.value) return
  if (!oChart) oChart = echarts.init(occupancyChart.value)
  oChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: data.map(d => d.date.slice(5)) },
    yAxis: { type: 'value', name: '%', min: 0, max: 100 },
    series: [{
      data: data.map(d => Number(d.rate)),
      type: 'line',
      smooth: true,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1,
        [{ offset: 0, color: 'rgba(39,174,96,0.25)' }, { offset: 1, color: 'rgba(39,174,96,0.03)' }]) },
      lineStyle: { color: '#27AE60', width: 3 },
      itemStyle: { color: '#27AE60' }
    }]
  })
}

function drawRevenueChart(data) {
  if (!revenueChart.value) return
  if (!rChart) rChart = echarts.init(revenueChart.value)
  rChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: data.map(d => d.date.slice(5)) },
    yAxis: { type: 'value', name: '￥' },
    series: [{
      data: data.map(d => Number(d.amount)),
      type: 'bar',
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1,
        [{ offset: 0, color: '#C8A25C' }, { offset: 1, color: '#E8D5A3' }]) },
      barMaxWidth: 30
    }]
  })
}

function drawPieChart(data) {
  if (!pieChart.value) return
  if (!pChart) pChart = echarts.init(pieChart.value)
  const total = data.reduce((s, d) => s + Number(d.revenue || 0), 0)
  pChart.setOption({
    tooltip: { trigger: 'item', formatter: p => `${p.name}: ¥${Number(p.value).toFixed(2)} (${p.percent}%)` },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      emphasis: { label: { fontSize: 16, fontWeight: 'bold' } },
      color: ['#C8A25C', '#27AE60', '#3498DB', '#E67E22', '#9B59B6'],
      data: data.map(d => ({ name: d.name, value: Number(d.revenue || 0) }))
        .filter(d => d.value > 0)
        .sort((a, b) => b.value - a.value)
    }],
    graphic: total > 0 ? [{
      type: 'text', left: 'center', top: '38%',
      style: { text: `¥${total.toFixed(0)}`, textAlign: 'center', fontSize: 14, fontWeight: 'bold' }
    }] : []
  })
}

function resizeCharts() {
  oChart?.resize(); rChart?.resize(); pChart?.resize()
}

onMounted(async () => {
  await loadStats()
  await loadCharts()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  oChart?.dispose(); rChart?.dispose(); pChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-num { font-size: 28px; font-weight: bold; }
.gc { color: #27AE60; } .yc { color: #E67E22; } .rc { color: #C8A25C; }
</style>
