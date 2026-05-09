<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">{{ id ? '编辑活动' : '创建活动' }}</h1>
    </div>
    <el-form class="panel" label-width="110px">
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="校区">
        <el-select v-model="campusId" @change="loadVenues">
          <el-option v-for="item in campuses" :key="item.id" :label="item.campusName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="场地">
        <el-select v-model="form.venueId">
          <el-option v-for="item in venues" :key="item.id" :label="`${item.venueName} ${item.roomNumber}`" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId">
          <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item label="报名截止"><el-date-picker v-model="form.enrollDeadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item label="人数上限"><el-input-number v-model="form.capacityLimit" :min="1" /></el-form-item>
      <el-form-item label="海报地址"><el-input v-model="form.posterUrl" /></el-form-item>
      <el-form-item label="详情"><el-input v-model="form.description" type="textarea" :rows="5" /></el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="save">保存草稿</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createActivity, getActivity, updateActivity } from '../../api/activity'
import { getCampuses, getCategories, getVenues } from '../../api/dictionary'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string | undefined
const loading = ref(false)
const campusId = ref<number>()
const campuses = ref<any[]>([])
const venues = ref<any[]>([])
const categories = ref<any[]>([])
const form = reactive<any>({
  title: '',
  venueId: undefined,
  categoryId: undefined,
  startTime: '',
  endTime: '',
  enrollDeadline: '',
  capacityLimit: 30,
  posterUrl: '',
  description: '',
})

async function loadVenues() {
  venues.value = await getVenues({ campusId: campusId.value }) as any[]
}

async function save() {
  loading.value = true
  try {
    if (id) await updateActivity(id, form)
    else await createActivity(form)
    router.push('/organizer/activities')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  campuses.value = await getCampuses() as any[]
  categories.value = await getCategories() as any[]
  if (campuses.value[0]) campusId.value = campuses.value[0].id
  await loadVenues()
  if (id) {
    const detail = await getActivity(id) as any
    Object.assign(form, {
      title: detail.title,
      venueId: detail.venueId || form.venueId,
      categoryId: detail.categoryId || form.categoryId,
      startTime: detail.startTime?.replace(' ', 'T'),
      endTime: detail.endTime?.replace(' ', 'T'),
      enrollDeadline: detail.enrollDeadline?.replace(' ', 'T'),
      capacityLimit: detail.capacityLimit,
      posterUrl: detail.posterUrl,
      description: detail.description,
    })
  }
})
</script>
