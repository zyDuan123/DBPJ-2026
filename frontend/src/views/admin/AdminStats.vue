<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">统计概览</h1>
        <p class="page-subtitle">面向管理员的运行视图，帮助快速判断活动供给、报名转化和场地使用情况。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="metric-grid">
      <div class="metric"><span>活动总数</span><strong>{{ overview.activityCount }}</strong></div>
      <div class="metric"><span>待审核</span><strong>{{ overview.pendingReviewCount }}</strong></div>
      <div class="metric"><span>已发布</span><strong>{{ overview.publishedCount }}</strong></div>
      <div class="metric"><span>报名记录</span><strong>{{ overview.registrationCount }}</strong></div>
      <div class="metric"><span>已签到</span><strong>{{ overview.checkedInCount }}</strong></div>
    </div>
    <div class="page-split" style="margin-top: 16px">
      <div class="panel">
        <div class="table-title">
          <h2 class="section-title">校区使用</h2>
          <span class="muted">按活动数量和场地数量观察资源分布</span>
        </div>
        <el-table :data="campusUsage" stripe>
            <el-table-column prop="campusName" label="校区" />
            <el-table-column prop="activityCount" label="活动数" />
            <el-table-column prop="venueCount" label="场地数" />
          </el-table>
      </div>
      <div class="soft-panel">
        <h2 class="section-title">运行摘要</h2>
        <div class="timeline-list">
          <div class="timeline-item">
            <strong>{{ checkInRate }}%</strong>
            <span class="muted">当前签到率</span>
          </div>
          <div class="timeline-item">
            <strong>通知与审计</strong>
            <span class="muted">二期可接入消息提醒、操作日志和异常报名追踪。</span>
          </div>
          <div class="timeline-item">
            <strong>资源治理</strong>
            <span class="muted">结合字典维护逐步沉淀校区、场地、分类标准。</span>
          </div>
        </div>
      </div>
    </div>
    <div class="panel" style="margin-top: 16px">
      <div class="table-title">
        <h2 class="section-title">分类热度</h2>
        <span class="muted">辅助判断活动类型供给是否均衡</span>
      </div>
      <el-table :data="categoryPopularity" stripe>
            <el-table-column prop="categoryName" label="分类" />
            <el-table-column prop="activityCount" label="活动数" />
            <el-table-column prop="averageEnrollment" label="平均报名" />
          </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCampusUsage, getCategoryPopularity, getOverview } from '../../api/stats'

const overview = ref<any>({})
const campusUsage = ref<any[]>([])
const categoryPopularity = ref<any[]>([])
const checkInRate = computed(() => {
  const total = Number(overview.value.registrationCount || 0)
  if (!total) return 0
  return Math.round((Number(overview.value.checkedInCount || 0) / total) * 100)
})

async function load() {
  overview.value = await getOverview()
  campusUsage.value = await getCampusUsage() as any[]
  categoryPopularity.value = await getCategoryPopularity() as any[]
}

onMounted(load)
</script>
