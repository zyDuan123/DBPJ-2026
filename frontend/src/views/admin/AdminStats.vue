<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">统计概览</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="metric-grid">
      <div class="metric"><span>活动总数</span><strong>{{ overview.activityCount }}</strong></div>
      <div class="metric"><span>待审核</span><strong>{{ overview.pendingReviewCount }}</strong></div>
      <div class="metric"><span>已发布</span><strong>{{ overview.publishedCount }}</strong></div>
      <div class="metric"><span>报名记录</span><strong>{{ overview.registrationCount }}</strong></div>
      <div class="metric"><span>已签到</span><strong>{{ overview.checkedInCount }}</strong></div>
    </div>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <div class="panel">
          <h3>校区使用</h3>
          <el-table :data="campusUsage">
            <el-table-column prop="campusName" label="校区" />
            <el-table-column prop="activityCount" label="活动数" />
            <el-table-column prop="venueCount" label="场地数" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="panel">
          <h3>分类热度</h3>
          <el-table :data="categoryPopularity">
            <el-table-column prop="categoryName" label="分类" />
            <el-table-column prop="activityCount" label="活动数" />
            <el-table-column prop="averageEnrollment" label="平均报名" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getCampusUsage, getCategoryPopularity, getOverview } from '../../api/stats'

const overview = ref<any>({})
const campusUsage = ref<any[]>([])
const categoryPopularity = ref<any[]>([])

async function load() {
  overview.value = await getOverview()
  campusUsage.value = await getCampusUsage() as any[]
  categoryPopularity.value = await getCategoryPopularity() as any[]
}

onMounted(load)
</script>
