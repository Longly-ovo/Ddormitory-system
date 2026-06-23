<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { get, put, remove } from '../api'
import type { Bed, Building, Floor, RoomMapItem, Student } from '../types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const buildings = ref<Building[]>([]); const floors = ref<Floor[]>([]); const rooms = ref<RoomMapItem[]>([])
const buildingId = ref<number>(); const floorId = ref<number>(); const loading = ref(false)
const bedDialog = ref(false); const studentDialog = ref(false); const assignDialog = ref(false)
const currentRoom = ref<RoomMapItem>(); const beds = ref<Bed[]>([]); const currentStudent = ref<Student>()
const currentBed = ref<Bed>(); const allStudents = ref<Student[]>([]); const assignStudentId = ref<number>()
const auth = useAuthStore(); const readonly = auth.role === 'COUNSELOR'

onMounted(async () => {
  buildings.value = await get<Building[]>('/buildings')
  if (buildings.value.length) buildingId.value = buildings.value[0].id
})
watch(buildingId, async id => {
  floors.value = id ? await get<Floor[]>(`/buildings/${id}/floors`) : []
  floorId.value = floors.value[0]?.id
})
watch(floorId, loadMap)
async function loadMap() {
  if (!floorId.value) { rooms.value = []; return }
  loading.value = true
  try { rooms.value = await get<RoomMapItem[]>(`/floors/${floorId.value}/map`) }
  finally { loading.value = false }
}
async function openRoom(room: RoomMapItem) {
  currentRoom.value = room; beds.value = await get<Bed[]>(`/rooms/${room.id}/beds`); bedDialog.value = true
}
async function clickBed(bed: Bed) {
  if (!bed.studentId) { ElMessage.info('该床位当前空闲'); return }
  currentStudent.value = await get<Student>(`/students/${bed.studentId}`); studentDialog.value = true
}
async function startAssign(bed: Bed) {
  currentBed.value = bed; assignStudentId.value = undefined
  allStudents.value = await get<Student[]>('/students'); assignDialog.value = true
}
async function assign() {
  if (!assignStudentId.value || !currentBed.value?.id) return
  await put(`/beds/${currentBed.value.id}/student`, { studentId: assignStudentId.value })
  ElMessage.success('床位分配成功'); assignDialog.value = false; await refreshRoom()
}
async function checkout(bed: Bed) {
  await ElMessageBox.confirm(`确认将 ${bed.studentName} 从 ${bed.bedNo} 退宿？`, '退宿确认', { type: 'warning' })
  await remove(`/beds/${bed.id}/student`); ElMessage.success('退宿成功'); await refreshRoom()
}
async function refreshRoom() { if (currentRoom.value) await openRoom(currentRoom.value); await loadMap() }
</script>

<template>
  <div class="page-head"><div><h1>楼层地图</h1><p>查看寝室床位与学生入住情况</p></div>
    <div class="legend"><span><i class="dot blue"></i>有空床</span><span><i class="dot green"></i>已满</span></div>
  </div>
  <div class="filter-bar">
    <label>楼栋</label><el-select v-model="buildingId" placeholder="选择楼栋"><el-option v-for="item in buildings" :key="item.id" :label="item.name" :value="item.id" /></el-select>
    <label>楼层</label><el-select v-model="floorId" placeholder="选择楼层"><el-option v-for="item in floors" :key="item.id" :label="item.name" :value="item.id" /></el-select>
    <el-button @click="loadMap">刷新</el-button>
  </div>
  <div v-loading="loading" class="room-grid" v-if="rooms.length">
    <button v-for="room in rooms" :key="room.id" class="room-card" :class="room.status.toLowerCase()" @click="openRoom(room)">
      <div class="room-top"><strong>{{ room.roomNo }}</strong><span>{{ room.status === 'FULL' ? '已满' : '有空床' }}</span></div>
      <div class="occupancy"><b>{{ room.occupiedBeds }}</b><span>/ {{ room.totalBeds }} 人</span></div>
      <div class="progress"><i :style="{ width: `${room.occupiedBeds / room.totalBeds * 100}%` }"></i></div>
      <small>点击查看床位</small>
    </button>
  </div>
  <el-empty v-else description="当前楼层暂无已配置床位的寝室" />

  <el-dialog v-model="bedDialog" :title="`${currentRoom?.roomNo || ''} 寝室床位`" width="680px">
    <div class="bed-grid">
      <div v-for="bed in beds" :key="bed.id" class="bed-card" :class="{ occupied: bed.studentId }">
        <div class="bed-icon">{{ bed.studentId ? '●' : '○' }}</div>
        <strong>{{ bed.bedNo }}</strong><span>{{ bed.studentName || '空闲' }}</span>
        <div class="bed-actions">
          <el-button v-if="bed.studentId" link type="primary" @click="clickBed(bed)">学生详情</el-button>
          <el-button v-if="bed.studentId && !readonly" link type="danger" @click="checkout(bed)">退宿</el-button>
          <el-button v-else-if="!readonly" link type="primary" @click="startAssign(bed)">分配学生</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
  <el-dialog v-model="studentDialog" title="学生详情" width="520px">
    <el-descriptions v-if="currentStudent" :column="2" border>
      <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item><el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ currentStudent.gender || '-' }}</el-descriptions-item><el-descriptions-item label="手机">{{ currentStudent.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="学院" :span="2">{{ currentStudent.college || '-' }}</el-descriptions-item><el-descriptions-item label="专业">{{ currentStudent.major || '-' }}</el-descriptions-item>
      <el-descriptions-item label="班级">{{ currentStudent.className || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
  <el-dialog v-model="assignDialog" title="分配学生" width="440px">
    <el-select v-model="assignStudentId" filterable style="width:100%" placeholder="按姓名或学号选择学生">
      <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name}（${s.studentNo}）`" :value="s.id" />
    </el-select>
    <template #footer><el-button @click="assignDialog=false">取消</el-button><el-button type="primary" :disabled="!assignStudentId" @click="assign">确认分配</el-button></template>
  </el-dialog>
</template>
