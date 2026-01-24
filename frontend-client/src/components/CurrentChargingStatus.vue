<template>
  <el-card class="current-charging-status-card" v-loading="loading">
    <template #header>
      <div class="card-header">
        <el-icon><Lightning /></el-icon>
        <span>当前充电状态</span>
      </div>
    </template>

    <!-- 正在充电 -->
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

        <el-button
          size="large"
          @click="handleViewDetail"
        >
          <el-icon><View /></el-icon>
          查看详情
        </el-button>
      </div>
    </div>

    <!-- 未充电 -->
    <div v-else class="no-charging-content">
      <el-empty description="当前没有正在进行的充电" :image-size="100">
        <el-button type="primary" @click="handleGoToChargingPiles">
          <el-icon><Search /></el-icon>
          查找充电桩
        </el-button>
      </el-empty>
    </div>

    <!-- 结束充电对话框 -->
    <el-dialog
      v-model="endChargingDialogVisible"
      title="结束充电"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="endChargingFormRef"
        :model="endChargingForm"
        :rules="endChargingRules"
        label-width="120px"
      >
        <el-form-item label="充电量" prop="electricQuantity">
          <el-input-number
            v-model="endChargingForm.electricQuantity"
            :min="0.01"
            :max="999.99"
            :precision="2"
            :step="1"
            placeholder="请输入充电量"
            style="width: 100%"
          />
          <div class="form-tip">单位：度（kWh）</div>
        </el-form-item>

        <el-form-item label="快捷选择">
          <el-button-group>
            <el-button @click="setElectricQuantity(10)">10度</el-button>
            <el-button @click="setElectricQuantity(20)">20度</el-button>
            <el-button @click="setElectricQuantity(30)">30度</el-button>
            <el-button @click="setElectricQuantity(50)">50度</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="endChargingDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmEndCharging"
          :loading="endingCharging"
        >
          确认结束
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Lightning,
  Loading,
  CircleClose,
  View,
  Search
} from '@element-plus/icons-vue'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import { calculateChargingDuration, formatDuration } from '@/types/chargingRecord'
import type { ChargingRecordInfo } from '@/types/chargingRecord'

const router = useRouter()
const chargingRecordStore = useChargingRecordStore()

// 状态
const loading = ref(false)
const currentRecord = ref<ChargingRecordInfo | null>(null)
const currentDuration = ref('0分钟')
const endingCharging = ref(false)
const endChargingDialogVisible = ref(false)

// 结束充电表单
const endChargingFormRef = ref<FormInstance>()
const endChargingForm = ref({
  electricQuantity: 0
})

// 表单验证规则
const endChargingRules: FormRules = {
  electricQuantity: [
    { required: true, message: '请输入充电量', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value || value <= 0) {
          callback(new Error('充电量必须大于0'))
        } else if (!/^\d+(\.\d{1,2})?$/.test(value.toString())) {
          callback(new Error('充电量最多保留2位小数'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 定时器
let durationTimer: number | null = null

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 更新充电时长
const updateDuration = () => {
  if (currentRecord.value) {
    const minutes = calculateChargingDuration(currentRecord.value.startTime)
    currentDuration.value = formatDuration(minutes)
  }
}

// 加载当前充电记录
const loadCurrentRecord = async () => {
  try {
    loading.value = true
    const data = await chargingRecordStore.fetchCurrentChargingRecord()
    currentRecord.value = data

    if (data) {
      updateDuration()
      // 启动定时器，每分钟更新一次时长
      if (!durationTimer) {
        durationTimer = window.setInterval(updateDuration, 60000)
      }
    } else {
      // 清除定时器
      if (durationTimer) {
        clearInterval(durationTimer)
        durationTimer = null
      }
    }
  } catch (error) {
    console.error('加载当前充电记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 设置充电量
const setElectricQuantity = (quantity: number) => {
  endChargingForm.value.electricQuantity = quantity
}

// 结束充电
const handleEndCharging = () => {
  endChargingForm.value.electricQuantity = 0
  endChargingDialogVisible.value = true
}

// 确认结束充电
const confirmEndCharging = async () => {
  if (!endChargingFormRef.value || !currentRecord.value) return

  try {
    // 验证表单
    await endChargingFormRef.value.validate()

    endingCharging.value = true

    // 调用结束充电接口
    await chargingRecordStore.finishCharging(currentRecord.value.id, {
      electricQuantity: endChargingForm.value.electricQuantity
    })

    // 关闭对话框
    endChargingDialogVisible.value = false

    // 重新加载当前充电记录
    await loadCurrentRecord()

    ElMessage.success('充电已结束')
  } catch (error) {
    console.error('结束充电失败:', error)
  } finally {
    endingCharging.value = false
  }
}

// 查看详情
const handleViewDetail = () => {
  if (currentRecord.value) {
    router.push(`/charging-record/${currentRecord.value.id}`)
  }
}

// 前往充电桩列表
const handleGoToChargingPiles = () => {
  router.push('/charging-piles')
}

// 组件挂载时加载数据
onMounted(async () => {
  await loadCurrentRecord()
})

// 组件卸载时清除定时器
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

.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style>
