<template>
  <el-container class="app-shell">
    <el-aside width="220px">
      <div class="brand">校园活动</div>
      <el-menu router :default-active="$route.path">
        <template v-for="item in menus" :key="item.path">
          <el-menu-item :index="item.path">{{ item.label }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div>{{ userStore.user?.name }} · {{ userStore.user?.role }}</div>
        <el-button @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const menus = computed(() => {
  if (userStore.user?.role === 'STUDENT') {
    return [
      { path: '/student/activities', label: '活动大厅' },
      { path: '/student/my-registrations', label: '我的活动' },
    ]
  }
  if (userStore.user?.role === 'ORGANIZER') {
    return [
      { path: '/organizer/activities', label: '活动管理' },
      { path: '/organizer/activities/new', label: '创建活动' },
    ]
  }
  return [
    { path: '/admin/reviews', label: '审核中心' },
    { path: '/admin/dictionaries', label: '资源管理' },
    { path: '/admin/stats', label: '统计概览' },
    { path: '/organizer/activities', label: '活动管理' },
  ]
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.el-aside {
  background: #fff;
  border-right: 1px solid #e5e7eb;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-weight: 700;
  border-bottom: 1px solid #e5e7eb;
}

.el-header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.el-main {
  padding: 0;
}
</style>
