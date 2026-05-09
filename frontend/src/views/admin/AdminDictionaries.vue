<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">资源管理</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="panel">
          <h3>校区</h3>
          <el-input v-model="campusForm.campusName" placeholder="校区名" />
          <el-input v-model="campusForm.location" placeholder="位置" style="margin: 8px 0" />
          <el-button type="primary" @click="addCampus">新增校区</el-button>
          <el-table :data="campuses" style="margin-top: 12px">
            <el-table-column prop="campusName" label="校区" />
            <el-table-column prop="location" label="位置" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel">
          <h3>场地</h3>
          <el-select v-model="venueForm.campusId" placeholder="校区" style="width: 100%">
            <el-option v-for="item in campuses" :key="item.id" :label="item.campusName" :value="item.id" />
          </el-select>
          <el-input v-model="venueForm.venueName" placeholder="场馆名" style="margin-top: 8px" />
          <el-input v-model="venueForm.roomNumber" placeholder="房间号" style="margin-top: 8px" />
          <el-input-number v-model="venueForm.capacity" :min="1" style="margin: 8px 0" />
          <el-button type="primary" @click="addVenue">新增场地</el-button>
          <el-table :data="venues" style="margin-top: 12px">
            <el-table-column prop="venueName" label="场馆" />
            <el-table-column prop="roomNumber" label="房间" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel">
          <h3>分类</h3>
          <el-input v-model="categoryName" placeholder="分类名" />
          <el-button type="primary" style="margin: 8px 0" @click="addCategory">新增分类</el-button>
          <el-table :data="categories">
            <el-table-column prop="categoryName" label="分类" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { createCampus, createCategory, createVenue, getCampuses, getCategories, getVenues } from '../../api/dictionary'

const campuses = ref<any[]>([])
const venues = ref<any[]>([])
const categories = ref<any[]>([])
const campusForm = reactive({ campusName: '', location: '' })
const venueForm = reactive({ campusId: undefined as number | undefined, venueName: '', roomNumber: '', capacity: 30 })
const categoryName = ref('')

async function load() {
  campuses.value = await getCampuses() as any[]
  venues.value = await getVenues() as any[]
  categories.value = await getCategories() as any[]
  if (!venueForm.campusId && campuses.value[0]) venueForm.campusId = campuses.value[0].id
}

async function addCampus() {
  await createCampus(campusForm)
  campusForm.campusName = ''
  campusForm.location = ''
  await load()
}

async function addVenue() {
  await createVenue(venueForm)
  venueForm.venueName = ''
  venueForm.roomNumber = ''
  await load()
}

async function addCategory() {
  await createCategory({ categoryName: categoryName.value })
  categoryName.value = ''
  await load()
}

onMounted(load)
</script>
