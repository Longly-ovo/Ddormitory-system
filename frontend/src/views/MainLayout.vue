<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute(); const router = useRouter(); const auth = useAuthStore()
const roleNames = { ADMIN: '管理员', DORM_MANAGER: '宿管', COUNSELOR: '辅导员', STUDENT: '学生' }
function logout() { auth.logout(); router.push('/login') }
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-brand"><span>D</span><strong>智慧宿舍</strong></div>
      <nav>
        <router-link v-if="auth.role === 'ADMIN' || auth.role === 'DORM_MANAGER' || auth.role === 'COUNSELOR'" to="/map" :class="{ active: route.path === '/map' }">楼层地图</router-link>
        <router-link v-if="auth.role === 'ADMIN' || auth.role === 'COUNSELOR'" to="/students" :class="{ active: route.path === '/students' }">{{ auth.role === 'COUNSELOR' ? '学生信息' : '学生管理' }}</router-link>
        <router-link v-if="auth.role === 'ADMIN' || auth.role === 'DORM_MANAGER'" to="/structure" :class="{ active: route.path === '/structure' }">宿舍结构</router-link>
        <router-link v-if="auth.role === 'STUDENT'" to="/my-dormitory" :class="{ active: route.path === '/my-dormitory' }">我的宿舍</router-link>
      </nav>
      <div class="sidebar-foot">MVP · 简洁可运行</div>
    </aside>
    <main class="main-area">
      <header class="topbar">
        <span class="mobile-title">智慧宿舍管理系统</span>
        <div class="user-info">
          <span>{{ auth.nickname || '用户' }} · {{ auth.role ? roleNames[auth.role] : '' }}</span>
          <el-tag v-if="auth.role === 'STUDENT' && auth.mustChangePassword" type="warning" size="small">请修改初始密码</el-tag>
          <el-button v-if="auth.role === 'STUDENT'" link type="primary" @click="router.push('/change-password')">修改密码</el-button>
          <el-button link type="primary" @click="logout">退出登录</el-button>
        </div>
      </header>
      <section class="content"><router-view /></section>
    </main>
  </div>
</template>
