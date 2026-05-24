<template>
  <div class="login-page">
    <section class="map-stage" aria-label="充电桩状态概览">
      <div class="brand-mark">
        <span class="brand-mark__icon">
          <el-icon><Lightning /></el-icon>
        </span>
        <span>智能充电桩平台</span>
      </div>

      <div class="map-grid" />
      <div class="map-landmark map-landmark--park-a" />
      <div class="map-landmark map-landmark--park-b" />
      <div class="map-landmark map-landmark--campus" />
      <div class="map-landmark map-landmark--water" />
      <div class="map-buildings" />
      <div class="river river--one" />
      <div class="river river--two" />

      <div class="station-card station-card--one">
        <span class="station-card__icon">
          <el-icon><Lightning /></el-icon>
        </span>
        <div>
          <strong>中北大学东门站</strong>
          <p>空闲 <b>4</b> 个</p>
        </div>
        <em>快充</em>
      </div>
      <span class="pin pin--one"><el-icon><LocationFilled /></el-icon></span>

      <div class="station-card station-card--two">
        <span class="station-card__icon">
          <el-icon><Lightning /></el-icon>
        </span>
        <div>
          <strong>一道门站</strong>
          <p>空闲 <b>2</b> 个</p>
        </div>
        <em>快充</em>
      </div>
      <span class="pin pin--two"><el-icon><LocationFilled /></el-icon></span>

      <div class="station-card station-card--three station-card--warning">
        <span class="station-card__icon">
          <el-icon><Lightning /></el-icon>
        </span>
        <div>
          <strong>行知广场站</strong>
          <p>占位预警 <b>1</b> 条</p>
        </div>
        <em>快充</em>
      </div>
      <span class="pin pin--three pin--warning"><el-icon><LocationFilled /></el-icon></span>

      <div class="station-card station-card--four">
        <span class="station-card__icon">
          <el-icon><Lightning /></el-icon>
        </span>
        <div>
          <strong>龙山路快充站</strong>
          <p>空闲 <b>3</b> 个</p>
        </div>
        <em>快充</em>
      </div>
      <span class="pin pin--four"><el-icon><LocationFilled /></el-icon></span>

      <div class="map-copy">
        <h1>实时掌握充电桩状态，<br />出发前确认可用车位</h1>
      </div>

      <div class="car-scene">
        <div class="charger">
          <span />
        </div>
        <div class="cable" />
        <div class="car">
          <div class="car__roof" />
          <div class="car__body" />
          <span class="car__wheel car__wheel--front" />
          <span class="car__wheel car__wheel--back" />
        </div>
      </div>
    </section>

    <section class="login-panel" aria-label="车主登录">
      <div class="login-card">
        <div class="login-card__header">
          <h2>智能充电桩平台</h2>
          <p>车主端登录</p>
        </div>

        <div class="login-tabs" aria-hidden="true">
          <span>验证码登录</span>
          <span class="is-active">密码登录</span>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="手机号 / 用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              size="large"
              clearable
              placeholder="请输入手机号或用户名"
            >
              <template #prefix>
                <el-icon><Iphone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              size="large"
              show-password
              clearable
              placeholder="请输入密码"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="login-submit">
            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-links">
          <router-link to="/register">注册账号</router-link>
          <span>忘记密码</span>
        </div>

        <p class="login-agreement">
          <el-checkbox v-model="agreementAccepted" class="agreement-checkbox">
            <span>
              登录即表示同意
              <span class="agreement-text">《用户协议》</span>
              和
              <span class="agreement-text">《隐私政策》</span>
            </span>
          </el-checkbox>
        </p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Iphone,
  Lightning,
  LocationFilled,
  Lock
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/api/auth'

defineOptions({
  name: 'ClientLoginView'
})

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const agreementAccepted = ref(false)

const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

