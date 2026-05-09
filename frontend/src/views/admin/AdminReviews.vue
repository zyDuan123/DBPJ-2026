<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">审核中心</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="activities" class="panel">
      <el-table-column prop="title" label="活动" min-width="180" />
      <el-table-column prop="organizerName" label="组织者" width="140" />
      <el-table-column prop="startTime" label="时间" width="180" />
      <el-table-column label="地点">
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
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getActivities, reviewActivity } from '../../api/activity'

const activities = ref<any[]>([])

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
