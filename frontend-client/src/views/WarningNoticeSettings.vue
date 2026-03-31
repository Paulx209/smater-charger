<template>
  <div class="warning-settings-container">
    <el-card class="settings-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
          <span>预警设置</span>
        </div>
      </template>

      <div class="settings-content">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="150px"
          class="settings-form"
        >
          <el-form-item label="超时预警阈值" prop="threshold">
            <div class="threshold-input">
              <el-slider
                v-model="form.threshold"
                :min="10"
                :max="60"
                :step="5"
                :marks="marks"
                show-stops
                style="width: 400px"
              />
              <el-input-number
                v-model="form.threshold"
                :min="10"
                :max="60"
                :step="1"
                style="width: 120px; margin-left: 20px"
              />
              <span class="unit">分钟</span>
            </div>
            <div class="form-tip">
              当充电记录接近超时结束时，系统会按照该阈值提前发送提醒。
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSave" :loading="saving">
              保存设置
            </el-button>
            <el-button @click="handleReset">
              恢复默认
            </el-button>
          </el-form-item>
        </el-form>

        <el-divider />

        <div class="info-section">
          <h3>说明</h3>
          <ul>
            <li>
              <strong>超时预警阈值：</strong>用于控制在充电记录即将超时前，提前多少分钟发送预警通知。
            </li>
            <li>
              <strong>阈值范围：</strong>当前支持 10-60 分钟，默认 30 分钟。
            </li>
            <li>
              <strong>通知范围：</strong>设置生效后，新的提醒将按最新阈值生成。
            </li>
            <li>
              <strong>恢复默认：</strong>仅恢复当前表单值，仍需点击“保存设置”才会真正写回后端。
            </li>
          </ul>
        </div>

        <div class="example-section">
          <h3>示例</h3>
          <el-alert
            title="预警时间示例"
            :description="`如果某条充电记录预计在 12:00 结束，当前阈值为 ${form.threshold} 分钟，则系统会在 ${getExampleTime} 触发提醒。`"
            type="info"
            :closable="false"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useWarningNoticeStore } from '@/stores/warningNotice'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const warningNoticeStore = useWarningNoticeStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  threshold: 30
})

const marks = {
  10: '10分钟',
  30: '30分钟',
  60: '60分钟'
}

const rules: FormRules = {
  threshold: [
    { required: true, message: '请输入预警阈值', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: number, callback: (error?: Error) => void) => {
        if (value < 10 || value > 60) {
          callback(new Error('预警阈值必须在 10-60 分钟之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const getExampleTime = computed(() => {
  const time = new Date()
  time.setHours(12, 0, 0, 0)
  time.setMinutes(time.getMinutes() + form.threshold)
  const hours = time.getHours().toString().padStart(2, '0')
  const minutes = time.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
})

const handleBack = () => {
  navigateBack(router, '/warning-notice')
}

const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true
    await warningNoticeStore.updateThresholdConfig({
      threshold: form.threshold
    })
  } catch (error) {
    console.error('保存预警设置失败:', error)
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  form.threshold = 30
  ElMessage.info('已恢复为默认阈值 30 分钟')
}

const loadConfig = async () => {
  try {
    loading.value = true
    const threshold = await warningNoticeStore.fetchThresholdConfig()
    form.threshold = threshold
  } catch (error) {
    console.error('加载预警设置失败:', error)
    ElMessage.error('加载预警设置失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.warning-settings-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.settings-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 18px;
  font-weight: 600;
}

.settings-content {
  padding: 20px 0;
}

.settings-form {
  margin-bottom: 30px;
}

.threshold-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.unit {
  color: #606266;
  font-size: 14px;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.info-section,
.example-section {
  margin-bottom: 30px;
}

.info-section h3,
.example-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-section ul {
  margin: 0;
  padding-left: 20px;
}

.info-section li {
  margin-bottom: 12px;
  line-height: 1.8;
  color: #606266;
}

.info-section strong {
  color: #303133;
}
</style>
