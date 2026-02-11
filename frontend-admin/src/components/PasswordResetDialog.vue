<template>
  <el-dialog
    v-model="dialogVisible"
    title="重置密码"
    width="500px"
    :before-close="handleClose"
  >
    <div v-loading="loading" class="dialog-content">
      <el-alert
        title="提示"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        系统将为该用户生成一个临时密码，请妥善保管并通知用户修改。
      </el-alert>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">
          {{ user?.username }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ user?.email }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="newPassword" class="password-section">
        <el-divider />
        <div class="password-label">临时密码</div>
        <el-input
          v-model="newPassword"
          readonly
          class="password-input"
        >
          <template #append>
            <el-button @click="handleCopyPassword">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
          </template>
        </el-input>
        <div class="password-tip">
          请将此密码告知用户，并提醒用户登录后立即修改密码。
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="!newPassword"
          type="primary"
          :loading="loading"
          @click="handleResetPassword"
        >
          生成临时密码
        </el-button>
        <el-button
          v-else
          type="success"
          @click="handleClose"
        >
          完成
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import { useUserManagementStore } from '@/stores/userManagement'
import type { UserAdminResponse } from '@/types/userManagement'

interface Props {
  visible: boolean
  user: UserAdminResponse | null
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const userManagementStore = useUserManagementStore()

const loading = ref(false)
const newPassword = ref('')

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 重置密码
const handleResetPassword = async () => {
  if (!props.user) return

  try {
    loading.value = true
    const response = await userManagementStore.resetPassword(props.user.id, {})
    newPassword.value = response.newPassword
    ElMessage.success('密码重置成功')
  } catch (error) {
    console.error('重置密码失败:', error)
  } finally {
    loading.value = false
  }
}

// 复制密码
const handleCopyPassword = async () => {
  try {
    await navigator.clipboard.writeText(newPassword.value)
    ElMessage.success('密码已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  newPassword.value = ''
  emit('success')
}
</script>

<style scoped>
.dialog-content {
  padding: 10px 0;
}

.password-section {
  margin-top: 20px;
}

.password-label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
}

.password-input {
  margin-bottom: 10px;
}

.password-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
