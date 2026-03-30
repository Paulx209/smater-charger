<template>
  <div class="announcement-list-container">
    <el-card class="list-card" v-loading="announcementStore.loading">
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建公告
          </el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 150px" @change="handleFilter">
              <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索标题"
              clearable
              style="width: 220px"
              @keyup.enter="handleFilter"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="announcementStore.adminAnnouncements" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="生效时间范围" width="320">
          <template #default="{ row }">{{ formatTimeRange(row.startTime, row.endTime) }}</template>
        </el-table-column>

        <el-table-column prop="adminName" label="发布人" width="120" />

        <el-table-column prop="createdTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row.id)">编辑</el-button>
            <el-button v-if="row.status === AnnouncementStatus.DRAFT" type="success" size="small" @click="handlePublish(row.id)">发布</el-button>
            <el-button v-if="row.status === AnnouncementStatus.PUBLISHED" type="warning" size="small" @click="handleUnpublish(row.id)">下线</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="announcementStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="announcementStore.currentPage"
          v-model:page-size="announcementStore.pageSize"
          :total="announcementStore.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAnnouncementStore } from '@/stores/announcement'
import {
  AnnouncementStatus,
  AnnouncementStatusColor,
  AnnouncementStatusText,
  formatTimeRange,
  type AnnouncementInfo
} from '@/types/announcement'

const router = useRouter()
const announcementStore = useAnnouncementStore()

const statusOptions = Object.values(AnnouncementStatus).map((status) => ({
  value: status,
  label: AnnouncementStatusText[status]
}))

const filterForm = reactive({
  status: undefined as AnnouncementStatus | undefined,
  keyword: undefined as string | undefined
})

const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusColor = (status: AnnouncementInfo['status']) => AnnouncementStatusColor[status]
const getStatusText = (status: AnnouncementInfo['status']) => AnnouncementStatusText[status]

const handleCreate = () => {
  router.push('/admin/announcement/create')
}

const handleEdit = (id: number) => {
  router.push(`/admin/announcement/edit/${id}`)
}

const handlePublish = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认发布该公告后，用户端将可以看到这条公告。', '发布确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await announcementStore.publishAnnouncementById(id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布公告失败:', error)
    }
  }
}

const handleUnpublish = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认下线该公告后，用户端将不再展示这条公告。', '下线确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await announcementStore.unpublishAnnouncementById(id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下线公告失败:', error)
    }
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该公告后，将无法恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await announcementStore.removeAnnouncement(id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
    }
  }
}

const handleFilter = async () => {
  await announcementStore.fetchAdminAnnouncementList({
    status: filterForm.status,
    keyword: filterForm.keyword,
    page: 1,
    size: announcementStore.pageSize
  })
}

const handleReset = async () => {
  filterForm.status = undefined
  filterForm.keyword = undefined
  await handleFilter()
}

const handleSizeChange = async (size: number) => {
  announcementStore.pageSize = size
  await announcementStore.fetchAdminAnnouncementList({
    status: filterForm.status,
    keyword: filterForm.keyword,
    page: 1,
    size
  })
}

const handlePageChange = async (page: number) => {
  announcementStore.currentPage = page
  await announcementStore.fetchAdminAnnouncementList({
    status: filterForm.status,
    keyword: filterForm.keyword,
    page,
    size: announcementStore.pageSize
  })
}

onMounted(async () => {
  await announcementStore.fetchAdminAnnouncementList()
})
</script>

<style scoped>
.announcement-list-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.filter-form {
  margin-bottom: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>