const loginRules: FormRules<LoginRequest> = {
  username: [{ required: true, message: '请输入手机号或用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为 6 到 20 位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    if (!agreementAccepted.value) {
      ElMessage.warning('请先勾选同意用户协议和隐私政策')
      return
    }

    loading.value = true
    await userStore.login(loginForm)
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  --teal: #00a991;
  --teal-dark: #008e7a;
  --ink: #142033;
  --muted: #7e8798;
  --line: rgba(18, 66, 86, 0.1);

  display: grid;
  grid-template-columns: minmax(600px, 1fr) minmax(430px, 540px);
  height: 100vh;
  min-height: 0;
  overflow: hidden;
  background: #eef9f8;
  color: var(--ink);
}

.map-stage {
  position: relative;
  min-height: 0;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.64), rgba(237, 250, 248, 0.9)),
    radial-gradient(circle at 14% 20%, rgba(15, 169, 145, 0.14), transparent 20%),
    radial-gradient(circle at 82% 55%, rgba(55, 204, 183, 0.14), transparent 22%);
}

.map-stage::after {
  position: absolute;
  inset: auto 0 0;
  height: 210px;
  content: '';
  background:
    linear-gradient(180deg, transparent, rgba(229, 249, 244, 0.95)),
    linear-gradient(90deg, rgba(0, 169, 145, 0.12), transparent 38%, rgba(0, 169, 145, 0.08));
  pointer-events: none;
}

.brand-mark {
  position: absolute;
  z-index: 4;
  top: 30px;
  left: 32px;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: var(--ink);
  font-size: 22px;
  font-weight: 800;
}

.brand-mark__icon {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 6px;
  color: #fff;
  background: linear-gradient(180deg, #11bf8f, #009b85);
  box-shadow: 0 10px 24px rgba(0, 169, 145, 0.22);
}

.map-grid {
  position: absolute;
  inset: -80px -60px 0 -80px;
  opacity: 0.86;
  background:
    linear-gradient(31deg, transparent 0 12%, rgba(135, 174, 197, 0.24) 12.2% 12.8%, rgba(255, 255, 255, 0.72) 12.9% 13.4%, rgba(135, 174, 197, 0.18) 13.5% 14%, transparent 14.2% 34%, rgba(135, 174, 197, 0.18) 34.2% 34.7%, transparent 35%),
    linear-gradient(146deg, transparent 0 9%, rgba(135, 174, 197, 0.22) 9.2% 9.8%, rgba(255, 255, 255, 0.72) 9.9% 10.5%, rgba(135, 174, 197, 0.16) 10.6% 11.2%, transparent 11.4% 29%, rgba(135, 174, 197, 0.18) 29.2% 29.7%, transparent 30%),
    linear-gradient(86deg, transparent 0 15%, rgba(135, 174, 197, 0.2) 15.2% 15.8%, rgba(255, 255, 255, 0.7) 15.9% 16.5%, rgba(135, 174, 197, 0.15) 16.6% 17%, transparent 17.2% 43%, rgba(135, 174, 197, 0.14) 43.2% 43.8%, transparent 44%),
    linear-gradient(8deg, transparent 0 18%, rgba(135, 174, 197, 0.14) 18.2% 18.6%, transparent 18.8% 48%, rgba(135, 174, 197, 0.14) 48.2% 48.7%, transparent 49%),
    repeating-linear-gradient(0deg, rgba(246, 252, 252, 0.58) 0 74px, rgba(224, 239, 241, 0.36) 75px 77px, rgba(246, 252, 252, 0.58) 78px 152px),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0) 0 106px, rgba(190, 217, 224, 0.32) 107px 110px, rgba(255, 255, 255, 0) 111px 214px);
}

.map-grid::before,
.map-grid::after {
  position: absolute;
  content: '';
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow:
    0 0 0 1px rgba(128, 168, 190, 0.12),
    0 0 0 7px rgba(179, 207, 218, 0.13);
}

.map-grid::before {
  top: 184px;
  left: -30px;
  width: 92%;
  height: 18px;
  transform: rotate(-13deg);
}

.map-grid::after {
  top: 408px;
  left: 44px;
  width: 78%;
  height: 15px;
  transform: rotate(22deg);
}

