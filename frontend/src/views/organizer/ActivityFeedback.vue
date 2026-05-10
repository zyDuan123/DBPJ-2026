<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">反馈看板</h1>
        <p class="page-subtitle">查看活动评分、低分反馈、评分分布和关键词摘要，辅助活动复盘。</p>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button :type="lowRatingOnly ? 'default' : 'primary'" @click="setLowRating(false)">全部反馈</el-button>
          <el-button :type="lowRatingOnly ? 'primary' : 'default'" @click="setLowRating(true)">低分反馈</el-button>
        </el-button-group>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div class="metric-grid" style="margin-bottom: 16px">
      <div class="metric"><span>反馈数量</span><strong>{{ summary.feedbackCount || 0 }}</strong></div>
      <div class="metric"><span>平均评分</span><strong>{{ summary.averageRating || 0 }}</strong></div>
      <div class="metric"><span>正向比例</span><strong>{{ summary.positiveRate || 0 }}%</strong></div>
      <div class="metric"><span>低分反馈</span><strong>{{ summary.lowRatingCount || 0 }}</strong></div>
    </div>

    <div class="page-split">
      <div class="panel">
        <div class="table-title">
          <h2 class="section-title">反馈明细</h2>
          <span class="muted">{{ lowRatingOnly ? '仅显示 3 分及以下反馈' : '按最近更新时间排序' }}</span>
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
        <h2 class="section-title">评分分布</h2>
        <div class="rating-bars">
          <div v-for="item in ratingDistribution" :key="item.rating" class="rating-bar-row">
            <span>{{ item.rating }} 星</span>
            <div class="rating-bar-track">
              <i :style="{ width: `${item.rate || 0}%` }" />
            </div>
            <strong>{{ item.count }}</strong>
          </div>
        </div>

        <h2 class="section-title" style="margin-top: 18px">关键词摘要</h2>
        <div v-if="keywords.length" class="keyword-cloud">
          <el-tag v-for="item in keywords" :key="item.keyword" effect="plain">
            {{ item.keyword }} {{ item.count }}
          </el-tag>
        </div>
        <div v-else class="muted">暂无可提取的关键词</div>
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
const ratingDistribution = ref<any[]>([])
const keywords = ref<any[]>([])
const lowRatingOnly = ref(false)

async function load() {
  const data = await getActivityFeedback(route.params.id as string, {
    page: 1,
    size: 100,
    lowRatingOnly: lowRatingOnly.value,
  }) as any
  summary.value = data.summary || {}
  records.value = data.records?.list || []
  ratingDistribution.value = data.ratingDistribution || []
  keywords.value = data.keywords || []
}

function setLowRating(value: boolean) {
  lowRatingOnly.value = value
  load()
}

onMounted(load)
</script>
