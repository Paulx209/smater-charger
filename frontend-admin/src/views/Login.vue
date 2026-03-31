<template>
  <div class="login-page">
    <section class="login-hero">
      <div class="hero-content">
        <p class="hero-tag">Smart Charger Admin</p>
        <h1 class="hero-title">智能充电管理后台</h1>
        <p class="hero-description">
          统一管理价格配置、预约、充电记录、车辆、公告、预警通知与统计能力。
        </p>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-header">
          <h2 class="login-title">{{ panelTitle }}</h2>
          <p class="login-subtitle">{{ panelSubtitle }}</p>
        </div>

        <el-alert
          v-if="isRegisterRoute"
          class="login-alert"
          type="warning"
          :closable="false"
          title="管理端不开放公开注册，请联系系统管理员分配账号。"
        />

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入管理员账号"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入登录密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="login-action">
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              登录后台
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>管理端仅允许已有管理员账号登录，不提供自助注册入口。</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { type FormInstance, type FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const isRegisterRoute = computed(() => route.path === '/register')
const panelTitle = computed(() => (isRegisterRoute.value ? '管理端注册未开放' : '管理员登录'))
const panelSubtitle = computed(() =>
  isRegisterRoute.value ? '当前地址仅用于提示，不提供注册能力。' : '请输入管理员账号和密码'
)

const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6 到 20 位之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true
    await userStore.login(loginForm)
    router.push('/')
  } catch (error) {
    console.error('管理员登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(420px, 1.2fr) minmax(420px, 0.9fr);
  background:
    radial-gradient(circle at top left, rgba(64, 158, 255, 0.18), transparent 36%),
    linear-gradient(135deg, #eef4ff 0%, #f8fbff 42%, #ffffff 100%);
}

.login-hero {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 72px;
}

.hero-content {
  max-width: 560px;
}

.hero-tag {
  margin: 0 0 16px;
  color: #409eff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-title {
  margin: 0;
  color: #18222f;
  font-size: 48px;
  line-height: 1.15;
  font-weight: 700;
}

.hero-description {
  margin: 24px 0 0;
  color: #52606d;
  font-size: 18px;
  line-height: 1.8;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
}

.login-card {
  width: min(100%, 460px);
  padding: 40px 36px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(215, 225, 236, 0.9);
  border-radius: 24px;
  box-shadow: 0 24px 60px rgba(23, 42, 79, 0.12);
  backdrop-filter: blur(8px);
}

.login-header {
  margin-bottom: 28px;
}

.login-title {
  margin: 0;
  color: #18222f;
  font-size: 30px;
  font-weight: 700;
}

.login-subtitle {
  margin: 12px 0 0;
  color: #6b7a8c;
  font-size: 15px;
}

.login-alert {
  margin-bottom: 20px;
}

.login-form {
  margin-top: 8px;
}

.login-action :deep(.el-form-item__content) {
  justify-content: stretch;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.login-footer {
  margin-top: 20px;
  color: #7c8b9b;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1080px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-hero {
    padding: 48px 24px 20px;
  }

  .hero-content {
    max-width: 640px;
    text-align: center;
  }

  .hero-title {
    font-size: 40px;
  }

  .login-panel {
    padding: 12px 20px 40px;
  }
}

@media (max-width: 640px) {
  .login-page {
    min-height: 100dvh;
  }

  .login-hero {
    padding: 32px 20px 12px;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-description {
    font-size: 16px;
  }

  .login-panel {
    padding: 8px 16px 24px;
  }

  .login-card {
    width: 100%;
    padding: 28px 22px;
    border-radius: 20px;
  }

  .login-title {
    font-size: 24px;
  }
}
</style>
