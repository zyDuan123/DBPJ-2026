<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">活动大厅</h1>
        <p class="page-subtitle">发现近期可报名活动，候补和签到状态会在个人中心同步更新。</p>
      </div>
      <el-button @click="load">刷新活动</el-button>
    </div>

    <div class="soft-panel">
      <div class="toolbar">
        <el-input v-model="filters.keyword" placeholder="搜索活动名称" style="width: 260px" clearable />
        <el-select v-model="filters.campusId" placeholder="校区" style="width: 160px" clearable>
          <el-option v-for="item in campuses" :key="item.id" :label="item.campusName" :value="item.id" />
        </el-select>
        <el-select v-model="filters.categoryId" placeholder="分类" style="width: 160px" clearable>
          <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
        </el-select>
        <el-button type="primary" @click="load">筛选</el-button>
      </div>
      <div class="phase-grid">
        <div class="phase-card">
          <strong>{{ activities.length }}</strong>
          <span class="muted">当前展示活动</span>
        </div>
        <div class="phase-card">
          <strong>候补递补</strong>
          <span class="muted">名额满后自动进入队列</span>
        </div>
        <div class="phase-card">
          <strong>现场签到</strong>
          <span class="muted">正选报名后生成签到码</span>
        </div>
      </div>
    </div>

    <div v-if="activities.length" class="activity-grid" style="margin-top: 16px">
      <el-card v-for="item in activities" :key="item.id" class="activity-card">
        <div class="activity-cover"></div>
        <div class="status-row">
          <h2 class="activity-title">{{ item.title }}</h2>
          <el-tag :type="statusTag(item.status)">{{ ActivityStatusText[item.status] }}</el-tag>
        </div>
        <div class="meta-list">
          <div class="meta-item"><span>时间</span><strong>{{ formatTime(item.startTime) }}</strong></div>
          <div class="meta-item"><span>地点</span><strong>{{ item.campusName }} · {{ item.roomNumber }}</strong></div>
          <div class="meta-item"><span>分类</span><strong>{{ item.categoryName }}</strong></div>
        </div>
        <div class="capacity-bar">
          <span :style="{ width: capacityPercent(item) + '%' }"></span>
        </div>
        <div class="status-row" style="margin-top: 12px">
          <span class="muted">名额 {{ item.currentEnrollment }} / {{ item.capacityLimit }}</span>
          <el-button type="primary" @click="$router.push(`/student/activities/${item.id}`)">查看详情</el-button>
        </div>
      </el-card>
    </div>
    <div v-else class="empty-hint" style="margin-top: 16px">
      暂无匹配活动
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getActivities } from '../../api/activity'
import { getCampuses, getCategories } from '../../api/dictionary'
import { ActivityStatusText, statusTag } from '../../utils/enums'

const filters = reactive({ keyword: '', campusId: undefined, categoryId: undefined })
const activities = ref<any[]>([])
const campuses = ref<any[]>([])
const categories = ref<any[]>([])

function capacityPercent(item: any) {
  if (!item.capacityLimit) return 0
  return Math.min(100, Math.round((item.currentEnrollment / item.capacityLimit) * 100))
}

function formatTime(value: string) {
  return value?.slice(0, 16).replace('T', ' ') || '-'
}

async function load() {
  const data = await getActivities({ ...filters, page: 1, size: 50 }) as any
  activities.value = data.list
}

onMounted(async () => {
  campuses.value = await getCampuses() as any[]
  categories.value = await getCategories() as any[]
  await load()
})
</script>
