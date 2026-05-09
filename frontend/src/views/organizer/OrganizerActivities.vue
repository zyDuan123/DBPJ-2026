<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">活动管理</h1>
        <p class="page-subtitle">集中查看草稿、审核进度、报名数据和现场签到入口。</p>
      </div>
      <el-button type="primary" @click="$router.push('/organizer/activities/new')">创建活动</el-button>
    </div>
    <div class="metric-grid" style="margin-bottom: 16px">
      <div class="metric"><span>全部活动</span><strong>{{ activities.length }}</strong></div>
      <div class="metric"><span>待审核</span><strong>{{ countByStatus('PENDING_REVIEW') }}</strong></div>
      <div class="metric"><span>已发布</span><strong>{{ countByStatus('PUBLISHED') }}</strong></div>
      <div class="metric"><span>总报名</span><strong>{{ totalEnrollment }}</strong></div>
    </div>
    <el-table :data="activities" class="panel" stripe>
      <el-table-column prop="title" label="活动" min-width="180" />
      <el-table-column prop="startTime" label="时间" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ ActivityStatusText[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报名" width="180">
        <template #default="{ row }">
          <div class="capacity-bar">
            <span :style="{ width: capacityPercent(row) + '%' }"></span>
          </div>
          <span class="muted">{{ row.currentEnrollment }} / {{ row.capacityLimit }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回原因" min-width="180" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/organizer/activities/${row.id}/registrations`)">名单</el-button>
          <el-button v-if="['DRAFT', 'REJECTED'].includes(row.status)" link @click="$router.push(`/organizer/activities/${row.id}/edit`)">编辑</el-button>
          <el-button v-if="['DRAFT', 'REJECTED'].includes(row.status)" link type="primary" @click="submit(row.id)">提交</el-button>
          <el-button v-if="!['CANCELLED', 'FINISHED'].includes(row.status)" link type="danger" @click="cancel(row.id)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cancelActivity, getActivities, submitActivity } from '../../api/activity'
import { ActivityStatusText, statusTag } from '../../utils/enums'

const activities = ref<any[]>([])

const totalEnrollment = computed(() => activities.value.reduce((sum, item) => sum + Number(item.currentEnrollment || 0), 0))

function countByStatus(status: string) {
  return activities.value.filter((item) => item.status === status).length
}

function capacityPercent(item: any) {
  if (!item.capacityLimit) return 0
  return Math.min(100, Math.round((item.currentEnrollment / item.capacityLimit) * 100))
}

async function load() {
  const data = await getActivities({ mine: true, page: 1, size: 50 }) as any
  activities.value = data.list
}

async function submit(id: number) {
  await submitActivity(id)
  ElMessage.success('已提交审核')
  await load()
}

async function cancel(id: number) {
  await cancelActivity(id)
  await load()
}

onMounted(load)
</script>
