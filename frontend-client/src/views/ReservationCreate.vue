<template>
  <div class="reservation-create-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span>创建预约</span>
          <div></div>
        </div>
      </template>

      <el-form
        ref="reservationFormRef"
        :model="reservationForm"
        :rules="reservationRules"
        label-width="120px"
        class="reservation-form"
      >
        <!-- 选择充电桩 -->
        <el-form-item label="充电桩" prop="pileId">
          <el-select
            v-model="reservationForm.pileId"
            placeholder="请选择充电桩"
            filterable
            clearable
            style="width: 100%"
            @change="handlePileChange"
          >
            <el-option
              v-for="pile in availablePiles"
              :key="pile.id"
              :label="`${pile.code} - ${pile.location}`"
              :value="pile.id"
            >
              <div class="pile-option">
                <span class="pile-code">{{ pile.code }}</span>
                <span class="pile-location">{{ pile.location }}</span>
                <el-tag :type="getPileStatusColor(pile.status)" size="small">
                  {{ pile.statusDesc }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">只显示空闲状态的充电桩</div>
        </el-form-item>

        <!-- 选择车辆 -->
        <el-form-item label="车辆" prop="vehicleId">
          <el-select
            v-model="reservationForm.vehicleId"
            placeholder="请选择车辆"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="vehicle in vehicleStore.vehicles"
              :key="vehicle.id"
              :label="vehicle.licensePlate"
              :value="vehicle.id"
            >
              <div class="vehicle-option">
                <span class="license-plate">{{ vehicle.licensePlate }}</span>
                <span v-if="vehicle.brand" class="vehicle-brand">{{ vehicle.brand }}</span>
                <span v-if="vehicle.model" class="vehicle-model">{{ vehicle.model }}</span>
                <el-tag v-if="vehicle.isDefault === 1" type="success" size="small">
                  默认
                </el-tag>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">
            <span>请先添加车辆</span>
            <el-button type="text" size="small" @click="handleAddVehicle">
              去添加
            </el-button>
          </div>
        </el-form-item>

        <!-- 预约时间 -->
        <el-form-item label="预约时间" prop="timeRange">
          <el-date-picker
            v-model="reservationForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
            :disabled-minutes="disabledMinutes"
            style="width: 100%"
            @change="handleTimeRangeChange"
          />
          <div class="form-tip">
            预约时间必须在未来，且时长不超过4小时
          </div>
        </el-form-item>

        <!-- 预约时长显示 -->
        <el-form-item v-if="duration > 0" label="预约时长">
          <el-tag type="info" size="large">{{ duration }} 小时</el-tag>
        </el-form-item>

        <!-- 充电桩信息预览 -->
        <el-form-item v-if="selectedPile" label="充电桩信息">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="编号">
              {{ selectedPile.code }}
            </el-descriptions-item>
            <el-descriptions-item label="位置">
              {{ selectedPile.location }}
            </el-descriptions-item>
            <el-descriptions-item label="类型">
              {{ selectedPile.typeDesc }}
            </el-descriptions-item>
            <el-descriptions-item label="功率">
              {{ selectedPile.power }} kW
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getPileStatusColor(selectedPile.status)">
                {{ selectedPile.statusDesc }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
            size="large"
          >
            提交预约
          </el-button>
          <el-button @click="handleBack" size="large">取消</el-button>
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
import { useReservationStore } from '@/stores/reservation'
import { useVehicleStore } from '@/stores/vehicle'
import { useChargingPileStore } from '@/stores/chargingPile'
import { validateReservationTime, calculateReservationDuration } from '@/types/reservation'
import { ChargingPileStatus, ChargingPileStatusTagType } from '@/types/chargingPile'

const router = useRouter()
const route = useRoute()
const reservationStore = useReservationStore()
const vehicleStore = useVehicleStore()
const chargingPileStore = useChargingPileStore()

// 表单引用
const reservationFormRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 预约表单
const reservationForm = reactive({
  pileId: undefined as number | undefined,
  vehicleId: undefined as number | undefined,
  timeRange: null as [string, string] | null
})

// 可用充电桩（只显示空闲状态）
const availablePiles = computed(() =>
  chargingPileStore.chargingPiles.filter(pile => pile.status === ChargingPileStatus.IDLE)
)

// 选中的充电桩
const selectedPile = computed(() =>
  chargingPileStore.chargingPiles.find(pile => pile.id === reservationForm.pileId)
)

// 预约时长
const duration = computed(() => {
  if (!reservationForm.timeRange) return 0
  const [startTime, endTime] = reservationForm.timeRange
  return calculateReservationDuration(startTime, endTime)
})

// 时间范围验证器
const validateTimeRange = (rule: any, value: [string, string] | null, callback: any) => {
  if (!value || value.length !== 2) {
    callback(new Error('请选择预约时间'))
    return
  }

  const [startTime, endTime] = value
  if (!validateReservationTime(startTime, endTime)) {
    callback(new Error('预约时间不合法（必须在未来，且时长不超过4小时）'))
    return
  }

  callback()
}

// 表单验证规则
const reservationRules: FormRules = {
  pileId: [{ required: true, message: '请选择充电桩', trigger: 'change' }],
  vehicleId: [{ required: true, message: '请选择车辆', trigger: 'change' }],
  timeRange: [{ required: true, validator: validateTimeRange, trigger: 'change' }]
}

// 获取充电桩状态颜色
const getPileStatusColor = (status: string) => {
  return ChargingPileStatusTagType[status as ChargingPileStatus] || 'info'
}

// 禁用日期（只能选择今天及以后）
const disabledDate = (date: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

// 禁用小时（如果是今天，禁用过去的小时）
const disabledHours = () => {
  const now = new Date()
  const currentHour = now.getHours()
  const hours: number[] = []

  // 如果选择的是今天，禁用过去的小时
  if (reservationForm.timeRange && reservationForm.timeRange[0]) {
    const selectedDate = new Date(reservationForm.timeRange[0])
    const today = new Date()
    if (
      selectedDate.getFullYear() === today.getFullYear() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getDate() === today.getDate()
    ) {
      for (let i = 0; i < currentHour; i++) {
        hours.push(i)
      }
    }
  }

  return hours
}

// 禁用分钟（如果是当前小时，禁用过去的分钟）
const disabledMinutes = (hour: number) => {
  const now = new Date()
  const currentHour = now.getHours()
  const currentMinute = now.getMinutes()
  const minutes: number[] = []

  // 如果选择的是今天的当前小时，禁用过去的分钟
  if (reservationForm.timeRange && reservationForm.timeRange[0]) {
    const selectedDate = new Date(reservationForm.timeRange[0])
    const today = new Date()
    if (
      selectedDate.getFullYear() === today.getFullYear() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getDate() === today.getDate() &&
      hour === currentHour
    ) {
      for (let i = 0; i <= currentMinute; i++) {
        minutes.push(i)
      }
    }
  }

  return minutes
}

// 充电桩改变
const handlePileChange = () => {
  // 可以在这里添加额外的逻辑
}

// 时间范围改变
const handleTimeRangeChange = async () => {
  if (!reservationForm.pileId || !reservationForm.timeRange) return

  const [startTime, endTime] = reservationForm.timeRange

  try {
    // 检查充电桩在该时间段是否可用
    const isAvailable = await reservationStore.checkAvailability(
      reservationForm.pileId,
      startTime,
      endTime
    )

    if (!isAvailable) {
      ElMessage.warning('该充电桩在选定时间段已被预约，请选择其他时间')
      reservationForm.timeRange = null
    }
  } catch (error) {
    console.error('检查充电桩可用性失败:', error)
  }
}

// 添加车辆
const handleAddVehicle = () => {
  router.push('/vehicles/add')
}

// 返回
const handleBack = () => {
  router.push('/reservations')
}

// 提交表单
const handleSubmit = async () => {
  if (!reservationFormRef.value) return

  try {
    // 验证表单
    await reservationFormRef.value.validate()

    if (!reservationForm.pileId || !reservationForm.vehicleId || !reservationForm.timeRange) {
      ElMessage.error('请填写完整信息')
      return
    }

    loading.value = true

    const [startTime, endTime] = reservationForm.timeRange

    // 再次检查充电桩可用性
    const isAvailable = await reservationStore.checkAvailability(
      reservationForm.pileId,
      startTime,
      endTime
    )

    if (!isAvailable) {
      ElMessage.error('该充电桩在选定时间段已被预约，请重新选择')
      return
    }

    // 创建预约
    await reservationStore.addReservation({
      chargingPileId: reservationForm.pileId,
      startTime
    })

    // 返回列表页
    router.push('/reservations')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载数据
const loadData = async () => {
  try {
    // 加载充电桩列表
    await chargingPileStore.fetchChargingPiles({ status: ChargingPileStatus.IDLE })

    // 加载车辆列表
    await vehicleStore.fetchMyVehicles()

    // 从路由参数获取充电桩ID（如果有）
    const pileIdParam = route.params.pileId
    if (pileIdParam) {
      const pileId = Number(pileIdParam)
      if (!isNaN(pileId)) {
        reservationForm.pileId = pileId
        console.log('从路由参数预填充充电桩ID:', pileId)
      }
    }

    // 如果有默认车辆，自动选中
    if (vehicleStore.defaultVehicle) {
      reservationForm.vehicleId = vehicleStore.defaultVehicle.id
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.reservation-create-container {
  padding: 20px;
  max-width: 900px;
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

.reservation-form {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pile-option {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pile-code {
  font-weight: 600;
  color: #303133;
  min-width: 100px;
}

.pile-location {
  flex: 1;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vehicle-option {
  display: flex;
  align-items: center;
  gap: 12px;
}

.license-plate {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  letter-spacing: 1px;
  color: #303133;
  min-width: 100px;
}

.vehicle-brand,
.vehicle-model {
  color: #606266;
}
</style>
