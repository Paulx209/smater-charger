<template>
  <div class="fault-report-detail-container">
    <el-card v-loading="faultReportStore.loading">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
          <span class="header-title">报修详情</span>
        </div>
      </template>

      <div v-if="faultReportStore.currentReport" class="detail-content">
        <div class="section">
          <div class="section-title">报修信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="报修编号">#{{ faultReportStore.currentReport.id }}</el-descriptions-item>
            <el-descriptions-item label="处理状态">
              <el-tag :type="FaultReportStatusColor[faultReportStore.currentReport.status]">
                {{ FaultReportStatusText[faultReportStore.currentReport.status] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="充电桩">
              {{ faultReportStore.currentReport.pileName || `充电桩 ${faultReportStore.currentReport.chargingPileId}` }}
            </el-descriptions-item>
            <el-descriptions-item label="充电桩类型">
              {{ formatPileType(faultReportStore.currentReport.pileType) }}
            </el-descriptions-item>
            <el-descriptions-item label="位置" :span="2">
              {{ faultReportStore.currentReport.pileLocation || '暂无位置' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ faultReportStore.currentReport.createdTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ faultReportStore.currentReport.updatedTime }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">用户信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">
              {{ faultReportStore.currentReport.userName || '未知用户' }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ faultReportStore.currentReport.userPhone || '暂无手机号' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">故障描述</div>
          <div class="description-box">{{ faultReportStore.currentReport.description }}</div>
        </div>

        <div class="section">
          <div class="section-title">处理报修</div>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="处理状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择处理状态" style="width: 220px">
                <el-option
                  v-for="(text, status) in FaultReportStatusText"
                  :key="status"
                  :label="text"
                  :value="status"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="处理备注" prop="handleRemark">
              <el-input
                v-model="form.handleRemark"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="填写处理动作、联系结果或解决说明"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit">保存处理结果</el-button>
              <el-button @click="handleBack">返回列表</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="未找到报修记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useFaultReportStore } from '@/stores/faultReport'
import {
  FaultReportStatus,
  FaultReportStatusColor,
  FaultReportStatusText,
  formatPileType
} from '@/types/faultReport'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const route = useRoute()
const faultReportStore = useFaultReportStore()
const formRef = ref<FormInstance>()

const form = reactive({
  status: undefined as FaultReportStatus | undefined,
  handleRemark: ''
})

const rules: FormRules = {
  status: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
  handleRemark: [
    { required: true, message: '请填写处理备注', trigger: 'blur' },
    { max: 500, message: '处理备注不能超过 500 个字符', trigger: 'blur' }
  ]
}

const handleBack = () => {
  navigateBack(router, '/fault-reports')
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id || Number.isNaN(id)) {
    ElMessage.error('报修 ID 无效')
    router.push('/fault-reports')
    return
  }

  try {
    const report = await faultReportStore.fetchReportDetail(id)
    form.status = report.status
    form.handleRemark = report.handleRemark || ''
  } catch (error) {
    console.error('加载报修详情失败:', error)
    router.push('/fault-reports')
  }
}

const handleSubmit = async () => {
  const id = Number(route.params.id)
  if (!id || !formRef.value) return

  await formRef.value.validate()
  await faultReportStore.submitHandle(id, {
    status: form.status as FaultReportStatus,
    handleRemark: form.handleRemark
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.fault-report-detail-container {
  padding: 20px;
  max-width: 1100px;
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
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.description-box {
  padding: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  color: #303133;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.empty-state {
  padding: 60px 0;
}
</style>
