<template>
  <div class="page" v-if="activity">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ activity.title }}</h1>
        <p class="page-subtitle">{{ activity.campusName }} · {{ activity.venueName }} {{ activity.roomNumber }}</p>
      </div>
      <el-tag :type="statusTag(activity.status)" size="large">{{ ActivityStatusText[activity.status] }}</el-tag>
    </div>

    <div class="detail-layout">
      <div class="soft-panel">
        <div class="activity-cover detail-cover"></div>
        <h2 class="section-title">活动信息</h2>
        <div class="meta-list">
          <div class="meta-item"><span>开始时间</span><strong>{{ formatTime(activity.startTime) }}</strong></div>
          <div class="meta-item"><span>结束时间</span><strong>{{ formatTime(activity.endTime) }}</strong></div>
          <div class="meta-item"><span>报名截止</span><strong>{{ formatTime(activity.enrollDeadline) }}</strong></div>
          <div class="meta-item"><span>组织者</span><strong>{{ activity.organizerName }}</strong></div>
        </div>
        <h2 class="section-title" style="margin-top: 22px">活动详情</h2>
        <p class="description">{{ activity.description || '暂无活动详情' }}</p>
      </div>

      <div class="panel action-panel">
        <h2 class="section-title">报名状态</h2>
        <div class="capacity-ring">
          <strong>{{ activity.currentEnrollment }}</strong>
          <span>/ {{ activity.capacityLimit }}</span>
        </div>
        <div class="capacity-bar" style="margin: 12px 0 18px">
          <span :style="{ width: capacityPercent + '%' }"></span>
        </div>
        <div class="meta-list">
          <div class="meta-item"><span>剩余名额</span><strong>{{ remaining }}</strong></div>
          <div class="meta-item"><span>我的状态</span>
            <el-tag v-if="activity.registrationStatus" :type="statusTag(activity.registrationStatus)">
              {{ RegistrationStatusText[activity.registrationStatus] }}
            </el-tag>
            <strong v-else>未报名</strong>
          </div>
        </div>
        <div class="action-stack">
          <el-button v-if="!activity.registrationStatus || activity.registrationStatus === 'CANCELLED'" type="primary" size="large" :loading="loading" @click="handleEnroll">立即报名</el-button>
          <el-button v-if="['ENROLLED', 'WAITLISTED'].includes(activity.registrationStatus)" type="danger" size="large" :loading="loading" @click="handleCancel">取消报名</el-button>
          <el-button v-if="activity.registrationStatus === 'ENROLLED'" size="large" @click="showCode">查看签到码</el-button>
        </div>
        <div class="phase-card" style="margin-top: 18px">
          <strong>二期预告</strong>
          <span class="muted">活动结束后将支持评价反馈和信用记录。</span>
        </div>
      </div>
    </div>

    <el-dialog v-model="codeVisible" title="签到码" width="520px">
      <el-input v-model="checkInCode" type="textarea" :rows="5" readonly />
      <p class="muted">现场交给组织者扫描或复制核销。</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivity } from '../../api/activity'
import { cancelRegistration, enroll, getCheckInCode } from '../../api/registration'
import { ActivityStatusText, RegistrationStatusText, statusTag } from '../../utils/enums'

const route = useRoute()
const activity = ref<any>(null)
const loading = ref(false)
const codeVisible = ref(false)
const checkInCode = ref('')

const capacityPercent = computed(() => {
  if (!activity.value?.capacityLimit) return 0
  return Math.min(100, Math.round((activity.value.currentEnrollment / activity.value.capacityLimit) * 100))
})

const remaining = computed(() => Math.max(0, (activity.value?.capacityLimit || 0) - (activity.value?.currentEnrollment || 0)))

function formatTime(value: string) {
  return value?.slice(0, 16).replace('T', ' ') || '-'
}

async function load() {
  activity.value = await getActivity(route.params.id as string)
}

async function handleEnroll() {
  loading.value = true
  try {
    const result = await enroll(route.params.id as string) as any
    ElMessage.success(result.message || '报名成功')
    await load()
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  loading.value = true
  try {
    await cancelRegistration(activity.value.registrationId)
    ElMessage.success('已取消')
    await load()
  } finally {
    loading.value = false
  }
}

async function showCode() {
  const data = await getCheckInCode(activity.value.registrationId) as any
  checkInCode.value = data.checkInCode
  codeVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 16px;
}

.detail-cover {
  height: 150px;
  margin: -18px -18px 18px;
}

.description {
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
}

.action-panel {
  align-self: start;
  position: sticky;
  top: 18px;
}

.capacity-ring {
  display: flex;
  align-items: baseline;
  justify-content: center;
  height: 118px;
  background: #f5f8fc;
  border: 1px solid #e5edf5;
  border-radius: 8px;
}

.capacity-ring strong {
  color: #2563eb;
  font-size: 48px;
}

.capacity-ring span {
  color: #64748b;
  font-size: 18px;
}

.action-stack {
  display: grid;
  gap: 10px;
  margin-top: 18px;
}

@media (max-width: 980px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .action-panel {
    position: static;
  }
}
</style>
