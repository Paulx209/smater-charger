<template>
  <div class="warning-notice-settings-container" v-loading="warningNoticeStore.loading">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>全局预警阈值设置</span>
          <el-button @click="handleBack">返回列表</el-button>
        </div>
      </template>

      <el-form label-width="140px" class="settings-form" @submit.prevent>
        <el-form-item label="超时预警阈值">
          <el-input-number v-model="form.threshold" :min="10" :max="60" />
          <span class="form-hint">单位：分钟，允许范围 10 到 60。</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存设置</el-button>
          <el-button @click="loadConfig">重新加载</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useWarningNoticeStore } from '@/stores/warningNotice'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const warningNoticeStore = useWarningNoticeStore()
const form = reactive({
  threshold: 30
})

const loadConfig = async () => {
  const response = await warningNoticeStore.fetchThresholdConfig()
  form.threshold = response.threshold
}

const handleSubmit = async () => {
  await warningNoticeStore.updateThresholdConfig({ threshold: form.threshold })
}

const handleBack = () => {
  navigateBack(router, '/warning-notices')
}

onMounted(loadConfig)
</script>

<style scoped>
.warning-notice-settings-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.settings-card {
  min-height: 280px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.settings-form {
  max-width: 520px;
}

.form-hint {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}
</style>