.map-buildings {
  position: absolute;
  inset: 74px 34px 178px 28px;
  z-index: 1;
  opacity: 0.42;
  pointer-events: none;
}

.map-buildings::before,
.map-buildings::after {
  position: absolute;
  width: 48px;
  height: 26px;
  content: '';
  border: 1px solid rgba(132, 166, 185, 0.15);
  border-radius: 4px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.55), rgba(214, 231, 234, 0.32)),
    rgba(215, 229, 232, 0.36);
  box-shadow:
    82px 24px 0 -4px rgba(205, 223, 227, 0.5),
    164px 3px 0 -6px rgba(205, 223, 227, 0.48),
    238px 38px 0 -2px rgba(205, 223, 227, 0.5),
    334px 10px 0 -7px rgba(205, 223, 227, 0.5),
    424px 44px 0 -5px rgba(205, 223, 227, 0.46),
    516px 12px 0 -3px rgba(205, 223, 227, 0.5),
    628px 54px 0 -6px rgba(205, 223, 227, 0.46),
    36px 118px 0 -5px rgba(205, 223, 227, 0.48),
    128px 146px 0 -2px rgba(205, 223, 227, 0.52),
    232px 112px 0 -7px rgba(205, 223, 227, 0.48),
    318px 152px 0 -3px rgba(205, 223, 227, 0.5),
    408px 126px 0 -6px rgba(205, 223, 227, 0.46),
    512px 160px 0 -2px rgba(205, 223, 227, 0.5),
    612px 116px 0 -5px rgba(205, 223, 227, 0.48),
    706px 146px 0 -7px rgba(205, 223, 227, 0.44),
    18px 238px 0 -4px rgba(205, 223, 227, 0.48),
    116px 274px 0 -7px rgba(205, 223, 227, 0.46),
    214px 230px 0 -2px rgba(205, 223, 227, 0.5),
    302px 278px 0 -5px rgba(205, 223, 227, 0.46),
    392px 236px 0 -3px rgba(205, 223, 227, 0.5),
    494px 282px 0 -6px rgba(205, 223, 227, 0.46),
    592px 232px 0 -2px rgba(205, 223, 227, 0.5),
    690px 272px 0 -5px rgba(205, 223, 227, 0.46),
    64px 370px 0 -3px rgba(205, 223, 227, 0.48),
    174px 406px 0 -7px rgba(205, 223, 227, 0.46),
    274px 360px 0 -4px rgba(205, 223, 227, 0.5),
    366px 396px 0 -2px rgba(205, 223, 227, 0.48),
    470px 356px 0 -6px rgba(205, 223, 227, 0.46),
    574px 400px 0 -3px rgba(205, 223, 227, 0.5),
    674px 362px 0 -7px rgba(205, 223, 227, 0.44);
  transform: rotate(-11deg);
}

.map-buildings::after {
  top: 20px;
  left: 52px;
  width: 30px;
  height: 52px;
  opacity: 0.72;
  box-shadow:
    76px 12px 0 -5px rgba(210, 226, 230, 0.52),
    156px 46px 0 -2px rgba(210, 226, 230, 0.5),
    252px 8px 0 -6px rgba(210, 226, 230, 0.48),
    350px 56px 0 -4px rgba(210, 226, 230, 0.48),
    458px 18px 0 -3px rgba(210, 226, 230, 0.5),
    558px 62px 0 -6px rgba(210, 226, 230, 0.44),
    646px 16px 0 -3px rgba(210, 226, 230, 0.48),
    36px 164px 0 -6px rgba(210, 226, 230, 0.48),
    132px 196px 0 -3px rgba(210, 226, 230, 0.52),
    236px 158px 0 -5px rgba(210, 226, 230, 0.48),
    328px 204px 0 -2px rgba(210, 226, 230, 0.5),
    428px 164px 0 -6px rgba(210, 226, 230, 0.46),
    532px 202px 0 -3px rgba(210, 226, 230, 0.48),
    636px 154px 0 -5px rgba(210, 226, 230, 0.46),
    82px 320px 0 -4px rgba(210, 226, 230, 0.48),
    188px 354px 0 -6px rgba(210, 226, 230, 0.46),
    292px 314px 0 -2px rgba(210, 226, 230, 0.5),
    388px 360px 0 -5px rgba(210, 226, 230, 0.46),
    486px 316px 0 -3px rgba(210, 226, 230, 0.5),
    594px 356px 0 -6px rgba(210, 226, 230, 0.46);
  transform: rotate(16deg);
}

