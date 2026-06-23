<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { get, post, put, remove } from '../api'
import type { Bed, Building, Floor, Room } from '../types'
import { ElMessage, ElMessageBox } from 'element-plus'

const buildings = ref<Building[]>([]); const floors = ref<Floor[]>([]); const rooms = ref<Room[]>([]); const beds = ref<Bed[]>([])
const buildingId = ref<number>(); const floorId = ref<number>(); const roomId = ref<number>()
const dialog = ref(false); const kind = ref<'building'|'floor'|'room'|'bed'>('building'); const editingId = ref<number>()
const form = reactive<any>({})
async function loadBuildings() { buildings.value = await get('/buildings'); if (!buildingId.value) buildingId.value = buildings.value[0]?.id }
watch(buildingId, async id => { floors.value = id ? await get(`/buildings/${id}/floors`) : []; floorId.value = floors.value[0]?.id })
watch(floorId, async id => { rooms.value = id ? await get(`/floors/${id}/rooms`) : []; roomId.value = rooms.value[0]?.id })
watch(roomId, async id => { beds.value = id ? await get(`/rooms/${id}/beds`) : [] })
onMounted(loadBuildings)
function open(type: typeof kind.value, row?: any) {
  kind.value = type; editingId.value = row?.id; Object.keys(form).forEach(k => delete form[k])
  if (row) Object.assign(form, row)
  else if (type === 'floor') Object.assign(form, { buildingId: buildingId.value, floorNo: floors.value.length + 1, name: '' })
  else if (type === 'room') Object.assign(form, { floorId: floorId.value, roomNo: '', sortOrder: rooms.value.length + 1 })
  else if (type === 'bed') Object.assign(form, { roomId: roomId.value, bedNo: '' })
  else Object.assign(form, { name: '', description: '' })
  dialog.value = true
}
async function save() {
  const url = `/${kind.value === 'building' ? 'buildings' : kind.value === 'floor' ? 'floors' : kind.value === 'room' ? 'rooms' : 'beds'}`
  if (editingId.value) await put(`${url}/${editingId.value}`, form); else await post(url, form)
  ElMessage.success('保存成功'); dialog.value = false; await reload(kind.value)
}
async function del(type: typeof kind.value, id?: number) {
  await ElMessageBox.confirm('仅可删除未被下级数据使用的记录，确认继续？', '删除确认', { type:'warning' })
  const url = type === 'building' ? 'buildings' : type === 'floor' ? 'floors' : type === 'room' ? 'rooms' : 'beds'
  await remove(`/${url}/${id}`); ElMessage.success('删除成功'); await reload(type)
}
async function reload(type: typeof kind.value) {
  if (type === 'building') { buildingId.value = undefined; await loadBuildings() }
  else if (type === 'floor') { floorId.value = undefined; floors.value = await get(`/buildings/${buildingId.value}/floors`); floorId.value = floors.value[0]?.id }
  else if (type === 'room') { roomId.value = undefined; rooms.value = await get(`/floors/${floorId.value}/rooms`); roomId.value = rooms.value[0]?.id }
  else beds.value = await get(`/rooms/${roomId.value}/beds`)
}
</script>

<template>
  <div class="page-head"><div><h1>宿舍结构</h1><p>按楼栋、楼层、寝室、床位逐级维护</p></div></div>
  <div class="structure-grid">
    <section class="structure-panel"><header><strong>楼栋</strong><el-button link type="primary" @click="open('building')">新增</el-button></header><div v-for="x in buildings" :key="x.id" class="structure-row" :class="{selected:buildingId===x.id}" @click="buildingId=x.id"><span>{{ x.name }}</span><i><el-button link @click.stop="open('building',x)">编辑</el-button><el-button link type="danger" @click.stop="del('building',x.id)">删除</el-button></i></div><el-empty v-if="!buildings.length" :image-size="50" /></section>
    <section class="structure-panel"><header><strong>楼层</strong><el-button link type="primary" :disabled="!buildingId" @click="open('floor')">新增</el-button></header><div v-for="x in floors" :key="x.id" class="structure-row" :class="{selected:floorId===x.id}" @click="floorId=x.id"><span>{{ x.name }}</span><i><el-button link @click.stop="open('floor',x)">编辑</el-button><el-button link type="danger" @click.stop="del('floor',x.id)">删除</el-button></i></div><el-empty v-if="!floors.length" :image-size="50" /></section>
    <section class="structure-panel"><header><strong>寝室</strong><el-button link type="primary" :disabled="!floorId" @click="open('room')">新增</el-button></header><div v-for="x in rooms" :key="x.id" class="structure-row" :class="{selected:roomId===x.id}" @click="roomId=x.id"><span>{{ x.roomNo }}</span><i><el-button link @click.stop="open('room',x)">编辑</el-button><el-button link type="danger" @click.stop="del('room',x.id)">删除</el-button></i></div><el-empty v-if="!rooms.length" :image-size="50" /></section>
    <section class="structure-panel"><header><strong>床位</strong><el-button link type="primary" :disabled="!roomId" @click="open('bed')">新增</el-button></header><div v-for="x in beds" :key="x.id" class="structure-row"><span>{{ x.bedNo }} · {{ x.studentName || '空闲' }}</span><i><el-button link @click.stop="open('bed',x)">编辑</el-button><el-button link type="danger" @click.stop="del('bed',x.id)">删除</el-button></i></div><el-empty v-if="!beds.length" :image-size="50" /></section>
  </div>
  <el-dialog v-model="dialog" :title="`${editingId ? '编辑' : '新增'}${kind==='building'?'楼栋':kind==='floor'?'楼层':kind==='room'?'寝室':'床位'}`" width="460px">
    <el-form label-width="80px">
      <template v-if="kind==='building'"><el-form-item label="楼栋名称"><el-input v-model="form.name" /></el-form-item><el-form-item label="简介"><el-input v-model="form.description" /></el-form-item></template>
      <template v-else-if="kind==='floor'"><el-form-item label="楼层号"><el-input-number v-model="form.floorNo" :min="-5" /></el-form-item><el-form-item label="显示名称"><el-input v-model="form.name" /></el-form-item></template>
      <template v-else-if="kind==='room'"><el-form-item label="寝室号"><el-input v-model="form.roomNo" /></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item></template>
      <template v-else><el-form-item label="床位号"><el-input v-model="form.bedNo" /></el-form-item></template>
    </el-form>
    <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>
