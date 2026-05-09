<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">报名名单</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="toolbar">
      <el-select v-model="status" placeholder="状态" clearable @change="load">
        <el-option label="正选" value="ENROLLED" />
        <el-option label="候补" value="WAITLISTED" />
        <el-option label="已签到" value="CHECKED_IN" />
      </el-select>
      <el-input v-model="checkInCode" placeholder="粘贴签到码" style="width: 360px" />
      <el-button type="primary" @click="handleCheckIn">核销签到</el-button>
    </div>
    <el-table :data="records" class="panel">
      <el-table-column prop="studentName" label="姓名" />
      <el-table-column prop="studentNo" label="学号" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.registrationStatus)">{{ RegistrationStatusText[row.registrationStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="queueNo" label="候补序号" />
      <el-table-column prop="checkInTime" label="签到时间" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { checkIn, getActivityRegistrations } from '../../api/registration'
import { RegistrationStatusText, statusTag } from '../../utils/enums'

const route = useRoute()
const status = ref('')
const records = ref<any[]>([])
const checkInCode = ref('')

async function load() {
  const data = await getActivityRegistrations(route.params.id as string, { status: status.value || undefined, page: 1, size: 100 }) as any
  records.value = data.list
}

async function handleCheckIn() {
  await checkIn(checkInCode.value)
  ElMessage.success('签到成功')
  checkInCode.value = ''
  await load()
}

onMounted(load)
</script>