.map-landmark {
  position: absolute;
  z-index: 1;
  border: 1px solid rgba(91, 174, 145, 0.1);
  opacity: 0.82;
  pointer-events: none;
}

.map-landmark--park-a {
  top: 96px;
  left: 64px;
  width: 150px;
  height: 86px;
  border-radius: 22px 52px 26px 46px;
  background: rgba(179, 227, 206, 0.34);
  transform: rotate(28deg);
}

.map-landmark--park-b {
  right: 86px;
  bottom: 284px;
  width: 168px;
  height: 92px;
  border-radius: 62px 24px 54px 30px;
  background: rgba(174, 224, 193, 0.32);
  transform: rotate(-19deg);
}

.map-landmark--campus {
  top: 402px;
  left: 210px;
  width: 182px;
  height: 108px;
  border-radius: 18px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.28), transparent),
    rgba(219, 235, 222, 0.36);
  transform: rotate(-8deg);
}

.map-landmark--water {
  top: 60px;
  right: 132px;
  width: 120px;
  height: 250px;
  border: none;
  border-radius: 999px;
  background: rgba(174, 228, 237, 0.24);
  filter: blur(1px);
  transform: rotate(-21deg);
}

.river {
  position: absolute;
  width: 740px;
  height: 90px;
  border-radius: 999px;
  background: rgba(145, 219, 231, 0.16);
  filter: blur(2px);
  transform: rotate(63deg);
}

.river--one {
  top: -40px;
  right: 210px;
}

.river--two {
  top: 260px;
  right: -250px;
}

.station-card {
  position: absolute;
  z-index: 3;
  display: grid;
  grid-template-columns: 38px 1fr;
  gap: 12px;
  width: 236px;
  min-height: 78px;
  padding: 15px 18px 12px;
  border: 1px solid rgba(0, 169, 145, 0.18);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 16px 38px rgba(24, 73, 91, 0.12);
  backdrop-filter: blur(10px);
}

.station-card::after {
  position: absolute;
  left: 50%;
  bottom: -18px;
  width: 0;
  height: 0;
  content: '';
  border-top: 18px solid rgba(255, 255, 255, 0.88);
  border-right: 16px solid transparent;
  border-left: 16px solid transparent;
  transform: translateX(-50%);
}

