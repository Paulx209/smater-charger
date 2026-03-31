<template>
  <div class="vehicle-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
          <span>{{ isEditMode ? '编辑车辆' : '新增车辆' }}</span>
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
            placeholder="请输入车牌号，例如 A12345"
            maxlength="8"
            clearable
            @input="handleLicensePlateInput"
          >
            <template #prefix>
              <el-icon><Tickets /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">车牌号格式会自动转为大写。</div>
        </el-form-item>

        <el-form-item label="品牌" prop="brand">
          <el-input
            v-model="vehicleForm.brand"
            placeholder="请输入品牌，例如 特斯拉"
            maxlength="50"
            clearable
          >
            <template #prefix>
              <el-icon><Van /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="型号" prop="model">
          <el-input
            v-model="vehicleForm.model"
            placeholder="请输入型号，例如 Model 3"
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
          <div class="form-tip">单位为 kWh，范围 0-200。</div>
        </el-form-item>

        <el-form-item v-if="!isEditMode" label="设为默认车" prop="isDefault">
          <el-switch
            v-model="vehicleForm.isDefault"
            :active-value="1"
            :inactive-value="0"
            active-text="是"
            inactive-text="否"
          />
          <div class="form-tip">设置后会优先作为预约和充电时的默认车辆。</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEditMode ? '保存' : '新增' }}
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
import { ArrowLeft, Tickets, Van } from '@element-plus/icons-vue'
import { useVehicleStore } from '@/stores/vehicle'
import { validateLicensePlate } from '@/types/vehicle'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const route = useRoute()
const vehicleStore = useVehicleStore()

const vehicleFormRef = ref<FormInstance>()
const loading = ref(false)

const isEditMode = computed(() => !!route.params.id)
const vehicleId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

const vehicleForm = reactive({
  licensePlate: '',
  brand: '',
  model: '',
  batteryCapacity: undefined as number | undefined,
  isDefault: 0
})

const validateLicensePlateRule = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入车牌号'))
  } else if (!validateLicensePlate(value)) {
    callback(new Error('车牌号格式不正确'))
  } else {
    callback()
  }
}

const vehicleRules: FormRules = {
  licensePlate: [{ required: true, validator: validateLicensePlateRule, trigger: 'blur' }],
  brand: [{ max: 50, message: '品牌长度不能超过 50 个字符', trigger: 'blur' }],
  model: [{ max: 50, message: '型号长度不能超过 50 个字符', trigger: 'blur' }],
  batteryCapacity: [
    {
      type: 'number',
      min: 0,
      max: 200,
      message: '电池容量范围为 0-200kWh',
      trigger: 'blur'
    }
  ]
}

const handleLicensePlateInput = (value: string) => {
  vehicleForm.licensePlate = value.toUpperCase()
}

const handleBack = () => {
  navigateBack(router, '/vehicles')
}

const handleCancel = () => {
  navigateBack(router, '/vehicles')
}

const handleSubmit = async () => {
  if (!vehicleFormRef.value) return

  try {
    await vehicleFormRef.value.validate()
    loading.value = true

    if (isEditMode.value && vehicleId.value) {
      await vehicleStore.modifyVehicle(vehicleId.value, {
        licensePlate: vehicleForm.licensePlate || undefined,
        brand: vehicleForm.brand || undefined,
        model: vehicleForm.model || undefined,
        batteryCapacity: vehicleForm.batteryCapacity
      })
    } else {
      await vehicleStore.addVehicle({
        licensePlate: vehicleForm.licensePlate,
        brand: vehicleForm.brand || undefined,
        model: vehicleForm.model || undefined,
        batteryCapacity: vehicleForm.batteryCapacity,
        isDefault: vehicleForm.isDefault
      })
    }

    router.push('/vehicles')
  } catch (error) {
    console.error('保存车辆失败:', error)
  } finally {
    loading.value = false
  }
}

const loadVehicleData = async () => {
  if (!isEditMode.value || !vehicleId.value) return

  try {
    loading.value = true
    const vehicle = await vehicleStore.fetchVehicleById(vehicleId.value)
    vehicleForm.licensePlate = vehicle.licensePlate
    vehicleForm.brand = vehicle.brand || ''
    vehicleForm.model = vehicle.model || ''
    vehicleForm.batteryCapacity = vehicle.batteryCapacity
  } catch (error) {
    console.error('加载车辆信息失败:', error)
    ElMessage.error('加载车辆信息失败')
    router.push('/vehicles')
  } finally {
    loading.value = false
  }
}

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
  display: flex;
  align-items: center;
  gap: 16px;
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
