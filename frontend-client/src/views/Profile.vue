<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 用户信息 -->
        <el-tab-pane label="用户信息" name="info">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户ID">{{ userInfo?.userId }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userInfo?.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userInfo?.nickname || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo?.phone }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ userInfo?.name || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="预警阈值">
              {{ userInfo?.warningThreshold ? `${userInfo.warningThreshold}分钟` : '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag :type="userInfo?.status === 1 ? 'success' : 'danger'">
                {{ userInfo?.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 编辑资料 -->
        <el-tab-pane label="编辑资料" name="edit">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="120px"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" clearable />
            </el-form-item>

            <el-form-item label="真实姓名" prop="name">
              <el-input v-model="profileForm.name" placeholder="请输入真实姓名" clearable />
            </el-form-item>

            <el-form-item label="预警阈值" prop="warningThreshold">
              <el-input-number
                v-model="profileForm.warningThreshold"
                :min="1"
                :max="120"
                placeholder="请输入预警阈值（分钟）"
              />
              <span style="margin-left: 10px; color: #999">分钟</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">
                保存修改
              </el-button>
              <el-button @click="resetProfileForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入旧密码"
                show-password
                clearable
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位）"
                show-password
                clearable
              />
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                clearable
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// 当前标签页
const activeTab = ref('info')

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 加载状态
const profileLoading = ref(false)
const passwordLoading = ref(false)

// 编辑资料表单
const profileForm = reactive({
  nickname: '',
  name: '',
  warningThreshold: null as number | null
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证确认密码
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 编辑资料验证规则
const profileRules: FormRules = {
  nickname: [{ max: 50, message: '昵称最多50个字符', trigger: 'blur' }],
  name: [{ max: 50, message: '姓名最多50个字符', trigger: 'blur' }]
}

// 修改密码验证规则
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

// 初始化资料表单
const initProfileForm = () => {
  if (userInfo.value) {
    profileForm.nickname = userInfo.value.nickname || ''
    profileForm.name = userInfo.value.name || ''
    profileForm.warningThreshold = userInfo.value.warningThreshold
  }
}

// 重置资料表单
const resetProfileForm = () => {
  initProfileForm()
  profileFormRef.value?.clearValidate()
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

// 更新资料
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()

    profileLoading.value = true

    const data = await updateProfile({
      nickname: profileForm.nickname || undefined,
      name: profileForm.name || undefined,
      warningThreshold: profileForm.warningThreshold || undefined
    })

    // 更新store中的用户信息
    userStore.updateUserInfo(data)

    ElMessage.success('资料更新成功')
  } catch (error) {
    console.error('更新资料失败:', error)
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()

    passwordLoading.value = true

    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功，请重新登录')

    // 登出并跳转到登录页
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await userStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

// 组件挂载时获取用户信息
onMounted(async () => {
  try {
    await userStore.getUserInfo()
    initProfileForm()
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
