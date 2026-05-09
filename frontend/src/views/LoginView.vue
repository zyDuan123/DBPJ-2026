<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-intro">
        <div class="brand-row">
          <div class="brand-mark">C</div>
          <div>
            <strong>校园活动报名系统</strong>
            <span>Campus Activity Hub</span>
          </div>
        </div>
        <h1>活动发现、报名候补、现场签到与反馈复盘一站完成</h1>
        <p>面向学生、组织者和管理员的课程项目演示系统，二期正在补充评价反馈、信用记录和运营看板。</p>
        <div class="intro-metrics">
          <div><strong>3</strong><span>角色门户</span></div>
          <div><strong>2</strong><span>期能力</span></div>
          <div><strong>1</strong><span>完整闭环</span></div>
        </div>
      </section>

      <section class="login-card">
        <div class="login-card-header">
          <div>
            <h2>欢迎回来</h2>
            <p class="muted">选择演示账号或输入账号登录</p>
          </div>
          <el-tag type="success" effect="plain">Demo</el-tag>
        </div>
        <div class="demo-users">
          <button v-for="item in demoUsers" :key="item.username" type="button" @click="fillDemo(item.username)">
            <strong>{{ item.label }}</strong>
            <span>{{ item.username }}</span>
          </button>
        </div>
        <el-form class="login-form" @submit.prevent>
          <el-form-item>
            <el-input v-model="username" size="large" placeholder="用户名 / 学号 / 手机号" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="password" size="large" placeholder="密码" type="password" show-password />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin">登录系统</el-button>
        </el-form>
        <p class="login-tip">演示密码均为 123456</p>
      </section>
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
const demoUsers = [
  { label: '学生', username: '20230001' },
  { label: '组织者', username: '计算机协会' },
  { label: '管理员', username: '系统管理员' },
]

function fillDemo(value: string) {
  username.value = value
  password.value = '123456'
}

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
  padding: 32px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.92), rgba(30, 64, 175, 0.78)),
    url("https://images.unsplash.com/photo-1523580846011-d3a5bc25702b?auto=format&fit=crop&w=1800&q=80") center / cover;
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 430px;
  gap: 28px;
  width: min(1080px, 100%);
  align-items: stretch;
}

.login-intro {
  min-height: 560px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 34px;
  color: #fff;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #2563eb, #14b8a6);
  border-radius: 8px;
  font-weight: 800;
}

.brand-row strong,
.brand-row span {
  display: block;
}

.brand-row span {
  margin-top: 3px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.login-intro h1 {
  max-width: 620px;
  margin: auto 0 16px;
  font-size: 42px;
  line-height: 1.18;
  font-weight: 800;
}

.login-intro p {
  max-width: 560px;
  margin: 0 0 28px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 16px;
  line-height: 1.8;
}

.intro-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.intro-metrics div {
  padding: 14px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
}

.intro-metrics strong,
.intro-metrics span {
  display: block;
}

.intro-metrics strong {
  font-size: 24px;
}

.intro-metrics span {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.login-card {
  align-self: center;
  padding: 30px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 8px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.24);
}

.login-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.login-card h2 {
  margin: 0;
  font-size: 26px;
}

.login-card-header p {
  margin: 6px 0 0;
}

.demo-users {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 22px;
}

.demo-users button {
  padding: 10px;
  text-align: left;
  cursor: pointer;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.demo-users button:hover {
  border-color: #2563eb;
  background: #eff6ff;
}

.demo-users strong,
.demo-users span {
  display: block;
}

.demo-users strong {
  color: #111827;
}

.demo-users span {
  margin-top: 3px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-form {
  margin-top: 8px;
}

.el-button {
  width: 100%;
}

.login-tip {
  margin: 16px 0 0;
  color: #64748b;
  text-align: center;
  font-size: 13px;
}

@media (max-width: 900px) {
  .login-page {
    padding: 18px;
  }

  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-intro {
    min-height: auto;
    padding: 12px 4px;
  }

  .login-intro h1 {
    margin-top: 44px;
    font-size: 30px;
  }

  .intro-metrics {
    grid-template-columns: 1fr;
  }

  .login-card {
    padding: 22px;
  }
}
</style>
