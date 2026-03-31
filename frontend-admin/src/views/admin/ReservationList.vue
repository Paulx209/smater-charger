<template>
  <div class="reservation-list-container">
    <el-card class="list-card" v-loading="reservationStore.loading">
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 160px">
              <el-option
                v-for="(text, status) in ReservationStatusText"
                :key="status"
                :label="text"
                :value="status"
              />
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

          <el-form-item label="充电桩 ID">
            <el-input-number
              v-model="filterForm.chargingPileId"
              :min="1"
              :controls="false"
              placeholder="请输入充电桩 ID"
            />
          </el-form-item>

          <el-form-item label="预约时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              range-separator="至"
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

      <el-table :data="reservationStore.reservations" stripe style="width: 100%">
        <el-table-column prop="id" label="预约 ID" width="100" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div>{{ getReservationUserLabel(row) }}</div>
            <div class="sub-text">用户 #{{ row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="充电桩" min-width="220">
          <template #default="{ row }">
            <div>{{ row.chargingPileCode || `充电桩 #${row.chargingPileId}` }}</div>
            <div class="sub-text">{{ row.chargingPileLocation || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getReservationStatusTagType(row.status)">
              {{ getReservationStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="剩余时间" width="140">
          <template #default="{ row }">{{ formatRemainingMinutes(row.remainingMinutes) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row.id)">详情</el-button>
            <el-button
              v-if="row.status === ReservationStatus.PENDING"
              type="danger"
              link
              @click="handleCancel(row.id)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="reservationStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="reservationStore.currentPage"
          v-model:page-size="reservationStore.pageSize"
          :total="reservationStore.total"
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
import { useReservationStore } from '@/stores/reservation'
import {
  ReservationStatus,
  ReservationStatusText,
  formatDateTime,
  formatRemainingMinutes,
  getReservationStatusTagType,
  getReservationStatusText,
  getReservationUserLabel,
  type ReservationQueryParams
} from '@/types/reservation'

const router = useRouter()
const reservationStore = useReservationStore()

const filterForm = reactive({
  userId: undefined as number | undefined,
  chargingPileId: undefined as number | undefined,
  status: undefined as ReservationStatus | undefined,
  dateRange: null as [string, string] | null
})

const buildQueryParams = (): Omit<ReservationQueryParams, 'page' | 'size'> => ({
  userId: filterForm.userId,
  chargingPileId: filterForm.chargingPileId,
  status: filterForm.status,
  startDate: filterForm.dateRange?.[0],
  endDate: filterForm.dateRange?.[1]
})

const reloadList = async () => {
  await reservationStore.fetchReservationList(buildQueryParams())
}

const handleFilter = async () => {
  reservationStore.currentPage = 1
  await reloadList()
}

const handleReset = async () => {
  filterForm.userId = undefined
  filterForm.chargingPileId = undefined
  filterForm.status = undefined
  filterForm.dateRange = null
  reservationStore.reset()
  await reservationStore.fetchReservationList({})
}

const handleSizeChange = async (size: number) => {
  reservationStore.pageSize = size
  await reloadList()
}

const handlePageChange = async (page: number) => {
  reservationStore.currentPage = page
  await reloadList()
}

const handleView = (id: number) => {
  router.push(`/reservations/${id}`)
}

const handleCancel = async (id: number) => {
  await ElMessageBox.confirm('确定要取消这条预约吗？', '取消预约', {
    type: 'warning'
  })
  await reservationStore.cancelReservation(id)
  await reloadList()
}

onMounted(async () => {
  await reservationStore.fetchReservationList({})
})
</script>

<style scoped>
.reservation-list-container {
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