.station-card__icon {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 7px;
  color: #fff;
  background: linear-gradient(180deg, #16bf83, #00a991);
  box-shadow: inset 0 -8px 16px rgba(0, 91, 75, 0.14);
}

.station-card strong {
  display: block;
  font-size: 16px;
  font-weight: 800;
}

.station-card p {
  margin: 8px 0 0;
  color: #405063;
  font-size: 15px;
}

.station-card b {
  color: #00a991;
  font-size: 22px;
}

.station-card em {
  position: absolute;
  left: 16px;
  bottom: -9px;
  z-index: 2;
  padding: 2px 8px;
  border: 1px solid rgba(0, 169, 145, 0.28);
  border-radius: 6px;
  color: #00a991;
  background: #f5fffb;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.station-card--warning {
  border-color: rgba(243, 151, 15, 0.26);
}

.station-card--warning .station-card__icon {
  background: linear-gradient(180deg, #ffb22b, #f29300);
}

.station-card--warning b,
.station-card--warning em {
  color: #f29300;
}

.station-card--warning em {
  border-color: rgba(242, 147, 0, 0.3);
  background: #fffaf0;
}

.station-card--one {
  top: 132px;
  left: 32%;
}

.station-card--two {
  top: 295px;
  left: 10%;
}

.station-card--three {
  top: 330px;
  right: 11%;
}

.station-card--four {
  top: 525px;
  left: 36%;
}

.pin {
  position: absolute;
  z-index: 2;
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border-radius: 50%;
  color: #00a15f;
  background: radial-gradient(circle, rgba(0, 169, 145, 0.25), rgba(0, 169, 145, 0.06) 52%, transparent 54%);
  font-size: 46px;
}

.pin::after {
  position: absolute;
  bottom: -7px;
  width: 86px;
  height: 22px;
  content: '';
  border-radius: 50%;
  background: rgba(0, 169, 145, 0.12);
}

.pin--one {
  top: 244px;
  left: 42%;
}

.pin--two {
  top: 408px;
  left: 18%;
}

.pin--three {
  top: 440px;
  right: 21%;
}

.pin--four {
  top: 630px;
  left: 45%;
}

.pin--warning {
  color: #f29300;
  background: radial-gradient(circle, rgba(242, 147, 0, 0.25), rgba(242, 147, 0, 0.06) 52%, transparent 54%);
}

.pin--warning::after {
  background: rgba(242, 147, 0, 0.12);
}

.map-copy {
  position: absolute;
  z-index: 4;
  left: 44px;
  right: 40px;
  bottom: 82px;
}

.map-copy h1 {
  max-width: 560px;
  margin: 0;
  padding-left: 22px;
  border-left: 4px solid var(--teal);
  color: #15233a;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.36;
}

.car-scene {
  position: absolute;
  z-index: 3;
  left: 72px;
  bottom: 0;
  width: 410px;
  height: 150px;
}

.charger {
  position: absolute;
  bottom: 18px;
  left: 12px;
  width: 36px;
  height: 92px;
  border-radius: 8px;
  background: linear-gradient(180deg, #eff9fb, #a9cad3);
  box-shadow: 10px 14px 28px rgba(47, 95, 109, 0.16);
}

.charger span {
  position: absolute;
  top: 15px;
  left: 9px;
  width: 18px;
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(180deg, #13c494, #058b7a);
}

.cable {
  position: absolute;
  bottom: 49px;
  left: 42px;
  width: 170px;
  height: 66px;
  border-bottom: 4px solid #46606b;
  border-left: 4px solid transparent;
  border-radius: 0 0 0 80px;
  transform: rotate(-7deg);
}

.car {
  position: absolute;
  right: 0;
  bottom: 22px;
  width: 270px;
  height: 94px;
}

.car__roof {
  position: absolute;
  top: 4px;
  left: 54px;
  width: 130px;
  height: 55px;
  border-radius: 80px 80px 16px 18px;
  background: linear-gradient(145deg, #d8eff5, #7fa7b5);
  transform: skewX(-18deg);
}

.car__body {
  position: absolute;
  bottom: 12px;
  left: 0;
  width: 260px;
  height: 58px;
  border-radius: 60px 95px 26px 24px;
  background: linear-gradient(180deg, #f9ffff, #a9cbd6);
  box-shadow: inset 0 -14px 24px rgba(55, 105, 119, 0.18), 0 18px 32px rgba(58, 105, 119, 0.12);
}

.car__wheel {
  position: absolute;
  bottom: 0;
  width: 34px;
  height: 34px;
  border: 7px solid #244451;
  border-radius: 50%;
  background: #dbeef2;
}

.car__wheel--front {
  right: 38px;
}

.car__wheel--back {
  left: 45px;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: clamp(18px, 3vh, 28px) 34px;
  background: linear-gradient(90deg, rgba(229, 249, 247, 0.44), rgba(242, 250, 249, 0.66));
  backdrop-filter: blur(12px);
}

.login-card {
  box-sizing: border-box;
  width: min(100%, 520px);
  max-height: calc(100vh - 36px);
  min-height: 0;
  padding: clamp(34px, 6vh, 60px) 54px clamp(24px, 4vh, 38px);
  border: 1px solid rgba(28, 62, 83, 0.14);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 22px 52px rgba(19, 53, 68, 0.13);
  backdrop-filter: blur(20px);
}

.login-card__header {
  text-align: center;
}

.login-card__header h2 {
  margin: 0;
  color: #142033;
  font-size: 34px;
  font-weight: 900;
}

.login-card__header p {
  margin: 12px 0 0;
  color: #8b94a5;
  font-size: 18px;
  font-weight: 700;
}

.login-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin: clamp(28px, 4vh, 44px) 0 clamp(22px, 3vh, 32px);
  border-bottom: 1px solid #dfe6ec;
  color: #8b94a5;
  font-size: 17px;
  font-weight: 800;
  text-align: center;
}

.login-tabs span {
  position: relative;
  padding-bottom: 16px;
}

.login-tabs .is-active {
  color: var(--teal-dark);
}

.login-tabs .is-active::after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 2px;
  content: '';
  background: var(--teal);
}

.login-form :deep(.el-form-item) {
  margin-bottom: clamp(18px, 2.5vh, 24px);
}

.login-form :deep(.el-form-item__label) {
  margin-bottom: 7px;
  color: #4a5568;
  font-size: 15px;
  font-weight: 700;
}

.login-form :deep(.el-input__wrapper) {
  height: clamp(48px, 6vh, 56px);
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px #d8e0e8 inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--teal) inset, 0 0 0 4px rgba(0, 169, 145, 0.1);
}

.login-form :deep(.el-input__inner) {
  color: #1b2a3d;
  font-weight: 600;
}

.login-form :deep(.el-input__prefix) {
  color: #8b94a5;
  font-size: 20px;
}

.login-submit {
  margin-top: clamp(4px, 1vh, 10px);
}

.login-button {
  width: 100%;
  height: clamp(52px, 7vh, 60px);
  border: none;
  border-radius: 5px;
  background: linear-gradient(135deg, #00b99f 0%, #009c87 100%);
  box-shadow: 0 16px 30px rgba(0, 169, 145, 0.26);
  font-size: 21px;
  font-weight: 900;
}

.login-button:hover,
.login-button:focus {
  background: linear-gradient(135deg, #05c7ab 0%, #009c87 100%);
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 2px;
}

.login-links a,
.login-links span,
.agreement-text {
  color: var(--teal-dark);
  font-weight: 700;
  text-decoration: none;
}

.login-links a:hover {
  color: #006f61;
}

.login-agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: clamp(24px, 4vh, 40px) 0 0;
  color: #7f8898;
  font-size: 14px;
}

.agreement-checkbox {
  height: auto;
}

.agreement-checkbox :deep(.el-checkbox__label) {
  color: #7f8898;
  font-size: 14px;
  line-height: 1.7;
  white-space: normal;
}

.agreement-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  border-color: var(--teal);
  background-color: var(--teal);
}

.agreement-checkbox :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #7f8898;
}

@media (max-width: 1120px) {
  .login-page {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 100vh;
    overflow: visible;
  }

  .map-stage {
    min-height: 660px;
  }

  .login-panel {
    padding: 28px 22px 42px;
  }

  .login-card {
    max-height: none;
    min-height: auto;
    padding-top: 48px;
  }
}

@media (min-width: 1121px) and (max-height: 740px) {
  .station-card--four,
  .pin--four {
    display: none;
  }
}

@media (max-width: 760px) {
  .login-page {
    background: #f1fbfa;
  }

  .map-stage {
    min-height: 400px;
  }

  .brand-mark {
    top: 22px;
    left: 20px;
    font-size: 18px;
  }

  .station-card,
  .pin,
  .car-scene {
    display: none;
  }

  .map-copy {
    left: 24px;
    right: 24px;
    bottom: 36px;
  }

  .map-copy h1 {
    font-size: 25px;
  }

  .login-panel {
    padding: 0 16px 28px;
  }

  .login-card {
    padding: 36px 22px 30px;
    border-radius: 12px;
  }

  .login-card__header h2 {
    font-size: 27px;
  }

  .login-card__header p {
    font-size: 17px;
  }

  .login-tabs {
    margin: 34px 0 28px;
    font-size: 16px;
  }
}
</style>
