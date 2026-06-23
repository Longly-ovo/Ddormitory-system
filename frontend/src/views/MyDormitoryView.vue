<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { get } from '../api'
import type { RoomMapItem, StudentDormitory } from '../types'

const dormitory = ref<StudentDormitory>()
const rooms = ref<RoomMapItem[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    [dormitory.value, rooms.value] = await Promise.all([
      get<StudentDormitory>('/student/me/dormitory'),
      get<RoomMapItem[]>('/student/me/floor-map')
    ])
  } finally { loading.value = false }
})
</script>

<template>
  <div v-loading="loading">
    <div class="page-head"><div><h1>我的宿舍</h1><p>查看本人住宿和所在楼层信息</p></div></div>
    <div class="table-panel" v-if="dormitory">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ dormitory.student.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ dormitory.student.name }}</el-descriptions-item>
        <el-descriptions-item label="楼栋">{{ dormitory.building?.name || '暂未分配宿舍' }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ dormitory.floor?.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间">{{ dormitory.room?.roomNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="床位">{{ dormitory.bed?.bedNo || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="page-head floor-head" v-if="rooms.length"><div><h1>所在楼层</h1><p>仅展示房间入住状态，不展示其他学生信息</p></div></div>
    <div class="room-grid readonly-map" v-if="rooms.length">
      <div v-for="room in rooms" :key="room.id" class="room-card" :class="room.status.toLowerCase()">
        <div class="room-top"><strong>{{ room.roomNo }}</strong><span>{{ room.status === 'FULL' ? '已满' : '有空床' }}</span></div>
        <div class="occupancy"><b>{{ room.occupiedBeds }}</b><span>/ {{ room.totalBeds }} 人</span></div>
        <div class="progress"><i :style="{ width: `${room.occupiedBeds / room.totalBeds * 100}%` }"></i></div>
        <small>只读信息</small>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="暂未分配宿舍" />
  </div>
</template>

<style scoped>
.floor-head { margin-top: 30px; }
.readonly-map .room-card { cursor: default; }
.readonly-map .room-card:hover { transform: none; }
</style>
