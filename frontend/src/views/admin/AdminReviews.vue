<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">审核中心</h1>
        <p class="page-subtitle">优先处理待审核活动，减少场地冲突和信息不完整带来的联调问题。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="metric-grid" style="margin-bottom: 16px">
      <div class="metric"><span>待审核</span><strong>{{ activities.length }}</strong></div>
      <div class="metric"><span>今日队列</span><strong>{{ todayCount }}</strong></div>
      <div class="metric"><span>二期预留</span><strong>审计</strong></div>
    </div>
    <div class="page-split">
      <div class="panel">
        <div class="table-title">
          <h2 class="section-title">审核队列</h2>
          <span class="muted">通过后学生端即可报名</span>
        </div>
        <el-table :data="activities" stripe>
          <el-table-column prop="title" label="活动" min-width="180" />
          <el-table-column prop="organizerName" label="组织者" width="140" />
          <el-table-column prop="startTime" label="时间" width="180" />
          <el-table-column label="地点" min-width="200">
            <template #default="{ row }">{{ row.campusName }} {{ row.venueName }} {{ row.roomNumber }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="approve(row.id)">通过</el-button>
              <el-button link type="danger" @click="reject(row.id)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="soft-panel">
        <h2 class="section-title">审核要点</h2>
        <div class="timeline-list">
          <div class="timeline-item">
            <strong>时间与场地</strong>
            <span class="muted">确认活动时间、报名截止和场地占用合理。</span>
          </div>
          <div class="timeline-item">
            <strong>活动信息</strong>
            <span class="muted">标题、分类、组织者和详情应足够清晰。</span>
          </div>
          <div class="timeline-item">
            <strong>操作留痕</strong>
            <span class="muted">二期将记录审核人、结果和原因。</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getActivities, reviewActivity } from '../../api/activity'

const activities = ref<any[]>([])
const today = new Date().toISOString().slice(0, 10)
const todayCount = computed(() => activities.value.filter((item) => String(item.startTime || '').startsWith(today)).length)

async function load() {
  const data = await getActivities({ status: 'PENDING_REVIEW', page: 1, size: 50 }) as any
  activities.value = data.list
}

async function approve(id: number) {
  await reviewActivity(id, { result: 'APPROVED' })
  await load()
}

async function reject(id: number) {
  const reason = await ElMessageBox.prompt('请输入驳回原因', '驳回活动')
  await reviewActivity(id, { result: 'REJECTED', reason: reason.value })
  await load()
}

onMounted(load)
</script>
