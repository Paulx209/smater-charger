<template>
  <el-card class="current-charging-status-card" v-loading="loading">
    <template #header>
      <div class="card-header">
        <el-icon><Lightning /></el-icon>
        <span>当前充电状态</span>
      </div>
    </template>

    <div v-if="currentRecord" class="charging-content">
      <div class="status-indicator">
        <el-tag type="warning" size="large" effect="dark">
          <el-icon class="is-loading"><Loading /></el-icon>
          充电中
        </el-tag>
      </div>

      <el-descriptions :column="1" border class="charging-info">
        <el-descriptions-item label="充电桩">
          <div class="pile-info">
            <div class="pile-name">{{ currentRecord.pileName || '未知充电桩' }}</div>
            <div class="pile-location">{{ currentRecord.pileLocation || '-' }}</div>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="车辆">
          {{ currentRecord.vehicleLicensePlate || '未绑定车辆' }}
        </el-descriptions-item>

        <el-descriptions-item label="开始时间">
          {{ formatDateTime(currentRecord.startTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="已充电时长">
          <span class="duration-text">{{ currentDuration }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="action-buttons">
        <el-button
          type="danger"
          size="large"
          @click="handleEndCharging"
          :loading="endingCharging"
        >
          <el-icon><CircleClose /></el-icon>
          结束充电
        </el-button>

        <el-button size="large" @click="handleViewDetail">
          <el-icon><View /></el-icon>
          查看详情
        </el-button>
      </div>
    </div>

    <div v-else class="no-charging-content">
      <el-empty description="当前没有正在进行的充电" :image-size="100">
        <el-button type="primary" @click="handleGoToChargingPiles">
          <el-icon><Search /></el-icon>
          查看充电桩
        </el-button>
      </el-empty>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lightning, Loading, CircleClose, View, Search } from '@element-plus/icons-vue'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import { calculateChargingDuration, formatDuration } from '@/types/chargingRecord'
import type { ChargingRecordInfo } from '@/types/chargingRecord'

const router = useRouter()
const chargingRecordStore = useChargingRecordStore()

const loading = ref(false)
const currentRecord = ref<ChargingRecordInfo | null>(null)
const currentDuration = ref('0分钟')
const endingCharging = ref(false)

let durationTimer: number | null = null

const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const updateDuration = () => {
  if (!currentRecord.value) return
  const minutes = calculateChargingDuration(currentRecord.value.startTime)
  currentDuration.value = formatDuration(minutes)
}

const loadCurrentRecord = async () => {
  try {
    loading.value = true
    const data = await chargingRecordStore.fetchCurrentChargingRecord()
    currentRecord.value = data

    if (data) {
      updateDuration()
      if (!durationTimer) {
        durationTimer = window.setInterval(updateDuration, 60000)
      }
    } else if (durationTimer) {
      clearInterval(durationTimer)
      durationTimer = null
    }
  } catch (error) {
    console.error('加载当前充电记录失败:', error)
  } finally {
    loading.value = false
  }
}

const confirmEndCharging = async () => {
  if (!currentRecord.value) return

  try {
    endingCharging.value = true
    await chargingRecordStore.finishCharging(currentRecord.value.id)
    await loadCurrentRecord()
    ElMessage.success('充电已结束')
  } catch (error) {
    console.error('结束充电失败:', error)
  } finally {
    endingCharging.value = false
  }
}

const handleEndCharging = async () => {
  if (!currentRecord.value) return

  try {
    await ElMessageBox.confirm(
      `将结束本次充电。系统会根据充电桩功率和实际充电时长自动计算充电量，当前已充电时长约为 ${currentDuration.value}。`,
      '确认结束充电',
      {
        confirmButtonText: '确认结束',
        cancelButtonText: '继续充电',
        type: 'warning'
      }
    )

    await confirmEndCharging()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('结束充电确认失败:', error)
    }
  }
}

const handleViewDetail = () => {
  if (currentRecord.value) {
    router.push(`/charging-record/${currentRecord.value.id}`)
  }
}

const handleGoToChargingPiles = () => {
  router.push('/charging-piles')
}

onMounted(async () => {
  await loadCurrentRecord()
})

onUnmounted(() => {
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
})
</script>

<style scoped>
.current-charging-status-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.charging-content {
  padding: 10px 0;
}

.status-indicator {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.status-indicator .el-tag {
  padding: 12px 24px;
  font-size: 16px;
}

.charging-info {
  margin-bottom: 20px;
}

.pile-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pile-name {
  font-weight: 600;
  color: #303133;
}

.pile-location {
  font-size: 12px;
  color: #909399;
}

.duration-text {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
}

.no-charging-content {
  padding: 20px 0;
}
</style>
