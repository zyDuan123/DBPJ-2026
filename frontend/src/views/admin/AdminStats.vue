<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">统计概览</h1>
        <p class="page-subtitle">面向管理员的运行视图，快速判断活动供给、报名转化、反馈质量和信用风险。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric"><span>活动总数</span><strong>{{ overview.activityCount }}</strong></div>
      <div class="metric"><span>待审核</span><strong>{{ overview.pendingReviewCount }}</strong></div>
      <div class="metric"><span>已发布</span><strong>{{ overview.publishedCount }}</strong></div>
      <div class="metric"><span>报名记录</span><strong>{{ overview.registrationCount }}</strong></div>
      <div class="metric"><span>已签到</span><strong>{{ overview.checkedInCount }}</strong></div>
      <div class="metric"><span>评价均分</span><strong>{{ feedbackSummary.averageRating || 0 }}</strong></div>
      <div class="metric"><span>低分反馈</span><strong>{{ feedbackSummary.lowRatingCount || 0 }}</strong></div>
      <div class="metric"><span>缺勤记录</span><strong>{{ creditSummary.absentCount || 0 }}</strong></div>
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
            <strong>{{ feedbackSummary.positiveRate || 0 }}%</strong>
            <span class="muted">评价正向比例</span>
          </div>
          <div class="timeline-item">
            <strong>{{ creditSummary.totalChange || 0 }}</strong>
            <span class="muted">全站信用分净变化</span>
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

    <div class="panel" style="margin-top: 16px">
      <div class="table-title">
        <h2 class="section-title">高分活动反馈</h2>
        <span class="muted">来自二期评价模块</span>
      </div>
      <el-table :data="topFeedbackActivities" stripe>
        <el-table-column prop="title" label="活动" min-width="220" />
        <el-table-column prop="feedbackCount" label="反馈数" width="120" />
        <el-table-column prop="averageRating" label="平均评分" width="120" />
      </el-table>
    </div>

    <div class="page-split" style="margin-top: 16px">
      <div class="panel">
        <div class="table-title">
          <h2 class="section-title">全站评分分布</h2>
          <span class="muted">用于观察整体满意度结构</span>
        </div>
        <div class="rating-bars">
          <div v-for="item in feedbackDistribution" :key="item.rating" class="rating-bar-row">
            <span>{{ item.rating }} 星</span>
            <div class="rating-bar-track">
              <i :style="{ width: `${item.rate || 0}%` }" />
            </div>
            <strong>{{ item.count }}</strong>
          </div>
        </div>
      </div>
      <div class="soft-panel">
        <h2 class="section-title">反馈关键词</h2>
        <div v-if="feedbackKeywords.length" class="keyword-cloud">
          <el-tag v-for="item in feedbackKeywords" :key="item.keyword" effect="plain">
            {{ item.keyword }} {{ item.count }}
          </el-tag>
        </div>
        <div v-else class="muted">暂无可提取的关键词</div>
      </div>
    </div>

    <div class="panel" style="margin-top: 16px">
      <div class="table-title">
        <h2 class="section-title">信用风险学生</h2>
        <span class="muted">按信用分和缺勤次数排序</span>
      </div>
      <el-table :data="riskStudents" stripe>
        <el-table-column prop="studentName" label="学生" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column prop="creditScore" label="信用分" width="120" />
        <el-table-column prop="absentCount" label="缺勤次数" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCreditOverview } from '../../api/credit'
import { getFeedbackOverview } from '../../api/feedback'
import { getCampusUsage, getCategoryPopularity, getOverview } from '../../api/stats'

const overview = ref<any>({})
const campusUsage = ref<any[]>([])
const categoryPopularity = ref<any[]>([])
const feedbackSummary = ref<any>({})
const topFeedbackActivities = ref<any[]>([])
const feedbackDistribution = ref<any[]>([])
const feedbackKeywords = ref<any[]>([])
const creditSummary = ref<any>({})
const riskStudents = ref<any[]>([])
const checkInRate = computed(() => {
  const total = Number(overview.value.registrationCount || 0)
  if (!total) return 0
  return Math.round((Number(overview.value.checkedInCount || 0) / total) * 100)
})

async function load() {
  overview.value = await getOverview()
  campusUsage.value = await getCampusUsage() as any[]
  categoryPopularity.value = await getCategoryPopularity() as any[]
  const feedback = await getFeedbackOverview() as any
  feedbackSummary.value = feedback.summary || {}
  topFeedbackActivities.value = feedback.topActivities || []
  feedbackDistribution.value = feedback.ratingDistribution || []
  feedbackKeywords.value = feedback.keywords || []
  const credit = await getCreditOverview() as any
  creditSummary.value = credit.summary || {}
  riskStudents.value = credit.riskStudents || []
}

onMounted(load)
</script>
