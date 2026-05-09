<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">反馈看板</h1>
        <p class="page-subtitle">查看活动评分、正向反馈比例和学生建议，便于活动复盘。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="metric-grid" style="margin-bottom: 16px">
      <div class="metric"><span>反馈数量</span><strong>{{ summary.feedbackCount || 0 }}</strong></div>
      <div class="metric"><span>平均评分</span><strong>{{ summary.averageRating || 0 }}</strong></div>
      <div class="metric"><span>正向比例</span><strong>{{ summary.positiveRate || 0 }}%</strong></div>
    </div>

    <div class="page-split">
      <div class="panel">
        <div class="table-title">
          <h2 class="section-title">反馈明细</h2>
          <span class="muted">按最近更新时间排序</span>
        </div>
        <el-table :data="records" stripe>
          <el-table-column prop="studentName" label="学生" width="140" />
          <el-table-column prop="studentNo" label="学号" width="130" />
          <el-table-column label="评分" width="180">
            <template #default="{ row }">
              <el-rate :model-value="row.rating" disabled />
            </template>
          </el-table-column>
          <el-table-column prop="content" label="反馈内容" min-width="220" show-overflow-tooltip />
          <el-table-column prop="updatedAt" label="更新时间" width="180" />
        </el-table>
      </div>

      <div class="soft-panel">
        <h2 class="section-title">复盘建议</h2>
        <div class="timeline-list">
          <div class="timeline-item">
            <strong>评分低于 4 分</strong>
            <span class="muted">重点查看内容、场地、流程组织是否存在共性问题。</span>
          </div>
          <div class="timeline-item">
            <strong>高频建议</strong>
            <span class="muted">可作为下次活动描述、资源申请和现场安排依据。</span>
          </div>
          <div class="timeline-item">
            <strong>后续扩展</strong>
            <span class="muted">可继续接入关键词统计和导出复盘报告。</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getActivityFeedback } from '../../api/feedback'

const route = useRoute()
const summary = ref<any>({})
const records = ref<any[]>([])

async function load() {
  const data = await getActivityFeedback(route.params.id as string, { page: 1, size: 100 }) as any
  summary.value = data.summary || {}
  records.value = data.records?.list || []
}

onMounted(load)
</script>
