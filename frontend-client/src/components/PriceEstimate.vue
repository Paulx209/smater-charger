<template>
  <el-card class="price-estimate-card">
    <template #header>
      <div class="card-header">
        <el-icon><Coin /></el-icon>
        <span>费用预估</span>
      </div>
    </template>

    <div class="estimate-form">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="预计充电量" prop="electricQuantity">
          <el-input-number
            v-model="form.electricQuantity"
            :min="0.01"
            :max="999.99"
            :precision="2"
            :step="1"
            placeholder="请输入预计充电量"
            style="width: 200px"
          />
          <span class="unit">度</span>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleEstimate"
            :loading="loading"
          >
            <el-icon><Coin /></el-icon>
            预估费用
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 费用明细 -->
      <div v-if="estimateResult" class="estimate-result">
        <el-divider content-position="left">费用明细</el-divider>

        <div class="result-table">
          <div class="result-row">
            <span class="label">充电量：</span>
            <span class="value">{{ estimateResult.electricQuantity.toFixed(2) }} 度</span>
          </div>

          <div class="result-row">
            <span class="label">电费：</span>
            <span class="value">{{ formatPrice(estimateResult.breakdown.electricityFee) }}</span>
          </div>

          <div class="result-row">
            <span class="label">服务费：</span>
            <span class="value">{{ formatPrice(estimateResult.breakdown.serviceFee) }}</span>
          </div>

          <el-divider />

          <div class="result-row total-row">
            <span class="label">总费用：</span>
            <span class="total-value">{{ formatPrice(estimateResult.totalPrice) }}</span>
          </div>
        </div>

        <div class="result-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>以上费用为预估值，实际费用以充电结束后的结算为准</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Coin, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { usePriceConfigStore } from '@/stores/priceConfig'
import {
  formatPrice,
  type ChargingPileType,
  type PriceEstimateResponse
} from '@/types/priceConfig'

// Props
interface Props {
  chargingPileType: ChargingPileType
}

const props = defineProps<Props>()

const priceConfigStore = usePriceConfigStore()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表单数据
const form = reactive({
  electricQuantity: 0
})

// 预估结果
const estimateResult = ref<PriceEstimateResponse | null>(null)

// 验证充电量
const validateElectricQuantity = (rule: any, value: number, callback: any) => {
  if (!value || value <= 0) {
    callback(new Error('充电量必须大于0'))
  } else if (!/^\d+(\.\d{1,2})?$/.test(value.toString())) {
    callback(new Error('充电量最多保留2位小数'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules: FormRules = {
  electricQuantity: [
    { required: true, message: '请输入预计充电量', trigger: 'blur' },
    { validator: validateElectricQuantity, trigger: 'blur' }
  ]
}

// 预估费用
const handleEstimate = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    loading.value = true

    // 调用预估接口
    const result = await priceConfigStore.estimateFee({
      chargingPileType: props.chargingPileType,
      electricQuantity: form.electricQuantity
    })

    estimateResult.value = result
    ElMessage.success('预估成功')
  } catch (error) {
    console.error('预估失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.price-estimate-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.estimate-form {
  padding: 10px 0;
}

.unit {
  margin-left: 10px;
  color: #606266;
}

.estimate-result {
  margin-top: 20px;
}

.result-table {
  padding: 10px 0;
}

.result-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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
  margin-top: 16px;
  margin-bottom: 10px;
}

.total-value {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.result-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 16px;
  padding: 10px;
  background-color: #ecf5ff;
  border-radius: 4px;
  font-size: 12px;
  color: #409eff;
}

.result-tip .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
}
</style>
