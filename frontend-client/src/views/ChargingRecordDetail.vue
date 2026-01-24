<template>
  <div class="charging-record-detail-container">
    <el-card v-loading="chargingRecordStore.loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span class="header-title">充电记录详情</span>
        </div>
      </template>

      <div v-if="chargingRecordStore.currentRecord" class="detail-content">
        <!-- 状态标签 -->
        <div class="status-section">
          <el-tag
            :type="ChargingRecordStatusColor[chargingRecordStore.currentRecord.status]"
            size="large"
          >
            {{ ChargingRecordStatusText[chargingRecordStore.currentRecord.status] }}
          </el-tag>
        </div>

        <!-- 充电桩信息 -->
        <div class="section">
          <div class="section-title">
            <el-icon><Location /></el-icon>
            <span>充电桩信息</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="充电桩名称">
              {{ chargingRecordStore.currentRecord.pileName || '未知充电桩' }}
            </el-descriptions-item>

            <el-descriptions-item label="充电桩类型">
              <el-tag :type="chargingRecordStore.currentRecord.pileType === 'AC' ? 'success' : 'warning'">
                {{ chargingRecordStore.currentRecord.pileType === 'AC' ? 'AC（交流慢充）' : 'DC（直流快充）' }}
              </el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="充电桩位置" :span="2">
              {{ chargingRecordStore.currentRecord.pileLocation || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 车辆信息 -->
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

        <!-- 充电信息 -->
        <div class="section">
          <div class="section-title">
            <el-icon><Timer /></el-icon>
            <span>充电信息</span>
          </div>

          <el-descriptions :column="2" border>
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

        <!-- 费用信息 -->
        <div v-if="chargingRecordStore.currentRecord.status === ChargingRecordStatus.COMPLETED" class="section">
          <div class="section-title">
            <el-icon><Money /></el-icon>
            <span>费用信息</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="每度电价格">
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

            <el-descriptions-item label="服务费">
              <span class="fee-breakdown">
                {{ chargingRecordStore.currentRecord.feeBreakdown ? formatFee(chargingRecordStore.currentRecord.feeBreakdown.serviceFee) : '-' }}
              </span>
            </el-descriptions-item>

            <el-descriptions-item label="总费用" :span="2">
              <span class="total-fee">
                {{ formatFee(chargingRecordStore.currentRecord.fee) }}
              </span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 记录信息 -->
        <div class="section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>记录信息</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="记录ID">
              {{ chargingRecordStore.currentRecord.id }}
            </el-descriptions-item>

            <el-descriptions-item label="创建时间">
              {{ formatDateTime(chargingRecordStore.currentRecord.createdTime) }}
            </el-descriptions-item>

            <el-descriptions-item label="更新时间" :span="2">
              {{ formatDateTime(chargingRecordStore.currentRecord.updatedTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作按钮 -->
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

      <div v-else class="empty-state">
        <el-empty description="充电记录不存在" />
      </div>
    </el-card>

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
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

const router = useRouter()
const route = useRoute()
const chargingRecordStore = useChargingRecordStore()

// 结束充电状态
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

// 返回列表
const handleBack = () => {
  router.back()
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
  if (!endChargingFormRef.value || !chargingRecordStore.currentRecord) return

  try {
    // 验证表单
    await endChargingFormRef.value.validate()

    endingCharging.value = true

    // 调用结束充电接口
    await chargingRecordStore.finishCharging(chargingRecordStore.currentRecord.id, {
      electricQuantity: endChargingForm.value.electricQuantity
    })

    // 关闭对话框
    endChargingDialogVisible.value = false

    // 重新加载充电记录详情
    const id = Number(route.params.id)
    await chargingRecordStore.fetchChargingRecordDetail(id)

    ElMessage.success('充电已结束')
  } catch (error) {
    console.error('结束充电失败:', error)
  } finally {
    endingCharging.value = false
  }
}

// 格式化日期时间
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

// 组件挂载时获取充电记录详情
onMounted(async () => {
  const id = Number(route.params.id)

  if (!id || isNaN(id)) {
    ElMessage.error('充电记录ID无效')
    router.push('/charging-record')
    return
  }

  try {
    await chargingRecordStore.fetchChargingRecordDetail(id)
  } catch (error) {
    console.error('获取充电记录详情失败:', error)
    router.push('/charging-record')
  }
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

.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style>
