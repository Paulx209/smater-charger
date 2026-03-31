<template>
  <div class="charging-record-detail-container">
    <el-card v-loading="chargingRecordStore.loading">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
          <span class="header-title">充电记录详情</span>
        </div>
      </template>

      <div v-if="chargingRecordStore.currentRecord" class="detail-content">
        <div class="section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="记录 ID">#{{ chargingRecordStore.currentRecord.id }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getChargingRecordStatusTagType(chargingRecordStore.currentRecord.status)">
                {{ getChargingRecordStatusText(chargingRecordStore.currentRecord.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用户 ID">#{{ chargingRecordStore.currentRecord.userId }}</el-descriptions-item>
            <el-descriptions-item label="车辆">{{ chargingRecordStore.currentRecord.vehicleLicensePlate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(chargingRecordStore.currentRecord.createdTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(chargingRecordStore.currentRecord.updatedTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">充电桩信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="充电桩 ID">#{{ chargingRecordStore.currentRecord.chargingPileId }}</el-descriptions-item>
            <el-descriptions-item label="充电桩名称">
              {{ chargingRecordStore.currentRecord.pileName || chargingRecordStore.currentRecord.chargingPileCode || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="充电桩位置" :span="2">
              {{ chargingRecordStore.currentRecord.pileLocation || chargingRecordStore.currentRecord.chargingPileLocation || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="充电桩类型">{{ chargingRecordStore.currentRecord.pileType || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">充电信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="开始时间">{{ formatDateTime(chargingRecordStore.currentRecord.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(chargingRecordStore.currentRecord.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="充电时长">{{ formatDuration(chargingRecordStore.currentRecord.duration) }}</el-descriptions-item>
            <el-descriptions-item label="充电量">{{ formatElectricity(chargingRecordStore.currentRecord.electricQuantity) }}</el-descriptions-item>
            <el-descriptions-item label="总费用">{{ formatMoney(chargingRecordStore.currentRecord.fee) }}</el-descriptions-item>
            <el-descriptions-item label="状态描述">{{ chargingRecordStore.currentRecord.statusDesc || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section" v-if="chargingRecordStore.currentRecord.feeBreakdown">
          <div class="section-title">费用拆分</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="电费">{{ formatMoney(chargingRecordStore.currentRecord.feeBreakdown?.electricityFee) }}</el-descriptions-item>
            <el-descriptions-item label="服务费">{{ formatMoney(chargingRecordStore.currentRecord.feeBreakdown?.serviceFee) }}</el-descriptions-item>
            <el-descriptions-item label="电价">{{ formatMoney(chargingRecordStore.currentRecord.pricePerKwh) }}</el-descriptions-item>
            <el-descriptions-item label="服务费单价">{{ formatMoney(chargingRecordStore.currentRecord.serviceFee) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="未找到充电记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import {
  formatDateTime,
  formatDuration,
  formatElectricity,
  formatMoney,
  getChargingRecordStatusTagType,
  getChargingRecordStatusText
} from '@/types/chargingRecord'
import { navigateBack } from '@/utils/navigation'

const route = useRoute()
const router = useRouter()
const chargingRecordStore = useChargingRecordStore()

const handleBack = () => {
  navigateBack(router, '/charging-records')
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id || Number.isNaN(id)) {
    ElMessage.error('充电记录 ID 无效')
    handleBack()
    return
  }

  try {
    await chargingRecordStore.fetchRecordDetail(id)
  } catch (error) {
    console.error('Failed to load charging record detail:', error)
    handleBack()
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.charging-record-detail-container {
  padding: 20px;
  max-width: 1100px;
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
