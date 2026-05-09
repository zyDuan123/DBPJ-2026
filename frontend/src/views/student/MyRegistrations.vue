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
    </div>
    <div class="page-split">
      <div class="panel">
        <el-tabs v-model="status" @tab-change="load">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane label="正选" name="ENROLLED" />
          <el-tab-pane label="候补" name="WAITLISTED" />
          <el-tab-pane label="已签到" name="CHECKED_IN" />
          <el-tab-pane label="已取消" name="CANCELLED" />
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
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="['ENROLLED', 'WAITLISTED'].includes(row.registrationStatus)" link type="danger" @click="cancel(row.registrationId)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="soft-panel">
        <h2 class="section-title">二期能力</h2>
        <div class="timeline-list">
          <div class="timeline-item">
            <strong>活动评价</strong>
            <span class="muted">已签到或已结束活动可填写反馈。</span>
          </div>
          <div class="timeline-item">
            <strong>信用分记录</strong>
            <span class="muted">缺勤、取消和签到行为进入个人信用明细。</span>
          </div>
          <div class="timeline-item">
            <strong>消息提醒</strong>
            <span class="muted">审核、候补转正、活动变更会统一提示。</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { cancelRegistration, getMyRegistrations } from '../../api/registration'
import { RegistrationStatusText, statusTag } from '../../utils/enums'

const status = ref('')
const records = ref<any[]>([])

function countByStatus(value: string) {
  return records.value.filter((item) => item.registrationStatus === value).length
}

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
