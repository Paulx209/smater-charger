<template>
  <div class="charging-pile-detail-container">
    <el-card v-loading="chargingPileStore.loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span class="header-title">充电桩详情</span>
        </div>
      </template>

      <div v-if="chargingPileStore.currentPile" class="detail-content">
        <!-- 基本信息 -->
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

        <!-- 位置信息 -->
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
              经度: {{ chargingPileStore.currentPile.lng }}, 纬度: {{ chargingPileStore.currentPile.lat }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 地图占位（可选） -->
          <div class="map-placeholder">
            <el-icon><MapLocation /></el-icon>
            <span>地图功能待集成</span>
          </div>
        </div>

        <!-- 费用信息 -->
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

        <!-- 操作按钮 -->
        <div class="section">
          <div class="action-buttons">
            <el-button
              v-if="chargingPileStore.currentPile.status === ChargingPileStatus.IDLE"
              type="success"
              size="large"
              @click="handleStartCharging"
              :loading="startingCharging"
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
            <el-alert
              :title="getStatusTip(chargingPileStore.currentPile.status)"
              type="warning"
              :closable="false"
            />
          </div>
        </div>
      </div>

      <!-- 故障报修对话框 -->
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
          <el-form-item label="故障类型" prop="faultType">
            <el-select
              v-model="faultReportForm.faultType"
              placeholder="请选择故障类型"
              style="width: 100%"
            >
              <el-option
                v-for="(text, type) in FaultTypeText"
                :key="type"
                :label="text"
                :value="type"
              />
            </el-select>
          </el-form-item>

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

          <el-form-item label="故障图片">
            <el-upload
              v-model:file-list="faultReportForm.imageList"
              :action="uploadAction"
              :headers="uploadHeaders"
              list-type="picture-card"
              :limit="3"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemoveImage"
              :before-upload="beforeUpload"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">最多上传3张图片，每张不超过2MB</div>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="faultReportDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmitFaultReport"
            :loading="submittingFaultReport"
          >
            提交报修
          </el-button>
        </template>
      </el-dialog>

      <div v-else class="empty-state">
        <el-empty description="充电桩信息不存在" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadUserFile } from 'element-plus'
import {
  ArrowLeft,
  InfoFilled,
  Location,
  MapLocation,
  Money,
  Calendar,
  Position,
  Lightning,
  Warning,
  Plus
} from '@element-plus/icons-vue'
import { useChargingPileStore } from '@/stores/chargingPile'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import { useVehicleStore } from '@/stores/vehicle'
import { usePriceConfigStore } from '@/stores/priceConfig'
import { useFaultReportStore } from '@/stores/faultReport'
import {
  ChargingPileStatus,
  ChargingPileStatusTagType
} from '@/types/chargingPile'
import { FaultType, FaultTypeText } from '@/types/faultReport'
import { getToken } from '@/utils/auth'
import PriceInfo from '@/components/PriceInfo.vue'
import PriceEstimate from '@/components/PriceEstimate.vue'

const router = useRouter()
const route = useRoute()
const chargingPileStore = useChargingPileStore()
const chargingRecordStore = useChargingRecordStore()
const vehicleStore = useVehicleStore()
const faultReportStore = useFaultReportStore()

// 开始充电状态
const startingCharging = ref(false)

// 故障报修对话框
const faultReportDialogVisible = ref(false)
const faultReportFormRef = ref<FormInstance>()
const submittingFaultReport = ref(false)

// 故障报修表单
const faultReportForm = reactive<{
  faultType: FaultType | ''
  description: string
  images: string[]
  imageList: UploadUserFile[]
}>({
  faultType: '',
  description: '',
  images: [],
  imageList: []
})

// 故障报修表单验证规则
const faultReportRules: FormRules = {
  faultType: [
    { required: true, message: '请选择故障类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入故障描述', trigger: 'blur' },
    { min: 10, max: 500, message: '故障描述长度在10-500个字符之间', trigger: 'blur' }
  ]
}

// 上传配置
const uploadAction = import.meta.env.VITE_API_BASE_URL + '/api/fault-reports/upload'
const uploadHeaders = {
  Authorization: `Bearer ${getToken()}`
}

