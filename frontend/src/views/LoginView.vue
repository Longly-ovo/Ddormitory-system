<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
async function submit() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    await router.push(auth.role === 'STUDENT' && auth.mustChangePassword ? '/change-password' : '/')
  }
  finally { loading.value = false }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand-mark">D</div>
      <h1>智慧宿舍管理系统</h1>
      <p>楼层、寝室、床位与学生一体化管理</p>
      <el-form @keyup.enter="submit">
        <el-form-item><el-input v-model="form.username" size="large" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" /></el-form-item>
        <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="submit">登录系统</el-button>
      </el-form>
      <div class="demo-tip">演示账号：admin / admin123</div>
    </div>
  </div>
</template>
