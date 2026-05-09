<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">活动大厅</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="toolbar">
      <el-input v-model="filters.keyword" placeholder="搜索活动" style="width: 220px" clearable />
      <el-select v-model="filters.campusId" placeholder="校区" style="width: 160px" clearable>
        <el-option v-for="item in campuses" :key="item.id" :label="item.campusName" :value="item.id" />
      </el-select>
      <el-select v-model="filters.categoryId" placeholder="分类" style="width: 160px" clearable>
        <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="load">筛选</el-button>
    </div>
    <div class="activity-grid">
      <el-card v-for="item in activities" :key="item.id" class="activity-card">
        <template #header>
          <div class="status-row">
            <strong>{{ item.title }}</strong>
            <el-tag :type="statusTag(item.status)">{{ ActivityStatusText[item.status] }}</el-tag>
          </div>
        </template>
        <p class="muted">{{ item.startTime }} - {{ item.endTime }}</p>
        <p>{{ item.campusName }} · {{ item.venueName }} {{ item.roomNumber }}</p>
        <p>名额：{{ item.currentEnrollment }} / {{ item.capacityLimit }}</p>
        <el-button type="primary" @click="$router.push(`/student/activities/${item.id}`)">查看详情</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getActivities } from '../../api/activity'
import { getCampuses, getCategories } from '../../api/dictionary'
import { ActivityStatusText, statusTag } from '../../utils/enums'

const filters = reactive({ keyword: '', campusId: undefined, categoryId: undefined })
const activities = ref<any[]>([])
const campuses = ref<any[]>([])
const categories = ref<any[]>([])

async function load() {
  const data = await getActivities({ ...filters, page: 1, size: 50 }) as any
  activities.value = data.list
}

onMounted(async () => {
  campuses.value = await getCampuses() as any[]
  categories.value = await getCategories() as any[]
  await load()
})
</script>
