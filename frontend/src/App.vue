<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: 'Microsoft YaHei', 'PingFang SC', Arial, sans-serif;
  background-color: #f5f7fa;
}
#app {
  min-height: 100vh;
}
.page-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 140px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.card-header h2 {
  font-size: 20px;
  color: #303133;
}
.status-tag-pending { color: #e6a23c; }
.status-tag-checked_in { color: #409eff; }
.status-tag-completed { color: #67c23a; }
.status-tag-cancelled { color: #f56c6c; }
.room-available { color: #67c23a; }
.room-booked { color: #e6a23c; }
.room-occupied { color: #f56c6c; }
.room-cleaning { color: #909399; }
.room-maintenance { color: #f56c6c; }
</style>
