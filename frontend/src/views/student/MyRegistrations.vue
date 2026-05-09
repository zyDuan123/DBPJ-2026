<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">我的活动</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-tabs v-model="status" @tab-change="load">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="正选" name="ENROLLED" />
      <el-tab-pane label="候补" name="WAITLISTED" />
      <el-tab-pane label="已签到" name="CHECKED_IN" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>
    <el-table :data="records" class="panel">
      <el-table-column prop="title" label="活动" />
      <el-table-column prop="startTime" label="时间" width="180" />
      <el-table-column label="地点">
        <template #default="{ row }">{{ row.campusName }} {{ row.venueName }} {{ row.roomNumber }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.registrationStatus)">{{ RegistrationStatusText[row.registrationStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="queueNo" label="候补序号" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="['ENROLLED', 'WAITLISTED'].includes(row.registrationStatus)" link type="danger" @click="cancel(row.registrationId)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { cancelRegistration, getMyRegistrations } from '../../api/registration'
import { RegistrationStatusText, statusTag } from '../../utils/enums'

const status = ref('')
const records = ref<any[]>([])

async function load() {
  const data = await getMyRegistrations({ status: status.value || undefined, page: 1, size: 50 }) as any
  records.value = data.list
}

async function cancel(id: number) {
  await cancelRegistration(id)
  await load()
}

onMounted(load)
</script>
