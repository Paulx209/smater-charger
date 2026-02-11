<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量操作"
    width="600px"
    :before-close="handleClose"
  >
    <div v-loading="loading" class="dialog-content">
      <el-alert
        title="提示"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      >
        您即将对 {{ userIds.length }} 个用户执行批量操作，请谨慎操作。
      </el-alert>

      <el-form :model="form" label-width="100px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="form.action">
            <el-radio value="enable">批量启用</el-radio>
            <el-radio value="disable">批量禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选中用户">
          <div class="user-count">
            共选中 <span class="count-number">{{ userIds.length }}</span> 个用户
          </div>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="confirm-text">
        <el-icon color="#E6A23C" size="20"><WarningFilled /></el-icon>
        <span>
          确定要{{ form.action === 'enable' ? '启用' : '禁用' }}这些用户吗？
        </span>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleConfirm"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import { useUserManagementStore } from '@/stores/userManagement'

interface Props {
  visible: boolean
  userIds: number[]
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const userManagementStore = useUserManagementStore()

const loading = ref(false)
const form = reactive({
  action: 'enable' as 'enable' | 'disable'
})

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 确认操作
const handleConfirm = async () => {
  if (props.userIds.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }

  try {
    loading.value = true
    const enabled = form.action === 'enable'

    await userManagementStore.batchUpdateStatus({
      userIds: props.userIds,
      enabled
    })

    ElMessage.success(`批量${enabled ? '启用' : '禁用'}成功`)
    emit('success')
    handleClose()
  } catch (error) {
    console.error('批量操作失败:', error)
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  form.action = 'enable'
}
</script>

<style scoped>
.dialog-content {
  padding: 10px 0;
}

.user-count {
  font-size: 14px;
  color: #606266;
}

.count-number {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  margin: 0 4px;
}

.confirm-text {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background-color: #fdf6ec;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
