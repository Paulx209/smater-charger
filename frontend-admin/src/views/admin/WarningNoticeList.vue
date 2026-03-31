<template>
  <div class="warning-notice-list-container">
    <el-card class="list-card" v-loading="warningNoticeStore.loading">
      <template #header>
        <div class="card-header">
          <span>预警通知管理</span>
          <div class="header-actions">
            <el-button @click="handleGoSettings">阈值设置</el-button>
            <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleBatchRead">
              批量标记已读
            </el-button>
          </div>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="通知类型">
            <el-select v-model="filterForm.type" clearable placeholder="请选择通知类型" style="width: 160px">
              <el-option
                v-for="(text, type) in WarningNoticeTypeText"
                :key="type"
                :label="text"
                :value="type"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="读取状态">
            <el-select v-model="filterForm.isRead" clearable placeholder="请选择读取状态" style="width: 160px">
              <el-option label="未读" :value="0" />
              <el-option label="已读" :value="1" />
            </el-select>
          </el-form-item>

          <el-form-item label="用户 ID">
            <el-input-number
              v-model="filterForm.userId"
              :min="1"
              :controls="false"
              placeholder="请输入用户 ID"
            />
          </el-form-item>

          <el-form-item label="创建时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="warningNoticeStore.notices"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="通知 ID" width="100" />
        <el-table-column label="用户" min-width="170">
          <template #default="{ row }">
            <div>{{ getWarningNoticeUserLabel(row) }}</div>
            <div class="sub-text">用户 #{{ row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="通知类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getWarningNoticeTypeTagType(row.type)">
              {{ getWarningNoticeTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="读取状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getReadStatusTagType(row.isRead)">
              {{ formatReadStatus(row.isRead) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getSendStatusTagType(row.sendStatus)">
              {{ getSendStatusText(row.sendStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联充电桩" min-width="180">
          <template #default="{ row }">
            <div>{{ row.pileName || '-' }}</div>
            <div class="sub-text">{{ row.pileLocation || '无位置信息' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="通知内容" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row.id)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="warningNoticeStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="warningNoticeStore.currentPage"
          v-model:page-size="warningNoticeStore.pageSize"
          :total="warningNoticeStore.total"
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useWarningNoticeStore } from '@/stores/warningNotice'
import {
  WarningNoticeType,
  WarningNoticeTypeText,
  formatDateTime,
  formatReadStatus,
  getReadStatusTagType,
  getSendStatusTagType,
  getSendStatusText,
  getWarningNoticeTypeTagType,
  getWarningNoticeTypeText,
  getWarningNoticeUserLabel,
  type WarningNoticeInfo,
  type WarningNoticeQueryParams
} from '@/types/warningNotice'

const router = useRouter()
const warningNoticeStore = useWarningNoticeStore()
const selectedIds = ref<number[]>([])

const filterForm = reactive({
  type: undefined as WarningNoticeType | undefined,
  isRead: undefined as number | undefined,
  userId: undefined as number | undefined,
  dateRange: null as [string, string] | null
})

const buildQueryParams = (): Omit<WarningNoticeQueryParams, 'page' | 'size'> => ({
  type: filterForm.type,
  isRead: filterForm.isRead,
  userId: filterForm.userId,
  startDate: filterForm.dateRange?.[0],
  endDate: filterForm.dateRange?.[1]
})

const reloadList = async () => {
  await warningNoticeStore.fetchWarningNoticeList(buildQueryParams())
}

const handleSelectionChange = (rows: WarningNoticeInfo[]) => {
  selectedIds.value = rows.map((item) => item.id)
}

const handleFilter = async () => {
  warningNoticeStore.currentPage = 1
  await reloadList()
}

const handleReset = async () => {
  filterForm.type = undefined
  filterForm.isRead = undefined
  filterForm.userId = undefined
  filterForm.dateRange = null
  selectedIds.value = []
  warningNoticeStore.reset()
  await warningNoticeStore.fetchWarningNoticeList({})
}

const handleSizeChange = async (size: number) => {
  warningNoticeStore.pageSize = size
  await reloadList()
}

const handlePageChange = async (page: number) => {
  warningNoticeStore.currentPage = page
  await reloadList()
}

const handleView = (id: number) => {
  router.push(`/warning-notices/${id}`)
}

const handleGoSettings = () => {
  router.push('/warning-notices/settings')
}

const handleBatchRead = async () => {
  await warningNoticeStore.batchMarkAsRead(selectedIds.value)
  selectedIds.value = []
  await reloadList()
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('删除后不可恢复，确认继续吗？', '删除预警通知', {
    type: 'warning'
  })
  await warningNoticeStore.removeWarningNotice(id)
  selectedIds.value = selectedIds.value.filter((item) => item !== id)
  await reloadList()
}

onMounted(async () => {
  await warningNoticeStore.fetchWarningNoticeList({})
})
</script>

<style scoped>
.warning-notice-list-container {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
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

.sub-text {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>