<template>
  <div class="charging-record-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span class="header-title">充电记录详情</span>
        </div>
      </template>

      <template v-if="loading">
        <div class="skeleton-container">
          <el-skeleton :rows="3" animated class="skeleton-section" />
          <el-skeleton :rows="4" animated class="skeleton-section" />
          <el-skeleton :rows="4" animated class="skeleton-section" />
        </div>
      </template>

      <template v-else-if="error">
        <el-result icon="error" title="加载失败" :sub-title="error">
          <template #extra>
            <el-button type="primary" @click="handleRetry">重试</el-button>
          </template>
        </el-result>
      </template>

      <template v-else-if="chargingRecordStore.currentRecord">
        <div class="detail-content">
          <div class="status-section">
            <el-tag
              :type="ChargingRecordStatusColor[chargingRecordStore.currentRecord.status]"
              size="large"
            >
              {{ ChargingRecordStatusText[chargingRecordStore.currentRecord.status] }}
            </el-tag>
          </div>

          <div class="section">
            <div class="section-title">
              <el-icon><Location /></el-icon>
              <span>充电桩信息</span>
            </div>

            <el-descriptions :column="isMobile ? 1 : 2" border>
              <el-descriptions-item label="充电桩编号">
                {{ chargingRecordStore.currentRecord.pileName || '未知充电桩' }}
              </el-descriptions-item>

              <el-descriptions-item label="充电桩类型">
                <el-tag :type="chargingRecordStore.currentRecord.pileType === 'AC' ? 'success' : 'warning'">
                  {{ chargingRecordStore.currentRecord.pileType === 'AC' ? 'AC（交流慢充）' : 'DC（直流快充）' }}
                </el-tag>
              </el-descriptions-item>

              <el-descriptions-item label="充电桩位置" :span="isMobile ? 1 : 2">
                {{ chargingRecordStore.currentRecord.pileLocation || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="section">
            <div class="section-title">
              <el-icon><Van /></el-icon>
              <span>车辆信息</span>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="车牌号">
                {{ chargingRecordStore.currentRecord.vehicleLicensePlate || '未绑定车辆' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="section">
            <div class="section-title">
              <el-icon><Timer /></el-icon>
              <span>充电信息</span>
            </div>

            <el-descriptions :column="isMobile ? 1 : 2" border>
              <el-descriptions-item label="开始时间">
                {{ formatDateTime(chargingRecordStore.currentRecord.startTime) }}
              </el-descriptions-item>

              <el-descriptions-item label="结束时间">
                {{ chargingRecordStore.currentRecord.endTime ? formatDateTime(chargingRecordStore.currentRecord.endTime) : '充电中' }}
              </el-descriptions-item>

              <el-descriptions-item label="充电时长">
                <span class="highlight-text">
                  {{ formatDuration(chargingRecordStore.currentRecord.duration) }}
                </span>
              </el-descriptions-item>

              <el-descriptions-item label="充电量">
                <span class="highlight-text">
                  {{ formatElectricQuantity(chargingRecordStore.currentRecord.electricQuantity) }}
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div v-if="chargingRecordStore.currentRecord.status === ChargingRecordStatus.COMPLETED" class="section">
            <div class="section-title">
              <el-icon><Money /></el-icon>
              <span>费用信息</span>
            </div>

            <el-descriptions :column="isMobile ? 1 : 2" border>
              <el-descriptions-item label="每度电价">
                {{ chargingRecordStore.currentRecord.pricePerKwh ? `¥${chargingRecordStore.currentRecord.pricePerKwh.toFixed(2)}/度` : '-' }}
              </el-descriptions-item>

              <el-descriptions-item label="服务费">
                {{ chargingRecordStore.currentRecord.serviceFee ? `¥${chargingRecordStore.currentRecord.serviceFee.toFixed(2)}/度` : '-' }}
              </el-descriptions-item>

              <el-descriptions-item label="电费">
                <span class="fee-breakdown">
                  {{ chargingRecordStore.currentRecord.feeBreakdown ? formatFee(chargingRecordStore.currentRecord.feeBreakdown.electricityFee) : '-' }}
                </span>
              </el-descriptions-item>

              <el-descriptions-item label="服务费合计">
                <span class="fee-breakdown">
                  {{ chargingRecordStore.currentRecord.feeBreakdown ? formatFee(chargingRecordStore.currentRecord.feeBreakdown.serviceFee) : '-' }}
                </span>
              </el-descriptions-item>

              <el-descriptions-item label="总费用" :span="isMobile ? 1 : 2">
                <span class="total-fee">
                  {{ formatFee(chargingRecordStore.currentRecord.fee) }}
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="section">
            <div class="section-title">
              <el-icon><InfoFilled /></el-icon>
              <span>记录信息</span>
            </div>

            <el-descriptions :column="isMobile ? 1 : 2" border>
              <el-descriptions-item label="记录 ID">
                {{ chargingRecordStore.currentRecord.id }}
              </el-descriptions-item>

              <el-descriptions-item label="创建时间">
                {{ formatDateTime(chargingRecordStore.currentRecord.createdTime) }}
              </el-descriptions-item>

              <el-descriptions-item label="更新时间" :span="isMobile ? 1 : 2">
                {{ formatDateTime(chargingRecordStore.currentRecord.updatedTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div v-if="chargingRecordStore.currentRecord.status === ChargingRecordStatus.CHARGING" class="action-section">
            <el-button
              type="danger"
              size="large"
              @click="handleEndCharging"
              :loading="endingCharging"
            >
              <el-icon><CircleClose /></el-icon>
              结束充电
            </el-button>
          </div>
        </div>
      </template>

      <div v-else class="empty-state">
        <el-empty description="充电记录不存在" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Location,
  Van,
  Timer,
  Money,
  InfoFilled,
  CircleClose
} from '@element-plus/icons-vue'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import {
  ChargingRecordStatus,
  ChargingRecordStatusText,
  ChargingRecordStatusColor,
  formatDuration,
  formatElectricQuantity,
  formatFee
} from '@/types/chargingRecord'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const route = useRoute()
const chargingRecordStore = useChargingRecordStore()

const loading = ref(false)
const error = ref<string | null>(null)
const endingCharging = ref(false)
const windowWidth = ref(window.innerWidth)

const isMobile = computed(() => windowWidth.value < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
}

const handleBack = () => {
  navigateBack(router, '/charging-record')
}

const handleRetry = async () => {
  await loadDetail()
}

const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getDurationText = () => {
  const record = chargingRecordStore.currentRecord
  if (!record) return '-'

  if (record.duration !== undefined && record.duration !== null) {
    return formatDuration(record.duration)
  }

  const start = new Date(record.startTime).getTime()
  const now = Date.now()
  const minutes = Math.max(1, Math.floor((now - start) / (1000 * 60)))
  return formatDuration(minutes)
}

const confirmEndCharging = async () => {
  if (!chargingRecordStore.currentRecord) return

  try {
    endingCharging.value = true
    await chargingRecordStore.finishCharging(chargingRecordStore.currentRecord.id)
    await loadDetail()
    ElMessage.success('充电已结束')
  } catch (err: any) {
    ElMessage.error(err.message || '结束充电失败')
  } finally {
    endingCharging.value = false
  }
}

const handleEndCharging = async () => {
  if (!chargingRecordStore.currentRecord) return

  try {
    await ElMessageBox.confirm(
      `将结束本次充电。系统会根据充电桩功率和实际充电时长自动计算充电量，当前已充电时长约为 ${getDurationText()}。`,
      '确认结束充电',
      {
        confirmButtonText: '确认结束',
        cancelButtonText: '继续充电',
        type: 'warning'
      }
    )

    await confirmEndCharging()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('结束充电确认失败:', err)
    }
  }
}

const loadDetail = async () => {
  const id = Number(route.params.id)

  if (!id || Number.isNaN(id)) {
    ElMessage.error('充电记录 ID 无效')
    router.push('/charging-record')
    return
  }

  try {
    loading.value = true
    error.value = null
    await chargingRecordStore.fetchChargingRecordDetail(id)
  } catch (err: any) {
    error.value = err.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  await loadDetail()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.charging-record-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.skeleton-container {
  padding: 20px 0;
}

.skeleton-section {
  margin-bottom: 30px;
}

.detail-content {
  padding: 20px 0;
}

.status-section {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  color: #409eff;
}

.highlight-text {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.fee-breakdown {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.total-fee {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.empty-state {
  padding: 60px 0;
}

.action-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .charging-record-detail-container {
    padding: 12px;
  }

  .header-title {
    font-size: 16px;
  }

  .detail-content {
    padding: 12px 0;
  }

  .section {
    margin-bottom: 24px;
  }

  .section-title {
    font-size: 14px;
  }

  .highlight-text {
    font-size: 14px;
  }

  .fee-breakdown {
    font-size: 14px;
  }

  .total-fee {
    font-size: 20px;
  }

  .action-section {
    flex-direction: column;
  }

  .action-section .el-button {
    width: 100%;
  }
}
</style>
