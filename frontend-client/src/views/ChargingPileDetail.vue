<template>
  <div class="charging-pile-detail-container">
    <el-card v-loading="chargingPileStore.loading">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
          <span class="header-title">充电桩详情</span>
        </div>
      </template>

      <div v-if="chargingPileStore.currentPile" class="detail-content">
        <div class="section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="充电桩编号">
              <span class="pile-code">{{ chargingPileStore.currentPile.code }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="充电桩状态">
              <el-tag :type="ChargingPileStatusTagType[chargingPileStore.currentPile.status]">
                {{ chargingPileStore.currentPile.statusDesc }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="充电桩类型">
              <el-tag :type="chargingPileStore.currentPile.type === 'AC' ? 'success' : 'warning'">
                {{ chargingPileStore.currentPile.type === 'AC' ? 'AC（交流慢充）' : 'DC（直流快充）' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="充电功率">
              {{ chargingPileStore.currentPile.power }} kW
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(chargingPileStore.currentPile.createdTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" :span="2">
              {{ formatDateTime(chargingPileStore.currentPile.updatedTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">
            <el-icon><Location /></el-icon>
            <span>位置信息</span>
          </div>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="详细地址">
              {{ chargingPileStore.currentPile.location }}
            </el-descriptions-item>
            <el-descriptions-item label="经纬度">
              经度：{{ chargingPileStore.currentPile.lng }}，纬度：{{ chargingPileStore.currentPile.lat }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="map-placeholder">
            <el-icon><MapLocation /></el-icon>
            <span>地图功能待集成</span>
          </div>
        </div>

        <div class="section">
          <div class="section-title">
            <el-icon><Money /></el-icon>
            <span>费用信息</span>
          </div>

          <div class="price-components">
            <PriceInfo :charging-pile-type="chargingPileStore.currentPile.type" />
            <PriceEstimate :charging-pile-type="chargingPileStore.currentPile.type" />
          </div>
        </div>

        <div class="section">
          <div class="action-buttons">
            <el-button
              v-if="chargingPileStore.currentPile.status === ChargingPileStatus.IDLE"
              type="success"
              size="large"
              :loading="startingCharging"
              @click="handleStartCharging"
            >
              <el-icon><Lightning /></el-icon>
              开始充电
            </el-button>

            <el-button
              type="primary"
              size="large"
              :disabled="chargingPileStore.currentPile.status !== ChargingPileStatus.IDLE"
              @click="handleReserve"
            >
              <el-icon><Calendar /></el-icon>
              预约充电
            </el-button>

            <el-button size="large" @click="handleNavigate">
              <el-icon><Position /></el-icon>
              导航到此
            </el-button>

            <el-button type="danger" size="large" @click="handleReportFault">
              <el-icon><Warning /></el-icon>
              故障报修
            </el-button>
          </div>

          <div v-if="chargingPileStore.currentPile.status !== ChargingPileStatus.IDLE" class="status-tip">
            <el-alert :title="getStatusTip(chargingPileStore.currentPile.status)" type="warning" :closable="false" />
          </div>
        </div>
      </div>

      <el-dialog
        v-model="faultReportDialogVisible"
        title="故障报修"
        width="600px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="faultReportFormRef"
          :model="faultReportForm"
          :rules="faultReportRules"
          label-width="100px"
        >
          <el-form-item label="故障描述" prop="description">
            <el-input
              v-model="faultReportForm.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述遇到的故障情况"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="faultReportDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submittingFaultReport" @click="handleSubmitFaultReport">
            提交报修
          </el-button>
        </template>
      </el-dialog>

      <div v-if="!chargingPileStore.currentPile" class="empty-state">
        <el-empty description="充电桩信息不存在" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  Calendar,
  InfoFilled,
  Lightning,
  Location,
  MapLocation,
  Money,
  Position,
  Warning
} from '@element-plus/icons-vue'
import PriceEstimate from '@/components/PriceEstimate.vue'
import PriceInfo from '@/components/PriceInfo.vue'
import { useChargingPileStore } from '@/stores/chargingPile'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import { useFaultReportStore } from '@/stores/faultReport'
import { usePriceConfigStore } from '@/stores/priceConfig'
import { useVehicleStore } from '@/stores/vehicle'
import { ChargingPileStatus, ChargingPileStatusTagType } from '@/types/chargingPile'

const router = useRouter()
const route = useRoute()
const chargingPileStore = useChargingPileStore()
const chargingRecordStore = useChargingRecordStore()
const vehicleStore = useVehicleStore()
const faultReportStore = useFaultReportStore()

const startingCharging = ref(false)
const faultReportDialogVisible = ref(false)
const faultReportFormRef = ref<FormInstance>()
const submittingFaultReport = ref(false)

const faultReportForm = reactive({
  description: ''
})

const faultReportRules: FormRules = {
  description: [
    { required: true, message: '请输入故障描述', trigger: 'blur' },
    { min: 10, max: 500, message: '故障描述长度应在 10 到 500 个字符之间', trigger: 'blur' }
  ]
}

const handleBack = () => {
  router.back()
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

const getStatusTip = (status: ChargingPileStatus) => {
  const tips: Record<ChargingPileStatus, string> = {
    [ChargingPileStatus.IDLE]: '',
    [ChargingPileStatus.CHARGING]: '该充电桩正在充电中，暂时无法预约。',
    [ChargingPileStatus.FAULT]: '该充电桩故障中，暂时无法使用。',
    [ChargingPileStatus.RESERVED]: '该充电桩已被预约，请选择其他充电桩。',
    [ChargingPileStatus.OVERTIME]: '该充电桩存在超时占位，暂时无法使用。'
  }
  return tips[status]
}

const handleReserve = () => {
  const pileId = Number(route.params.id)
  router.push(`/reservations/create/${pileId}`)
}

const handleStartCharging = async () => {
  if (!chargingPileStore.currentPile) return

  try {
    const currentRecord = await chargingRecordStore.fetchCurrentChargingRecord()
    if (currentRecord) {
      ElMessage.warning('您已有进行中的充电记录，请先结束当前充电。')
      return
    }

    await vehicleStore.fetchMyVehicles()
    const vehicles = vehicleStore.vehicles

    if (vehicles.length === 0) {
      await ElMessageBox.confirm(
        '您还没有可用车辆，请先添加车辆后再开始充电。',
        '提示',
        {
          confirmButtonText: '去添加车辆',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      router.push('/vehicles/add')
      return
    }

    const selectedVehicle = vehicleStore.defaultVehicle ?? vehicles[0]

    if (!selectedVehicle) {
      ElMessage.error('未找到可用车辆')
      return
    }

    if (vehicleStore.defaultVehicle) {
      ElMessage.info(`本次将使用默认车辆 ${selectedVehicle.licensePlate}`)
    }

    const { value: electricQuantity } = await ElMessageBox.prompt(
      '请输入计划充电电量。',
      '充电预估',
      {
        confirmButtonText: '计算费用',
        cancelButtonText: '取消',
        inputType: 'number',
        inputPlaceholder: '请输入充电电量',
        inputValidator: (value) => {
          const amount = Number(value)
          if (!value || amount <= 0) return '充电电量必须大于 0'
          if (amount > 999) return '充电电量不能超过 999'
          return true
        }
      }
    )

    const priceConfigStore = usePriceConfigStore()
    const estimate = await priceConfigStore.estimateFee({
      chargingPileType: chargingPileStore.currentPile.type,
      electricQuantity: Number(electricQuantity)
    })

    const confirmMessage = `
      <div style="text-align: left; padding: 10px;">
        <h3 style="margin-bottom: 15px;">费用预估</h3>
        <p><strong>充电桩编号：</strong>${chargingPileStore.currentPile.code}</p>
        <p><strong>充电桩类型：</strong>${chargingPileStore.currentPile.type === 'AC' ? 'AC（交流慢充）' : 'DC（直流快充）'}</p>
        <p><strong>预估电量：</strong>${estimate.electricQuantity.toFixed(2)} 度</p>
        <p><strong>电价：</strong>￥${estimate.pricePerKwh.toFixed(2)}/度</p>
        <p><strong>服务费：</strong>￥${estimate.serviceFee.toFixed(2)}/度</p>
        <hr style="margin: 10px 0;">
        <p><strong>电费：</strong>￥${estimate.breakdown.electricityFee.toFixed(2)}</p>
        <p><strong>服务费合计：</strong>￥${estimate.breakdown.serviceFee.toFixed(2)}</p>
        <p style="font-size: 18px; color: #f56c6c; margin-top: 10px;">
          <strong>预估总价：￥${estimate.totalPrice.toFixed(2)}</strong>
        </p>
        <p style="font-size: 12px; color: #909399; margin-top: 10px;">
          * 实际费用以充电完成后的结算结果为准。
        </p>
      </div>
    `

    await ElMessageBox.confirm(confirmMessage, '确认开始充电', {
      confirmButtonText: '开始充电',
      cancelButtonText: '取消',
      type: 'info',
      dangerouslyUseHTMLString: true
    })

    startingCharging.value = true

    const record = await chargingRecordStore.beginCharging({
      chargingPileId: chargingPileStore.currentPile.id,
      vehicleId: selectedVehicle.id
    })

    ElMessage.success('开始充电成功')
    router.push(`/charging-record/${record.id}`)
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    console.error('开始充电失败:', error)
  } finally {
    startingCharging.value = false
  }
}

const handleNavigate = () => {
  if (!chargingPileStore.currentPile) return

  const { lng, lat, location } = chargingPileStore.currentPile
  const url = `https://uri.amap.com/marker?position=${lng},${lat}&name=${encodeURIComponent(location)}`
  window.open(url, '_blank')
}

const handleReportFault = () => {
  faultReportForm.description = ''
  faultReportFormRef.value?.clearValidate()
  faultReportDialogVisible.value = true
}

const handleSubmitFaultReport = async () => {
  if (!faultReportFormRef.value || !chargingPileStore.currentPile) return

  try {
    await faultReportFormRef.value.validate()
    submittingFaultReport.value = true

    await faultReportStore.createReport({
      chargingPileId: chargingPileStore.currentPile.id,
      description: faultReportForm.description
    })

    faultReportDialogVisible.value = false
    ElMessage.success('报修提交成功，我们会尽快处理。')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交故障报修失败:', error)
    }
  } finally {
    submittingFaultReport.value = false
  }
}

onMounted(async () => {
  const id = Number(route.params.id)

  if (!id || Number.isNaN(id)) {
    ElMessage.error('充电桩 ID 无效')
    router.push('/charging-piles')
    return
  }

  try {
    await chargingPileStore.fetchChargingPileById(id)
  } catch (error) {
    console.error('获取充电桩详情失败:', error)
    router.push('/charging-piles')
  }
})
</script>

<style scoped>
.charging-pile-detail-container {
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

.pile-code {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.map-placeholder {
  margin-top: 16px;
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.map-placeholder .el-icon {
  font-size: 48px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.status-tip {
  margin-top: 16px;
}

.price-components {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.empty-state {
  padding: 60px 0;
}
</style>
