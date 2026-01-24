<template>
  <div class="reservation-list-container">
    <el-card class="list-card" v-loading="reservationStore.loading">
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建预约
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
              @change="handleFilter"
            >
              <el-option label="待使用" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-form-item>

          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleFilter"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 预约列表 -->
      <div v-if="reservationStore.reservations.length === 0" class="empty-state">
        <el-empty description="暂无预约记录">
          <el-button type="primary" @click="handleCreate">创建预约</el-button>
        </el-empty>
      </div>

      <div v-else class="reservation-list">
        <el-card
          v-for="reservation in reservationStore.reservations"
          :key="reservation.id"
          class="reservation-card"
          shadow="hover"
        >
          <div class="reservation-header">
            <div class="reservation-title">
              <el-icon><Location /></el-icon>
              <span class="pile-name">{{ reservation.chargingPileCode || `充电桩 #${reservation.chargingPileId}` }}</span>
              <el-tag v-if="reservation.chargingPileTypeDesc" type="info" size="small" style="margin-left: 8px">
                {{ reservation.chargingPileTypeDesc }}
              </el-tag>
            </div>
            <el-tag
              :type="getStatusColor(reservation.status)"
              size="large"
            >
              {{ getStatusText(reservation.status) }}
            </el-tag>
          </div>

          <div class="reservation-info">
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span class="info-label">预约时间：</span>
              <span class="info-text">{{ formatTimeRange(reservation.startTime, reservation.endTime) }}</span>
            </div>

            <div class="info-item">
              <el-icon><Timer /></el-icon>
              <span class="info-label">预约时长：</span>
              <span class="info-text">{{ calculateDuration(reservation.startTime, reservation.endTime) }} 小时</span>
            </div>

            <div v-if="reservation.chargingPileLocation" class="info-item">
              <el-icon><MapLocation /></el-icon>
              <span class="info-label">充电桩位置：</span>
              <span class="info-text">{{ reservation.chargingPileLocation }}</span>
            </div>

            <div v-if="reservation.chargingPilePower" class="info-item">
              <el-icon><Lightning /></el-icon>
              <span class="info-label">充电功率：</span>
              <span class="info-text">{{ reservation.chargingPilePower }} kW</span>
            </div>

            <div v-if="reservation.remainingMinutes !== undefined && reservation.remainingMinutes !== null" class="info-item">
              <el-icon><Timer /></el-icon>
              <span class="info-label">剩余时间：</span>
              <span class="info-text" :class="{ 'text-warning': reservation.remainingMinutes < 30 }">
                {{ formatRemainingTime(reservation.remainingMinutes) }}
              </span>
            </div>
          </div>

          <div class="reservation-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(reservation.id)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="canCancel(reservation)"
              type="danger"
              size="small"
              @click="handleCancel(reservation.id)"
            >
              取消预约
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Plus,
  Location,
  Clock,
  Timer,
  MapLocation,
  Lightning
} from '@element-plus/icons-vue'
import { useReservationStore } from '@/stores/reservation'
import {
  ReservationStatusText,
  ReservationStatusColor,
  formatReservationTimeRange,
  calculateReservationDuration,
  canCancelReservation,
  type ReservationInfo,
  type ReservationStatus
} from '@/types/reservation'

const router = useRouter()
const reservationStore = useReservationStore()

// 筛选表单
const filterForm = reactive({
  status: undefined as ReservationStatus | undefined
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 获取状态文本
const getStatusText = (status: ReservationStatus) => {
  return ReservationStatusText[status]
}

// 获取状态颜色
const getStatusColor = (status: ReservationStatus) => {
  return ReservationStatusColor[status]
}

// 格式化时间范围
const formatTimeRange = (startTime: string, endTime: string) => {
  return formatReservationTimeRange(startTime, endTime)
}

// 计算时长
const calculateDuration = (startTime: string, endTime: string) => {
  return calculateReservationDuration(startTime, endTime)
}

// 判断是否可以取消
const canCancel = (reservation: ReservationInfo) => {
  return canCancelReservation(reservation)
}

// 格式化剩余时间
const formatRemainingTime = (minutes: number) => {
  if (minutes < 0) return '已过期'
  if (minutes < 60) return `${minutes} 分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours} 小时 ${mins} 分钟` : `${hours} 小时`
}

// 创建预约
const handleCreate = () => {
  router.push('/reservations/create')
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/reservations/${id}`)
}

// 取消预约
const handleCancel = async (id: number) => {
  try {
    const { value: cancelReason } = await ElMessageBox.prompt(
      '请输入取消原因（可选）',
      '取消预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入取消原因'
      }
    )

    await reservationStore.cancelReservationById(id, {
      cancelReason: cancelReason || undefined
    })
  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') {
      return
    }
    console.error('取消预约失败:', error)
  }
}

// 筛选
const handleFilter = async () => {
  try {
    await reservationStore.fetchMyReservations({
      status: filterForm.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
      // 不传递 page 参数，让 store 使用 currentPage
    })
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// 重置
const handleReset = async () => {
  filterForm.status = undefined
  dateRange.value = null
  reservationStore.currentPage = 1  // 重置到第一页
  await handleFilter()
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  reservationStore.pageSize = size
  await handleFilter()
}

// 页码改变
const handlePageChange = async (page: number) => {
  reservationStore.currentPage = page
  await handleFilter()
}

// 组件挂载时加载数据
onMounted(async () => {
  await reservationStore.fetchMyReservations()
})
</script>

<style scoped>
.reservation-list-container {
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

.empty-state {
  padding: 60px 0;
}

.reservation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.reservation-card {
  transition: all 0.3s;
}

.reservation-card:hover {
  transform: translateY(-4px);
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.reservation-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pile-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.reservation-info {
  margin-bottom: 16px;
  min-height: 120px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.info-item .el-icon {
  margin-right: 8px;
  margin-top: 2px;
  color: #909399;
  flex-shrink: 0;
}

.info-label {
  font-weight: 500;
  margin-right: 4px;
  flex-shrink: 0;
}

.info-text {
  flex: 1;
  word-break: break-word;
}

.text-warning {
  color: #e6a23c;
  font-weight: 600;
}

.cancel-reason {
  color: #f56c6c;
}

.reservation-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
