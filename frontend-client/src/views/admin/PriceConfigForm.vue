<template>
  <div class="price-config-form-container">
    <el-card class="form-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span>{{ isEdit ? '编辑费用配置' : '新增费用配置' }}</span>
          <div></div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
        class="price-config-form"
      >
        <el-form-item label="充电桩类型" prop="chargingPileType">
          <el-select
            v-model="form.chargingPileType"
            placeholder="请选择充电桩类型"
            :disabled="isEdit"
            style="width: 300px"
          >
            <el-option label="AC交流慢充" value="AC" />
            <el-option label="DC直流快充" value="DC" />
          </el-select>
          <div class="form-tip">编辑时不可修改充电桩类型</div>
        </el-form-item>

        <el-form-item label="每度电价格" prop="pricePerKwh">
          <el-input-number
            v-model="form.pricePerKwh"
            :min="0.01"
            :max="999.99"
            :precision="2"
            :step="0.1"
            placeholder="请输入每度电价格"
            style="width: 300px"
          />
          <span class="unit">元/度</span>
          <div class="form-tip">价格必须大于0，最多保留2位小数</div>
        </el-form-item>

        <el-form-item label="服务费" prop="serviceFee">
          <el-input-number
            v-model="form.serviceFee"
            :min="0"
            :max="999.99"
            :precision="2"
            :step="0.1"
            placeholder="请输入服务费"
            style="width: 300px"
          />
          <span class="unit">元/度</span>
          <div class="form-tip">服务费不能为负数，最多保留2位小数</div>
        </el-form-item>

        <el-form-item label="总计">
          <div class="total-price">
            {{ formatTotalPrice() }}
          </div>
          <div class="form-tip">每度总费用 = 每度电价格 + 服务费</div>
        </el-form-item>

        <el-form-item label="生效开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="请选择生效开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 300px"
          />
          <div class="form-tip">不填写则表示从创建时开始生效</div>
        </el-form-item>

        <el-form-item label="生效结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="请选择生效结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 300px"
          />
          <div class="form-tip">不填写则表示长期有效</div>
        </el-form-item>

        <el-form-item label="是否激活" prop="isActive">
          <el-switch
            v-model="form.isActive"
            :active-value="1"
            :inactive-value="0"
            active-text="激活"
            inactive-text="停用"
          />
          <div class="form-tip">激活后该配置将立即生效</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { usePriceConfigStore } from '@/stores/priceConfig'
import { formatUnitPrice, type ChargingPileType } from '@/types/priceConfig'

const router = useRouter()
const route = useRoute()
const priceConfigStore = usePriceConfigStore()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 配置ID
const configId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

// 表单数据
const form = reactive({
  chargingPileType: '' as ChargingPileType | '',
  pricePerKwh: 0,
  serviceFee: 0,
  startTime: '',
  endTime: '',
  isActive: 1
})

// 验证价格
const validatePrice = (rule: any, value: number, callback: any) => {
  if (!value || value <= 0) {
    callback(new Error('价格必须大于0'))
  } else if (!/^\d+(\.\d{1,2})?$/.test(value.toString())) {
    callback(new Error('价格最多保留2位小数'))
  } else {
    callback()
  }
}

// 验证服务费
const validateServiceFee = (rule: any, value: number, callback: any) => {
  if (value < 0) {
    callback(new Error('服务费不能为负数'))
  } else if (!/^\d+(\.\d{1,2})?$/.test(value.toString())) {
    callback(new Error('服务费最多保留2位小数'))
  } else {
    callback()
  }
}

// 验证时间范围
const validateTimeRange = (rule: any, value: string, callback: any) => {
  if (form.startTime && form.endTime) {
    if (new Date(form.startTime) >= new Date(form.endTime)) {
      callback(new Error('开始时间必须小于结束时间'))
    }
  }
  callback()
}

// 表单验证规则
const rules: FormRules = {
  chargingPileType: [
    { required: true, message: '请选择充电桩类型', trigger: 'change' }
  ],
  pricePerKwh: [
    { required: true, message: '请输入每度电价格', trigger: 'blur' },
    { validator: validatePrice, trigger: 'blur' }
  ],
  serviceFee: [
    { required: true, message: '请输入服务费', trigger: 'blur' },
    { validator: validateServiceFee, trigger: 'blur' }
  ],
  startTime: [
    { validator: validateTimeRange, trigger: 'change' }
  ],
  endTime: [
    { validator: validateTimeRange, trigger: 'change' }
  ]
}

// 格式化总价
const formatTotalPrice = () => {
  const total = (form.pricePerKwh || 0) + (form.serviceFee || 0)
  return formatUnitPrice(total)
}

// 返回
const handleBack = () => {
  router.push('/admin/price-config')
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    loading.value = true

    if (isEdit.value && configId.value) {
      // 编辑模式
      await priceConfigStore.modifyPriceConfig(configId.value, {
        pricePerKwh: form.pricePerKwh,
        serviceFee: form.serviceFee,
        isActive: form.isActive
      })
    } else {
      // 新增模式
      await priceConfigStore.addPriceConfig({
        chargingPileType: form.chargingPileType as ChargingPileType,
        pricePerKwh: form.pricePerKwh,
        serviceFee: form.serviceFee,
        startTime: form.startTime || undefined,
        endTime: form.endTime || undefined,
        isActive: form.isActive
      })
    }

    // 成功后返回列表页
    router.push('/admin/price-config')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载配置详情（编辑模式）
const loadConfigDetail = async () => {
  if (!configId.value) return

  try {
    loading.value = true
    const data = await priceConfigStore.fetchPriceConfigDetail(configId.value)

    // 填充表单
    form.chargingPileType = data.chargingPileType
    form.pricePerKwh = data.pricePerKwh
    form.serviceFee = data.serviceFee
    form.startTime = data.startTime || ''
    form.endTime = data.endTime || ''
    form.isActive = data.isActive
  } catch (error) {
    console.error('加载配置详情失败:', error)
    ElMessage.error('加载配置详情失败')
    router.push('/admin/price-config')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (isEdit.value) {
    loadConfigDetail()
  }
})
</script>

<style scoped>
.price-config-form-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.form-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.price-config-form {
  padding: 20px 0;
}

.unit {
  margin-left: 10px;
  color: #606266;
}

.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.total-price {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}
</style>
