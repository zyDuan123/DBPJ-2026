<template>
  <div class="page" v-if="activity">
    <div class="page-header">
      <h1 class="page-title">{{ activity.title }}</h1>
      <el-tag :type="statusTag(activity.status)">{{ ActivityStatusText[activity.status] }}</el-tag>
    </div>
    <div class="panel">
      <p class="muted">{{ activity.startTime }} - {{ activity.endTime }}</p>
      <p>{{ activity.campusName }} · {{ activity.venueName }} {{ activity.roomNumber }}</p>
      <p>报名截止：{{ activity.enrollDeadline }}</p>
      <p>名额：{{ activity.currentEnrollment }} / {{ activity.capacityLimit }}</p>
      <p>{{ activity.description }}</p>
      <div class="toolbar">
        <el-button v-if="!activity.registrationStatus || activity.registrationStatus === 'CANCELLED'" type="primary" :loading="loading" @click="handleEnroll">立即报名</el-button>
        <el-button v-if="['ENROLLED', 'WAITLISTED'].includes(activity.registrationStatus)" type="danger" :loading="loading" @click="handleCancel">取消报名</el-button>
        <el-button v-if="activity.registrationStatus === 'ENROLLED'" @click="showCode">查看签到码</el-button>
        <el-tag v-if="activity.registrationStatus" :type="statusTag(activity.registrationStatus)">{{ RegistrationStatusText[activity.registrationStatus] }}</el-tag>
      </div>
    </div>
    <el-dialog v-model="codeVisible" title="签到码" width="520px">
      <el-input v-model="checkInCode" type="textarea" :rows="5" readonly />
      <p class="muted">现场交给组织者扫描或复制核销。</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
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
