<template>
  <div class="login-page">
    <div class="login-box">
      <h1>校园活动报名系统</h1>
      <p class="muted">使用演示账号登录：20230001 / 计算机协会 / 系统管理员，密码均为 123456</p>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="username" placeholder="用户名 / 学号 / 手机号" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" placeholder="密码" type="password" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const username = ref('20230001')
const password = ref('123456')
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    const user = await userStore.login(username.value, password.value)
    if (user.role === 'STUDENT') router.push('/student/activities')
    if (user.role === 'ORGANIZER') router.push('/organizer/activities')
    if (user.role === 'ADMIN') router.push('/admin/reviews')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #edf7f4, #f8fafc 45%, #eef2ff);
}

.login-box {
  width: min(420px, calc(100vw - 32px));
  padding: 28px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.login-box h1 {
  margin: 0 0 10px;
  font-size: 24px;
}

.el-button {
  width: 100%;
}
</style>
