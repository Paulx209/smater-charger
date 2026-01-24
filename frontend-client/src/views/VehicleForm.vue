<template>
  <div class="vehicle-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEditMode ? '编辑车辆' : '添加车辆' }}</span>
        </div>
      </template>

      <el-form
        ref="vehicleFormRef"
        :model="vehicleForm"
        :rules="vehicleRules"
        label-width="100px"
        class="vehicle-form"
      >
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input
            v-model="vehicleForm.licensePlate"
            placeholder="请输入车牌号（如：京A12345）"
            maxlength="8"
            clearable
            @input="handleLicensePlateInput"
          >
            <template #prefix>
              <el-icon><Tickets /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">车牌号会自动转换为大写</div>
        </el-form-item>

        <el-form-item label="品牌" prop="brand">
          <el-input
            v-model="vehicleForm.brand"
            placeholder="请输入品牌（如：特斯拉）"
            maxlength="50"
            clearable
          >
            <template #prefix>
              <el-icon><Van /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="车型" prop="model">
          <el-input
            v-model="vehicleForm.model"
            placeholder="请输入车型（如：Model 3）"
            maxlength="50"
            clearable
          >
            <template #prefix>
              <el-icon><Van /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="电池容量" prop="batteryCapacity">
          <el-input-number
            v-model="vehicleForm.batteryCapacity"
            :min="0"
            :max="200"
            :precision="1"
            :step="0.1"
            placeholder="请输入电池容量"
            style="width: 100%"
          />
          <div class="form-tip">单位：kWh，范围：0-200</div>
        </el-form-item>

        <el-form-item v-if="!isEditMode" label="设为默认" prop="isDefault">
          <el-switch
            v-model="vehicleForm.isDefault"
            :active-value="1"
            :inactive-value="0"
            active-text="是"
            inactive-text="否"
          />
          <div class="form-tip">设为默认车辆后，其他车辆将自动取消默认</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEditMode ? '保存' : '添加' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Tickets, Van } from '@element-plus/icons-vue'
import { useVehicleStore } from '@/stores/vehicle'
import { validateLicensePlate } from '@/types/vehicle'

const router = useRouter()
const route = useRoute()
const vehicleStore = useVehicleStore()

// 表单引用
const vehicleFormRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 是否为编辑模式
const isEditMode = computed(() => !!route.params.id)

// 车辆ID（编辑模式）
const vehicleId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

// 车辆表单
const vehicleForm = reactive({
  licensePlate: '',
  brand: '',
  model: '',
  batteryCapacity: undefined as number | undefined,
  isDefault: 0
})

// 车牌号验证器
const validateLicensePlateRule = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入车牌号'))
  } else if (!validateLicensePlate(value)) {
    callback(new Error('车牌号格式不正确'))
  } else {
    callback()
  }
}

// 表单验证规则
const vehicleRules: FormRules = {
  licensePlate: [{ required: true, validator: validateLicensePlateRule, trigger: 'blur' }],
  brand: [{ max: 50, message: '品牌长度不能超过50', trigger: 'blur' }],
  model: [{ max: 50, message: '车型长度不能超过50', trigger: 'blur' }],
  batteryCapacity: [
    {
      type: 'number',
      min: 0,
      max: 200,
      message: '电池容量范围为0-200kWh',
      trigger: 'blur'
    }
  ]
}

// 车牌号输入处理（自动转大写）
const handleLicensePlateInput = (value: string) => {
  vehicleForm.licensePlate = value.toUpperCase()
}

// 提交表单
const handleSubmit = async () => {
  if (!vehicleFormRef.value) return

  try {
    // 验证表单
    await vehicleFormRef.value.validate()

    loading.value = true

    if (isEditMode.value && vehicleId.value) {
      // 编辑模式
      await vehicleStore.modifyVehicle(vehicleId.value, {
        licensePlate: vehicleForm.licensePlate || undefined,
        brand: vehicleForm.brand || undefined,
        model: vehicleForm.model || undefined,
        batteryCapacity: vehicleForm.batteryCapacity
      })
    } else {
      // 添加模式
      await vehicleStore.addVehicle({
        licensePlate: vehicleForm.licensePlate,
        brand: vehicleForm.brand || undefined,
        model: vehicleForm.model || undefined,
        batteryCapacity: vehicleForm.batteryCapacity,
        isDefault: vehicleForm.isDefault
      })
    }

    // 返回列表页
    router.push('/vehicles')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/vehicles')
}

// 加载车辆数据（编辑模式）
const loadVehicleData = async () => {
  if (!isEditMode.value || !vehicleId.value) return

  try {
    loading.value = true
    const vehicle = await vehicleStore.fetchVehicleById(vehicleId.value)

    // 填充表单
    vehicleForm.licensePlate = vehicle.licensePlate
    vehicleForm.brand = vehicle.brand || ''
    vehicleForm.model = vehicle.model || ''
    vehicleForm.batteryCapacity = vehicle.batteryCapacity
  } catch (error) {
    console.error('加载车辆数据失败:', error)
    ElMessage.error('加载车辆数据失败')
    router.push('/vehicles')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (isEditMode.value) {
    loadVehicleData()
  }
})
</script>

<style scoped>
.vehicle-form-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.form-card {
  min-height: 500px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
}

.vehicle-form {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
