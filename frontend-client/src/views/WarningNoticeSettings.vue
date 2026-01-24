<template>
  <div class="warning-settings-container">
    <el-card class="settings-card" v-loading="loading">
      <template #header>
        <div class="card-header">
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
          <el-form-item label="超时占位预警阈值" prop="threshold">
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
              充电完成后，如果在设定时间内未结束充电记录，将触发超时占位预警
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

        <!-- 说明信息 -->
        <el-divider />

        <div class="info-section">
          <h3>功能说明</h3>
          <ul>
            <li>
              <strong>超时占位预警：</strong>当您的充电完成后，如果在设定时间内未结束充电记录，系统将发送预警通知提醒您尽快移车。
            </li>
            <li>
              <strong>预警阈值：</strong>可设置为10-60分钟，默认为30分钟。建议根据您的实际情况设置合理的阈值。
            </li>
            <li>
              <strong>通知方式：</strong>系统将通过站内通知和短信两种方式发送预警，确保您能及时收到提醒。
            </li>
            <li>
              <strong>避免占位：</strong>及时移车不仅可以避免占用充电资源，也能为其他车主提供便利。
            </li>
          </ul>
        </div>

        <div class="example-section">
          <h3>示例</h3>
          <el-alert
            title="当前设置示例"
            :description="`如果您的充电在 12:00 完成，系统将在 ${getExampleTime} 发送预警通知。`"
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
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useWarningNoticeStore } from '@/stores/warningNotice'

const warningNoticeStore = useWarningNoticeStore()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const saving = ref(false)

// 表单数据
const form = reactive({
  threshold: 30
})

// 滑块标记
const marks = {
  10: '10分钟',
  30: '30分钟',
  60: '60分钟'
}

// 表单验证规则
const rules: FormRules = {
  threshold: [
    { required: true, message: '请设置预警阈值', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value < 10 || value > 60) {
          callback(new Error('预警阈值必须在10-60分钟之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算示例时间
const getExampleTime = computed(() => {
  const time = new Date()
  time.setHours(12, 0, 0, 0)
  time.setMinutes(time.getMinutes() + form.threshold)
  const hours = time.getHours().toString().padStart(2, '0')
  const minutes = time.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
})

// 保存设置
const handleSave = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    saving.value = true

    // 调用API保存设置
    await warningNoticeStore.updateThresholdConfig({
      threshold: form.threshold
    })
  } catch (error) {
    console.error('保存设置失败:', error)
  } finally {
    saving.value = false
  }
}

// 恢复默认
const handleReset = () => {
  form.threshold = 30
  ElMessage.info('已恢复默认设置（30分钟）')
}

// 加载当前配置
const loadConfig = async () => {
  try {
    loading.value = true
    const threshold = await warningNoticeStore.fetchThresholdConfig()
    form.threshold = threshold
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载配置
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
