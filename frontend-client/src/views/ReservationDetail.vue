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
                {{ reservation.chargingPileType === 'AC' ? 'AC（交流慢充）' : 'DC（直流快充）' }}
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
            v-if="canStartCharging(reservation)"
            type="success"
            size="large"
            @click="handleStartCharging"
            :loading="startingCharging"
          >
            <el-icon><Lightning /></el-icon>
            开始充电
          </el-button>

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
import { useChargingRecordStore } from '@/stores/chargingRecord'
import { useVehicleStore } from '@/stores/vehicle'
import { usePriceConfigStore } from '@/stores/priceConfig'
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
const chargingRecordStore = useChargingRecordStore()
const vehicleStore = useVehicleStore()
const priceConfigStore = usePriceConfigStore()

// 开始充电状态
const startingCharging = ref(false)

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

// 判断是否可以开始充电
const canStartCharging = (reservation: any) => {
  if (!reservation) return false

  // 只有已确认的预约才能开始充电
  if (reservation.status !== 'CONFIRMED') return false

  // 检查是否在预约时间范围内（允许提前15分钟）
  const now = new Date()
  const startTime = new Date(reservation.startTime)
  const endTime = new Date(reservation.endTime)

  // 提前15分钟可以开始充电
  const allowStartTime = new Date(startTime.getTime() - 15 * 60 * 1000)

  return now >= allowStartTime && now <= endTime
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

// 开始充电
const handleStartCharging = async () => {
  if (!reservation.value) return

  try {
    // 检查是否已有正在进行的充电记录
    const currentRecord = await chargingRecordStore.fetchCurrentChargingRecord()
    if (currentRecord) {
      ElMessage.warning('您已有正在进行的充电，请先结束当前充电')
      return
    }

    // 获取用户的车辆列表
    await vehicleStore.fetchMyVehicles()
    const vehicles = vehicleStore.vehicles

    // 检查是否有车辆
    if (vehicles.length === 0) {
      await ElMessageBox.confirm(
        '您还没有绑定车辆，请先添加车辆后再进行充电',
        '提示',
        {
          confirmButtonText: '去添加车辆',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      router.push('/vehicles/add')
      return
    }

    // 让用户选择车辆
    const { value: selectedVehicleId } = await ElMessageBox.prompt(
      '请选择要充电的车辆',
      '选择车辆',
      {
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputType: 'select',
        inputOptions: vehicles.map(v => ({
          label: `${v.licensePlate} (${v.brand} ${v.model})`,
          value: v.id
        })),
        inputValidator: (value) => {
          if (!value) {
            return '请选择车辆'
          }
          return true
        }
      }
    )

    // 让用户输入预计充电量
    const { value: electricQuantity } = await ElMessageBox.prompt(
      '请输入预计充电量（度）',
      '充电量预估',
      {
        confirmButtonText: '查看预估费用',
        cancelButtonText: '取消',
        inputType: 'number',
        inputPlaceholder: '请输入预计充电量',
        inputValidator: (value) => {
          const num = Number(value)
          if (!value || num <= 0) {
            return '充电量必须大于0'
          }
          if (num > 999) {
            return '充电量不能超过999度'
          }
          return true
        }
      }
    )

    // 获取充电桩类型（从预约信息中获取，如果没有则默认为AC）
    const pileType = reservation.value.chargingPileType || 'AC'

    // 调用费用预估接口
    const estimate = await priceConfigStore.estimateFee({
      chargingPileType: pileType,
      electricQuantity: Number(electricQuantity)
    })

    // 显示费用预估并确认
    const confirmMessage = `
      <div style="text-align: left; padding: 10px;">
        <h3 style="margin-bottom: 15px;">费用预估</h3>
        <p><strong>充电桩：</strong>${reservation.value.chargingPileCode || `#${reservation.value.chargingPileId}`}</p>
        <p><strong>充电桩类型：</strong>${pileType === 'AC' ? 'AC（交流慢充）' : 'DC（直流快充）'}</p>
        <p><strong>预计充电量：</strong>${estimate.electricQuantity.toFixed(2)} 度</p>
        <p><strong>每度电价格：</strong>¥${estimate.pricePerKwh.toFixed(2)}/度</p>
        <p><strong>服务费：</strong>¥${estimate.serviceFee.toFixed(2)}/度</p>
        <hr style="margin: 10px 0;">
        <p><strong>电费：</strong>¥${estimate.breakdown.electricityFee.toFixed(2)}</p>
        <p><strong>服务费：</strong>¥${estimate.breakdown.serviceFee.toFixed(2)}</p>
        <p style="font-size: 18px; color: #f56c6c; margin-top: 10px;">
          <strong>预估总费用：¥${estimate.totalPrice.toFixed(2)}</strong>
        </p>
        <p style="font-size: 12px; color: #909399; margin-top: 10px;">
          * 以上为预估费用，实际费用以充电结束后的结算为准
        </p>
      </div>
    `

    await ElMessageBox.confirm(confirmMessage, '确认开始充电', {
      confirmButtonText: '确认充电',
      cancelButtonText: '取消',
      type: 'info',
      dangerouslyUseHTMLString: true
    })

    startingCharging.value = true

    // 调用开始充电接口
    const record = await chargingRecordStore.beginCharging({
      chargingPileId: reservation.value.chargingPileId,
      vehicleId: Number(selectedVehicleId)
    })

    ElMessage.success('开始充电成功')

    // 跳转到充电记录详情页
    router.push(`/charging-record/${record.id}`)
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消操作
      return
    }
    console.error('开始充电失败:', error)
  } finally {
    startingCharging.value = false
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
