<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { get, post, put, remove } from '../api'
import type { Student, StudentDormitory, StudentListItem } from '../types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const rows = ref<StudentListItem[]>([]); const keyword = ref(''); const dialog = ref(false); const editingId = ref<number>()
const auth = useAuthStore(); const readonly = auth.role === 'COUNSELOR'
const dormDialog = ref(false); const dormitory = ref<StudentDormitory>()
const empty = (): Student => ({ studentNo: '', name: '', gender: '', phone: '', college: '', major: '', className: '' })
const form = reactive<Student>(empty())
async function load() { rows.value = await get<StudentListItem[]>('/students', keyword.value ? { keyword: keyword.value } : undefined) }
onMounted(load)
function create() { editingId.value = undefined; Object.assign(form, empty()); dialog.value = true }
function edit(row: StudentListItem) {
  editingId.value = row.id
  Object.assign(form, {
    studentNo: row.studentNo,
    name: row.name,
    gender: row.gender || '',
    phone: row.phone || '',
    college: row.college || '',
    major: row.major || '',
    className: row.className || ''
  })
  dialog.value = true
}
async function save() {
  if (editingId.value) {
    await put(`/students/${editingId.value}`, form)
    ElMessage.success('保存成功')
  } else {
    await post('/students', form)
    ElMessage.success(`学生及登录账号创建成功，用户名：${form.studentNo}，初始密码：123456`)
  }
  dialog.value = false; await load()
}
async function del(row: StudentListItem) {
  await ElMessageBox.confirm(`确认删除学生 ${row.name}？`, '删除确认', { type: 'warning' })
  await remove(`/students/${row.id}`); ElMessage.success('删除成功'); await load()
}
async function showDormitory(row: StudentListItem) {
  dormitory.value = await get<StudentDormitory>(`/students/${row.id}/dormitory`)
  dormDialog.value = true
}
</script>

<template>
  <div class="page-head"><div><h1>{{ readonly ? '学生信息' : '学生管理' }}</h1><p>{{ readonly ? '查看学生档案及当前住宿位置' : '维护学生档案，已入住学生需先退宿才能删除' }}</p></div><el-button v-if="!readonly" type="primary" @click="create">新增学生</el-button></div>
  <div class="table-panel"><div class="table-tools"><el-input v-model="keyword" clearable placeholder="搜索学号、姓名、寝室或床位" style="width:320px" @keyup.enter="load" /><el-button @click="load">查询</el-button></div>
    <el-table :data="rows" stripe>
      <el-table-column label="学生信息" min-width="220"><template #default="scope"><strong>{{ scope.row.name }}</strong><div class="muted-line">{{ scope.row.studentNo }} · {{ scope.row.college || '未填写学院' }} · {{ scope.row.className || '未填写班级' }}</div></template></el-table-column>
      <el-table-column prop="dormitoryText" label="当前住宿" min-width="240" />
      <el-table-column label="操作" :width="readonly ? 100 : 220"><template #default="scope"><el-button link type="primary" @click="showDormitory(scope.row)">住宿信息</el-button><el-button v-if="!readonly" link type="primary" @click="edit(scope.row)">编辑</el-button><el-button v-if="!readonly" link type="danger" @click="del(scope.row)">删除</el-button></template></el-table-column>
    </el-table><el-empty v-if="!rows.length" description="暂无学生数据" /></div>
  <el-dialog v-model="dialog" :title="editingId ? '编辑学生' : '新增学生'" width="600px">
    <el-form label-width="76px"><div class="form-grid"><el-form-item label="学号" required><el-input v-model="form.studentNo" :disabled="!!editingId" /></el-form-item><el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item><el-form-item label="性别"><el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item><el-form-item label="学院"><el-input v-model="form.college" /></el-form-item><el-form-item label="专业"><el-input v-model="form.major" /></el-form-item><el-form-item label="班级"><el-input v-model="form.className" /></el-form-item></div></el-form>
    <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" :disabled="!form.studentNo || !form.name" @click="save">保存</el-button></template>
  </el-dialog>
  <el-dialog v-model="dormDialog" title="学生住宿信息" width="560px">
    <el-descriptions v-if="dormitory" :column="2" border>
      <el-descriptions-item label="学号">{{ dormitory.student.studentNo }}</el-descriptions-item><el-descriptions-item label="姓名">{{ dormitory.student.name }}</el-descriptions-item>
      <el-descriptions-item label="楼栋">{{ dormitory.building?.name || '暂未入住' }}</el-descriptions-item><el-descriptions-item label="楼层">{{ dormitory.floor?.name || '-' }}</el-descriptions-item>
      <el-descriptions-item label="房间">{{ dormitory.room?.roomNo || '-' }}</el-descriptions-item><el-descriptions-item label="床位">{{ dormitory.bed?.bedNo || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>
