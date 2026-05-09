<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">我的活动</h1>
        <p class="page-subtitle">跟踪正选、候补、签到和取消记录，二期会在这里接入评价反馈与信用记录。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="metric-grid" style="margin-bottom: 16px">
      <div class="metric"><span>当前记录</span><strong>{{ records.length }}</strong></div>
      <div class="metric"><span>正选活动</span><strong>{{ countByStatus('ENROLLED') }}</strong></div>
      <div class="metric"><span>候补排队</span><strong>{{ countByStatus('WAITLISTED') }}</strong></div>
      <div class="metric"><span>完成签到</span><strong>{{ countByStatus('CHECKED_IN') }}</strong></div>
      <div class="metric"><span>信用分</span><strong>{{ creditScore }}</strong></div>
    </div>
    <div class="page-split">
      <div class="panel">
        <el-tabs v-model="status" @tab-change="load">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane label="正选" name="ENROLLED" />
          <el-tab-pane label="候补" name="WAITLISTED" />
          <el-tab-pane label="已签到" name="CHECKED_IN" />
          <el-tab-pane label="已取消" name="CANCELLED" />
          <el-tab-pane label="缺勤" name="ABSENT" />
        </el-tabs>
        <el-table :data="records" stripe>
          <el-table-column prop="title" label="活动" min-width="180" />
          <el-table-column prop="startTime" label="时间" width="180" />
          <el-table-column label="地点" min-width="180">
            <template #default="{ row }">{{ row.campusName }} {{ row.venueName }} {{ row.roomNumber }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.registrationStatus)">{{ RegistrationStatusText[row.registrationStatus] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="queueNo" label="候补序号" width="100" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="['ENROLLED', 'WAITLISTED'].includes(row.registrationStatus)" link type="danger" @click="cancel(row.registrationId)">取消</el-button>
              <el-button v-if="row.registrationStatus === 'CHECKED_IN'" link type="success" @click="openFeedback(row)">评价</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="soft-panel">
        <h2 class="section-title">信用记录</h2>
        <div class="timeline-list">
          <div v-if="creditRecords.length === 0" class="timeline-item">
            <strong>暂无信用流水</strong>
            <span class="muted">完成签到或被标记缺勤后会生成记录。</span>
          </div>
          <div v-for="item in creditRecords" :key="item.recordId" class="timeline-item">
            <strong :class="Number(item.changeValue) >= 0 ? 'credit-plus' : 'credit-minus'">
              {{ Number(item.changeValue) >= 0 ? '+' : '' }}{{ item.changeValue }}
            </strong>
            <span class="muted">{{ item.reason }} · {{ item.activityTitle || '系统记录' }}</span>
          </div>
          <div class="timeline-item">
            <strong>规则说明</strong>
            <span class="muted">基础分 100，签到 +1，活动结束后未签到 -10。</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="feedbackVisible" title="活动评价" width="560px">
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate v-model="feedbackForm.rating" show-score />
        </el-form-item>
        <el-form-item label="反馈内容">
          <el-input v-model="feedbackForm.content" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="可以写下活动内容、场地、组织流程等建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" :loading="feedbackLoading" @click="saveFeedback">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyCredit } from '../../api/credit'
import { getMyFeedback, submitFeedback } from '../../api/feedback'
import { cancelRegistration, getMyRegistrations } from '../../api/registration'
import { RegistrationStatusText, statusTag } from '../../utils/enums'

const status = ref('')
const records = ref<any[]>([])
const feedbackVisible = ref(false)
const feedbackLoading = ref(false)
const currentRecord = ref<any>(null)
const feedbackForm = ref({ rating: 5, content: '' })
const creditScore = ref(100)
const creditRecords = ref<any[]>([])

function countByStatus(value: string) {
  return records.value.filter((item) => item.registrationStatus === value).length
}

async function load() {
  const data = await getMyRegistrations({ status: status.value || undefined, page: 1, size: 50 }) as any
  records.value = data.list
  const credit = await getMyCredit({ page: 1, size: 6 }) as any
  creditScore.value = credit.score
  creditRecords.value = credit.records?.list || []
}

async function cancel(id: number) {
  await cancelRegistration(id)
  await load()
}

async function openFeedback(row: any) {
  currentRecord.value = row
  feedbackForm.value = { rating: 5, content: '' }
  const existing = await getMyFeedback(row.activityId) as any
  if (existing?.feedbackId) {
    feedbackForm.value = {
      rating: Number(existing.rating || 5),
      content: existing.content || '',
    }
  }
  feedbackVisible.value = true
}

async function saveFeedback() {
  if (!currentRecord.value) return
  feedbackLoading.value = true
  try {
    await submitFeedback(currentRecord.value.activityId, feedbackForm.value)
    ElMessage.success('评价已提交')
    feedbackVisible.value = false
  } finally {
    feedbackLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.credit-plus {
  color: #059669;
}

.credit-minus {
  color: #dc2626;
}
</style>
