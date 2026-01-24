<template>
  <div class="reservation-detail-container">
    <el-card class="detail-card" v-loading="reservationStore.loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span>预约详情</span>
          <div></div>
        </div>
      </template>

      <div v-if="reservation" class="detail-content">
        <!-- 状态标签 -->
        <div class="status-section">
          <el-tag
            :type="getStatusColor(reservation.status)"
            size="large"
            class="status-tag"
          >
            {{ getStatusText(reservation.status) }}
          </el-tag>
        </div>

        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="1" border class="info-section">
          <el-descriptions-item label="预约编号">
            {{ reservation.id }}
          </el-descriptions-item>

          <el-descriptions-item label="充电桩编号">
            <div class="pile-info">
              <el-icon><Location /></el-icon>
              <span>{{ reservation.chargingPileCode || `#${reservation.chargingPileId}` }}</span>
              <el-tag v-if="reservation.chargingPileTypeDesc" type="info" size="small" style="margin-left: 8px">
                {{ reservation.chargingPileTypeDesc }}
              </el-tag>
            </div>
          </el-descriptions-item>

          <el-descriptions-item v-if="reservation.chargingPileLocation" label="充电桩位置">
            <div class="location-info">
              <el-icon><MapLocation /></el-icon>
              <span>{{ reservation.chargingPileLocation }}</span>
              <el-button
                type="text"
                size="small"
                @click="handleViewPile"
                style="margin-left: 8px"
              >
                查看充电桩详情
              </el-button>
            </div>
          </el-descriptions-item>

          <el-descriptions-item v-if="reservation.chargingPilePower" label="充电功率">
            <div class="power-info">
              <el-icon><Lightning /></el-icon>
              <span>{{ reservation.chargingPilePower }} kW</span>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="预约时间">
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTimeRange(reservation.startTime, reservation.endTime) }}</span>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="预约时长">
            <div class="duration-info">
              <el-icon><Timer /></el-icon>
              <span>{{ calculateDuration(reservation.startTime, reservation.endTime) }} 小时</span>
            </div>
          </el-descriptions-item>

          <el-descriptions-item v-if="reservation.remainingMinutes !== undefined && reservation.remainingMinutes !== null" label="剩余时间">
            <div class="remaining-info" :class="{ 'text-warning': reservation.remainingMinutes < 30 }">
              <el-icon><Timer /></el-icon>
              <span>{{ formatRemainingTime(reservation.remainingMinutes) }}</span>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="创建时间">
            {{ formatDateTime(reservation.createdTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="action-section">
          <el-button
            v-if="canCancel(reservation)"
            type="danger"
            size="large"
            @click="handleCancel"
          >
            取消预约
          </el-button>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="预约不存在或已被删除">
          <el-button type="primary" @click="handleBack">返回列表</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Location,
  MapLocation,
  Clock,
  Timer,
  Lightning
} from '@element-plus/icons-vue'
import { useReservationStore } from '@/stores/reservation'
import {
  ReservationStatusText,
  ReservationStatusColor,
  formatReservationTimeRange,
  calculateReservationDuration,
  canCancelReservation,
  type ReservationStatus
} from '@/types/reservation'

const router = useRouter()
const route = useRoute()
const reservationStore = useReservationStore()

// 预约ID
const reservationId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

// 当前预约
const reservation = computed(() => reservationStore.currentReservation)

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

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 判断是否可以取消
const canCancel = (reservation: any) => {
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

// 查看充电桩详情
const handleViewPile = () => {
  if (reservation.value?.chargingPileId) {
    router.push(`/charging-piles/${reservation.value.chargingPileId}`)
  }
}

// 返回
const handleBack = () => {
  router.push('/reservations')
}

// 取消预约
const handleCancel = async () => {
  if (!reservationId.value) return

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

    await reservationStore.cancelReservationById(reservationId.value, {
      cancelReason: cancelReason || undefined
    })

    // 返回列表页
    router.push('/reservations')
  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') {
      return
    }
    console.error('取消预约失败:', error)
  }
}

// 加载预约数据
const loadReservationData = async () => {
  if (!reservationId.value) {
    ElMessage.error('预约ID无效')
    router.push('/reservations')
    return
  }

  try {
    await reservationStore.fetchReservationById(reservationId.value)
  } catch (error) {
    console.error('加载预约数据失败:', error)
    ElMessage.error('加载预约数据失败')
    router.push('/reservations')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadReservationData()
})
</script>

<style scoped>
.reservation-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.detail-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.detail-content {
  padding: 20px 0;
}

.status-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.status-tag {
  font-size: 16px;
  padding: 12px 24px;
}

.info-section {
  margin-bottom: 30px;
}

.pile-info,
.location-info,
.time-info,
.duration-info,
.power-info,
.remaining-info,
.cancel-reason {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-warning {
  color: #e6a23c;
  font-weight: 600;
}

.license-plate {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  letter-spacing: 1px;
}

.cancel-reason {
  color: #f56c6c;
}

.action-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.empty-state {
  padding: 60px 0;
}
</style>
