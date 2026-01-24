<template>
  <el-card class="price-info-card" v-loading="loading">
    <template #header>
      <div class="card-header">
        <el-icon><Money /></el-icon>
        <span>费用标准</span>
      </div>
    </template>

    <div v-if="priceConfig" class="price-info-content">
      <div class="info-row">
        <span class="label">充电桩类型：</span>
        <el-tag :type="chargingPileType === 'AC' ? 'success' : 'warning'">
          {{ getTypeText() }}
        </el-tag>
      </div>

      <div class="info-row">
        <span class="label">每度电价格：</span>
        <span class="value">{{ formatUnitPrice(priceConfig.pricePerKwh) }}</span>
      </div>

      <div class="info-row">
        <span class="label">服务费：</span>
        <span class="value">{{ formatUnitPrice(priceConfig.serviceFee) }}</span>
      </div>

      <el-divider />

      <div class="info-row total-row">
        <span class="label">总计：</span>
        <span class="total-value">{{ formatTotalPrice() }}</span>
      </div>

      <div v-if="priceConfig.startTime || priceConfig.endTime" class="validity-info">
        <el-icon><Clock /></el-icon>
        <span>{{ formatTimeRange(priceConfig.startTime, priceConfig.endTime) }}</span>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <el-empty description="暂无费用配置" :image-size="80" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Money, Clock } from '@element-plus/icons-vue'
import { usePriceConfigStore } from '@/stores/priceConfig'
import {
  ChargingPileTypeText,
  formatUnitPrice,
  formatTimeRange,
  calculateTotalUnitPrice,
  type ChargingPileType,
  type PriceConfigInfo
} from '@/types/priceConfig'

// Props
interface Props {
  chargingPileType: ChargingPileType
}

const props = defineProps<Props>()

const priceConfigStore = usePriceConfigStore()

// 状态
const priceConfig = ref<PriceConfigInfo | null>(null)
const loading = ref(false)

// 获取类型文本
const getTypeText = () => {
  return ChargingPileTypeText[props.chargingPileType]
}

// 格式化总价
const formatTotalPrice = () => {
  if (!priceConfig.value) return '¥0.00/度'
  const total = calculateTotalUnitPrice(
    priceConfig.value.pricePerKwh,
    priceConfig.value.serviceFee
  )
  return formatUnitPrice(total)
}

// 加载费用配置
const loadPriceConfig = async () => {
  try {
    loading.value = true
    const data = await priceConfigStore.fetchCurrentPriceConfig(props.chargingPileType)
    priceConfig.value = data
  } catch (error) {
    console.error('加载费用配置失败:', error)
    priceConfig.value = null
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadPriceConfig()
})
</script>

<style scoped>
.price-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.price-info-content {
  padding: 10px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.label {
  font-size: 14px;
  color: #606266;
}

.value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.total-row {
  margin-top: 20px;
  margin-bottom: 10px;
}

.total-value {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
}

.validity-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 20px 0;
}
</style>
