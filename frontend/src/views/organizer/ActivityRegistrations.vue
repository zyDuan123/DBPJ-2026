<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">报名名单</h1>
        <p class="page-subtitle">集中处理候补队列、现场签到核销和名单核对。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="metric-grid" style="margin-bottom: 16px">
      <div class="metric"><span>名单人数</span><strong>{{ records.length }}</strong></div>
      <div class="metric"><span>正选</span><strong>{{ countByStatus('ENROLLED') }}</strong></div>
      <div class="metric"><span>候补</span><strong>{{ countByStatus('WAITLISTED') }}</strong></div>
      <div class="metric"><span>已签到</span><strong>{{ countByStatus('CHECKED_IN') }}</strong></div>
    </div>
    <div class="action-band">
      <el-select v-model="status" placeholder="状态" clearable @change="load">
        <el-option label="正选" value="ENROLLED" />
        <el-option label="候补" value="WAITLISTED" />
        <el-option label="已签到" value="CHECKED_IN" />
      </el-select>
      <el-input v-model="checkInCode" placeholder="粘贴签到码" style="width: 360px" />
      <el-button type="primary" @click="handleCheckIn">核销签到</el-button>
    </div>
    <div class="page-split">
      <div class="panel">
        <div class="table-title">
          <h2 class="section-title">报名记录</h2>
          <span class="muted">按状态筛选后可直接核销签到</span>
        </div>
        <el-table :data="records" stripe>
          <el-table-column prop="studentName" label="姓名" min-width="120" />
          <el-table-column prop="studentNo" label="学号" min-width="130" />
          <el-table-column prop="phone" label="电话" min-width="130" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.registrationStatus)">{{ RegistrationStatusText[row.registrationStatus] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="queueNo" label="候补序号" width="100" />
          <el-table-column prop="checkInTime" label="签到时间" min-width="180" />
        </el-table>
      </div>
      <div class="soft-panel">
        <h2 class="section-title">二期管理扩展</h2>
        <div class="timeline-list">
          <div class="timeline-item">
            <strong>名单导出</strong>
            <span class="muted">后续支持导出 Excel 作为线下签到备份。</span>
          </div>
          <div class="timeline-item">
            <strong>缺勤标记</strong>
            <span class="muted">活动结束后可批量处理未签到记录。</span>
          </div>
          <div class="timeline-item">
            <strong>反馈看板</strong>
            <span class="muted">汇总评分、建议和活动复盘数据。</span>
          </div>
        </div>
      </div>
    </div>
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

function countByStatus(value: string) {
  return records.value.filter((item) => item.registrationStatus === value).length
}

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
