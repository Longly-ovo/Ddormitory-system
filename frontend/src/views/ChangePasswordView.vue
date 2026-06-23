<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { put } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

async function submit() {
  if (!form.oldPassword || !form.newPassword || !form.confirmPassword) return
  loading.value = true
  try {
    await put('/auth/password', form)
    auth.passwordChanged()
    ElMessage.success('密码修改成功')
    await router.push('/my-dormitory')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="password-page">
    <div class="password-card">
      <div class="page-head"><div><h1>修改登录密码</h1><p>{{ auth.mustChangePassword ? '当前仍在使用统一初始密码，建议立即修改' : '定期修改密码可以保护账号安全' }}</p></div></div>
      <el-alert v-if="auth.mustChangePassword" title="初始密码仅用于首次登录，新密码不能继续使用 123456" type="warning" :closable="false" show-icon />
      <el-form label-width="92px" class="password-form" @keyup.enter="submit">
        <el-form-item label="旧密码" required><el-input v-model="form.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码" required><el-input v-model="form.newPassword" type="password" show-password placeholder="6-64位，不能与旧密码相同" /></el-form-item>
        <el-form-item label="确认密码" required><el-input v-model="form.confirmPassword" type="password" show-password /></el-form-item>
      </el-form>
      <div class="password-actions">
        <el-button v-if="auth.mustChangePassword" @click="router.push('/my-dormitory')">稍后修改</el-button>
        <el-button type="primary" :loading="loading" :disabled="!form.oldPassword || !form.newPassword || !form.confirmPassword" @click="submit">确认修改</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.password-page { max-width: 680px; margin: 30px auto; }
.password-card { padding: 28px; background: white; border: 1px solid #e5ebf3; border-radius: 14px; box-shadow: 0 6px 24px rgba(32,62,98,.05); }
.password-form { margin-top: 24px; }
.password-actions { display: flex; justify-content: flex-end; gap: 10px; }
</style>
