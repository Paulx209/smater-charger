<template>
  <div class="reservation-detail-container">
    <el-card v-loading="reservationStore.loading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
            <span class="header-title">预约详情</span>
          </div>
          <el-button
            v-if="reservationStore.currentReservation?.status === ReservationStatus.PENDING"
            type="danger"
            @click="handleCancel"
          >
            取消预约
          </el-button>
        </div>
      </template>

      <div v-if="reservationStore.currentReservation" class="detail-content">
        <div class="section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预约 ID">#{{ reservationStore.currentReservation.id }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getReservationStatusTagType(reservationStore.currentReservation.status)">
                {{ getReservationStatusText(reservationStore.currentReservation.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用户">{{ getReservationUserLabel(reservationStore.currentReservation) }}</el-descriptions-item>
            <el-descriptions-item label="用户 ID">#{{ reservationStore.currentReservation.userId }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(reservationStore.currentReservation.createdTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(reservationStore.currentReservation.updatedTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">充电桩信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="充电桩 ID">#{{ reservationStore.currentReservation.chargingPileId }}</el-descriptions-item>
            <el-descriptions-item label="充电桩编号">
              {{ reservationStore.currentReservation.chargingPileCode || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="充电桩位置" :span="2">
              {{ reservationStore.currentReservation.chargingPileLocation || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="充电桩类型">
              {{ reservationStore.currentReservation.chargingPileTypeDesc || reservationStore.currentReservation.chargingPileType || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="功率">
              {{ reservationStore.currentReservation.chargingPilePower != null ? `${reservationStore.currentReservation.chargingPilePower} kW` : '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">预约时间</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="开始时间">{{ formatDateTime(reservationStore.currentReservation.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(reservationStore.currentReservation.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="状态说明">
              {{ reservationStore.currentReservation.statusDesc || getReservationStatusText(reservationStore.currentReservation.status) }}
            </el-descriptions-item>
            <el-descriptions-item label="剩余时间">
              {{ formatRemainingMinutes(reservationStore.currentReservation.remainingMinutes) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="未找到预约详情" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useReservationStore } from '@/stores/reservation'
import {
  ReservationStatus,
  formatDateTime,
  formatRemainingMinutes,
  getReservationStatusTagType,
  getReservationStatusText,
  getReservationUserLabel
} from '@/types/reservation'
import { navigateBack } from '@/utils/navigation'

const route = useRoute()
const router = useRouter()
const reservationStore = useReservationStore()

const handleBack = () => {
  navigateBack(router, '/reservations')
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id || Number.isNaN(id)) {
    ElMessage.error('预约 ID 无效')
    handleBack()
    return
  }

  try {
    await reservationStore.fetchReservationDetail(id)
  } catch (error) {
    console.error('Failed to load reservation detail:', error)
    handleBack()
  }
}

const handleCancel = async () => {
  if (!reservationStore.currentReservation) {
    return
  }

  await ElMessageBox.confirm('确定要取消这条预约吗？', '取消预约', {
    type: 'warning'
  })
  await reservationStore.cancelReservation(reservationStore.currentReservation.id)
  await reservationStore.fetchReservationDetail(reservationStore.currentReservation.id)
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.reservation-detail-container {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.empty-state {
  padding: 60px 0;
}
</style>