// 返回列表
const handleBack = () => {
  router.back()
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

// 获取状态提示
const getStatusTip = (status: ChargingPileStatus) => {
  const tips: Record<ChargingPileStatus, string> = {
    [ChargingPileStatus.IDLE]: '',
    [ChargingPileStatus.CHARGING]: '该充电桩正在充电中，暂时无法预约',
    [ChargingPileStatus.FAULT]: '该充电桩故障中，暂时无法使用',
    [ChargingPileStatus.RESERVED]: '该充电桩已被预约，请选择其他充电桩',
    [ChargingPileStatus.OVERTIME]: '该充电桩超时占位中，暂时无法使用'
  }
  return tips[status]
}

// 预约充电
const handleReserve = async () => {
  const pileId = Number(route.params.id)
  router.push(`/reservations/create/${pileId}`)
}

// 开始充电
const handleStartCharging = async () => {
  if (!chargingPileStore.currentPile) return

  try {
    // 检查是否已有正在进行的充电记录
    const currentRecord = await chargingRecordStore.fetchCurrentChargingRecord()
    if (currentRecord) {
      ElMessage.warning('您已有正在进行的充电，请先结束当前充电')
      return
    }

    // 获取用户的车辆列表
    await vehicleStore.fetchMyVehicles()
    const vehicles = vehicleStore.vehicles

    // 检查是否有车辆
    if (vehicles.length === 0) {
      await ElMessageBox.confirm(
        '您还没有绑定车辆，请先添加车辆后再进行充电',
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

    // 让用户选择车辆
    const { value: selectedVehicleId } = await ElMessageBox.prompt(
      '请选择要充电的车辆',
      '选择车辆',
      {
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputType: 'select',
        inputOptions: vehicles.map(v => ({
          label: `${v.licensePlate} (${v.brand} ${v.model})`,
          value: v.id
        })),
        inputValidator: (value) => {
          if (!value) {
            return '请选择车辆'
          }
          return true
        }
      }
    )

    // 让用户输入预计充电量
    const { value: electricQuantity } = await ElMessageBox.prompt(
      '请输入预计充电量（度）',
      '充电量预估',
      {
        confirmButtonText: '查看预估费用',
        cancelButtonText: '取消',
        inputType: 'number',
        inputPlaceholder: '请输入预计充电量',
        inputValidator: (value) => {
          const num = Number(value)
          if (!value || num <= 0) {
            return '充电量必须大于0'
          }
          if (num > 999) {
            return '充电量不能超过999度'
          }
          return true
        }
      }
    )

    // 调用费用预估接口
    const priceConfigStore = usePriceConfigStore()
    const estimate = await priceConfigStore.estimateFee({
      chargingPileType: chargingPileStore.currentPile.type,
      electricQuantity: Number(electricQuantity)
    })

    // 显示费用预估并确认
    const confirmMessage = `
      <div style="text-align: left; padding: 10px;">
        <h3 style="margin-bottom: 15px;">费用预估</h3>
        <p><strong>充电桩：</strong>${chargingPileStore.currentPile.code}</p>
        <p><strong>充电桩类型：</strong>${chargingPileStore.currentPile.type === 'AC' ? 'AC（交流慢充）' : 'DC（直流快充）'}</p>
        <p><strong>预计充电量：</strong>${estimate.electricQuantity.toFixed(2)} 度</p>
        <p><strong>每度电价格：</strong>¥${estimate.pricePerKwh.toFixed(2)}/度</p>
        <p><strong>服务费：</strong>¥${estimate.serviceFee.toFixed(2)}/度</p>
        <hr style="margin: 10px 0;">
        <p><strong>电费：</strong>¥${estimate.breakdown.electricityFee.toFixed(2)}</p>
        <p><strong>服务费：</strong>¥${estimate.breakdown.serviceFee.toFixed(2)}</p>
        <p style="font-size: 18px; color: #f56c6c; margin-top: 10px;">
          <strong>预估总费用：¥${estimate.totalPrice.toFixed(2)}</strong>
        </p>
        <p style="font-size: 12px; color: #909399; margin-top: 10px;">
          * 以上为预估费用，实际费用以充电结束后的结算为准
        </p>
      </div>
    `

    await ElMessageBox.confirm(confirmMessage, '确认开始充电', {
      confirmButtonText: '确认充电',
      cancelButtonText: '取消',
      type: 'info',
      dangerouslyUseHTMLString: true
    })

    startingCharging.value = true

    // 调用开始充电接口
    const record = await chargingRecordStore.beginCharging({
      chargingPileId: chargingPileStore.currentPile.id,
      vehicleId: Number(selectedVehicleId)
    })

    ElMessage.success('开始充电成功')

    // 跳转到充电记录详情页
    router.push(`/charging-record/${record.id}`)
  } catch (error: any) {
    if (error === 'cancel' || error.message === 'cancel') {
      // 用户取消操作
      return
    }
    console.error('开始充电失败:', error)
  } finally {
    startingCharging.value = false
  }
}

// 导航到充电桩
const handleNavigate = () => {
  if (!chargingPileStore.currentPile) return

  const { lng, lat, location } = chargingPileStore.currentPile

  // 使用高德地图导航（Web端）
  const url = `https://uri.amap.com/marker?position=${lng},${lat}&name=${encodeURIComponent(location)}`
  window.open(url, '_blank')
}

// 打开故障报修对话框
const handleReportFault = () => {
  // 重置表单
  faultReportForm.faultType = ''
  faultReportForm.description = ''
  faultReportForm.images = []
  faultReportForm.imageList = []
  faultReportFormRef.value?.clearValidate()

  faultReportDialogVisible.value = true
}

// 上传成功回调
const handleUploadSuccess = (response: any, file: UploadUserFile) => {
  if (response.url) {
    faultReportForm.images.push(response.url)
  }
}

// 移除图片
const handleRemoveImage = (file: UploadUserFile) => {
  const index = faultReportForm.imageList.findIndex(f => f.uid === file.uid)
  if (index !== -1) {
    faultReportForm.images.splice(index, 1)
  }
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

// 提交故障报修
const handleSubmitFaultReport = async () => {
  if (!faultReportFormRef.value || !chargingPileStore.currentPile) return

  try {
    // 验证表单
    await faultReportFormRef.value.validate()

    submittingFaultReport.value = true

    // 提交报修
    await faultReportStore.createReport({
      pileId: chargingPileStore.currentPile.id,
      faultType: faultReportForm.faultType as FaultType,
      description: faultReportForm.description,
      images: faultReportForm.images.length > 0 ? faultReportForm.images : undefined
    })

    faultReportDialogVisible.value = false
    ElMessage.success('故障报修提交成功，我们会尽快处理')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交故障报修失败:', error)
    }
  } finally {
    submittingFaultReport.value = false
  }
}

// 组件挂载时获取充电桩详情
onMounted(async () => {
  const id = Number(route.params.id)

  if (!id || isNaN(id)) {
    ElMessage.error('充电桩ID无效')
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
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
}

.map-placeholder .el-icon {
  font-size: 48px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
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

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
