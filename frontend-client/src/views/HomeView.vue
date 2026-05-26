<template>
  <div class="home-dashboard">
    <section class="dashboard-hero">
      <article class="vehicle-card">
        <div v-if="displayVehicle" class="vehicle-card__content">
          <span class="section-kicker">当前车辆</span>
          <div class="vehicle-title-row">
            <h2>{{ displayVehicle.licensePlate }}</h2>
            <span class="default-tag">默认车辆</span>
          </div>
          <p class="vehicle-meta">{{ vehicleDescription }}</p>

          <div class="range-block">
            <span>续航里程</span>
            <strong>320<small>km</small></strong>
          </div>

          <div class="battery-row">
            <div class="battery-track">
              <span style="width: 66%" />
            </div>
            <em>电量 66%</em>
          </div>
        </div>

        <div v-else class="vehicle-empty">
          <span class="section-kicker">当前车辆</span>
          <h2>{{ vehicleEmptyTitle }}</h2>
          <p>{{ vehicleEmptyDescription }}</p>
          <el-button class="orange-button" @click="navigateTo('/vehicles')">
            {{ vehicleStore.vehicles.length > 0 ? '前去设置' : '前去绑定' }}
          </el-button>
        </div>
      </article>

      <article class="find-pile-card">
        <div>
          <span class="map-pin">
            <el-icon><Location /></el-icon>
          </span>
          <h2>快速找桩</h2>
          <p>查找附近可用充电桩</p>
          <el-button class="orange-button" @click="navigateTo('/charging-piles')">
            <el-icon><Search /></el-icon>
            去找桩
          </el-button>
        </div>
      </article>
    </section>

    <section class="charging-status-card" v-loading="chargingLoading || endingCharging">
      <div class="status-card__title">当前充电状态</div>

      <div v-if="currentRecord" class="charging-status-card__body">
        <span class="status-illustration status-illustration--active">
          <el-icon><Lightning /></el-icon>
        </span>
        <div class="status-copy">
          <h3>正在充电中</h3>
          <p>
            {{ currentRecord.pileName || '未知充电桩' }}
            <span v-if="currentRecord.vehicleLicensePlate">
              · {{ currentRecord.vehicleLicensePlate }}
            </span>
            · 已充 {{ currentDuration }}
          </p>
        </div>
        <div class="status-actions">
          <el-button plain class="outline-orange-button" @click="handleViewDetail">查看详情</el-button>
          <el-button class="orange-button" @click="handleEndCharging">结束充电</el-button>
        </div>
      </div>

      <div v-else class="charging-status-card__body">
        <span class="status-illustration">
          <el-icon><Document /></el-icon>
        </span>
        <div class="status-copy">
          <h3>暂无正在进行的充电</h3>
          <p>去查找充电桩，开启充电之旅吧</p>
        </div>
        <el-button plain class="outline-orange-button" @click="navigateTo('/charging-piles')">
          去查找充电桩
        </el-button>
      </div>
    </section>

    <section class="quick-section">
      <h2>常用功能</h2>
      <div class="quick-grid">
        <button
          v-for="item in quickActions"
          :key="item.path"
          type="button"
          class="quick-card"
          @click="navigateTo(item.path)"
        >
          <span class="quick-card__icon" :class="item.tone">
            <el-badge
              v-if="item.badge"
              :value="item.badge"
              :hidden="item.badge === 0"
              class="quick-card__badge"
            >
              <el-icon><component :is="item.icon" /></el-icon>
            </el-badge>
            <el-icon v-else><component :is="item.icon" /></el-icon>
          </span>
          <div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
        </button>
      </div>
    </section>

    <section class="bottom-grid">
      <article class="carbon-card">
        <div class="carbon-card__art">
          <span />
        </div>
        <div>
          <h2>智慧充电，温暖相伴</h2>
          <p>您已累计减少碳排放 <strong>128.6kg</strong></p>
        </div>
      </article>

      <article class="monthly-card" v-loading="monthlyLoading">
        <h2>本月充电统计</h2>
        <div class="monthly-stats">
          <div class="monthly-stat">
            <span class="monthly-stat__icon monthly-stat__icon--orange">
              <el-icon><Calendar /></el-icon>
            </span>
            <span>充电次数</span>
            <strong>{{ monthlyStats?.totalCount ?? 0 }}<small>次</small></strong>
          </div>
          <div class="monthly-stat">
            <span class="monthly-stat__icon monthly-stat__icon--green">
              <el-icon><Lightning /></el-icon>
            </span>
            <span>充电电量</span>
            <strong>{{ formatKwh(monthlyStats?.totalElectricQuantity) }}<small>kWh</small></strong>
          </div>
          <div class="monthly-stat">
            <span class="monthly-stat__icon monthly-stat__icon--pink">
              <el-icon><Money /></el-icon>
            </span>
            <span>消费金额</span>
            <strong>¥ {{ formatMoney(monthlyStats?.totalFee) }}</strong>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, type Component } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  Calendar,
  Document,
  Lightning,
  Location,
  Money,
  Search,
  Tools,
  User,
  Van,
  Warning
} from '@element-plus/icons-vue'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import { useVehicleStore } from '@/stores/vehicle'
import { useWarningNoticeStore } from '@/stores/warningNotice'
import { calculateChargingDuration, formatDuration } from '@/types/chargingRecord'
import type {
  ChargingRecordInfo,
  ChargingRecordStatisticsMonthly
} from '@/types/chargingRecord'

interface QuickAction {
  title: string
  description: string
  path: string
  icon: Component
  tone: string
  badge?: number
}

defineOptions({
  name: 'OwnerHomeDashboard'
})

const router = useRouter()
const vehicleStore = useVehicleStore()
const chargingRecordStore = useChargingRecordStore()
const warningNoticeStore = useWarningNoticeStore()

const currentRecord = ref<ChargingRecordInfo | null>(null)
const currentDuration = ref('0分钟')
const chargingLoading = ref(false)
const monthlyLoading = ref(false)
const endingCharging = ref(false)
const monthlyStats = ref<ChargingRecordStatisticsMonthly | null>(null)

let durationTimer: number | null = null

const displayVehicle = computed(() => {
  return vehicleStore.defaultVehicle ?? null
})
const vehicleEmptyTitle = computed(() => {
  return vehicleStore.vehicles.length > 0 ? '暂未设置默认车辆，前去设置' : '暂未绑定车辆，前去绑定'
})
const vehicleEmptyDescription = computed(() => {
  return vehicleStore.vehicles.length > 0
    ? '设置默认车辆后，首页会展示车牌和车辆信息。'
    : '绑定默认车辆后，首页会展示车牌和车辆信息。'
})
const vehicleDescription = computed(() => {
  if (!displayVehicle.value) return ''

  const parts = [displayVehicle.value.brand, displayVehicle.value.model].filter(Boolean)
  if (displayVehicle.value.batteryCapacity) {
    parts.push(`${displayVehicle.value.batteryCapacity}kWh`)
  }

  return parts.length > 0 ? parts.join(' · ') : '新能源车辆'
})
const quickActions = computed<QuickAction[]>(() => [
  {
    title: '充电地图',
    description: '查找附近充电站点',
    path: '/charging-piles',
    icon: Location,
    tone: 'tone-orange'
  },
  {
    title: '充电记录',
    description: '查看历史充电记录',
    path: '/charging-record',
    icon: Document,
    tone: 'tone-green'
  },
  {
    title: '我的预约',
    description: '管理预约记录',
    path: '/reservations',
    icon: Calendar,
    tone: 'tone-red'
  },
  {
    title: '车辆管理',
    description: '管理我的车辆信息',
    path: '/vehicles',
    icon: Van,
    tone: 'tone-purple'
  },
  {
    title: '预警通知',
    description: '查看占位预警和系统通知',
    path: '/warning-notice',
    icon: Bell,
    tone: 'tone-red',
    badge: warningNoticeStore.unreadCount
  },
  {
    title: '预警设置',
    description: '设置预警方式和阈值',
    path: '/warning-notice/settings',
    icon: Warning,
    tone: 'tone-blue'
  },
  {
    title: '故障报修',
    description: '报告充电桩故障',
    path: '/fault-reports',
    icon: Tools,
    tone: 'tone-yellow'
  },
  {
    title: '个人中心',
    description: '管理个人信息和账户设置',
    path: '/profile',
    icon: User,
    tone: 'tone-green'
  }
])

const navigateTo = (path: string) => {
  router.push(path)
}

const formatKwh = (value?: number) => {
  if (value === undefined || value === null) return '0'
  return Number(value).toFixed(1)
}

const formatMoney = (value?: number) => {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

const updateDuration = () => {
  if (!currentRecord.value) return
  const minutes = calculateChargingDuration(currentRecord.value.startTime)
  currentDuration.value = formatDuration(minutes)
}

const resetDurationTimer = () => {
  if (durationTimer) {
    window.clearInterval(durationTimer)
    durationTimer = null
  }
}

const loadCurrentChargingRecord = async () => {
  try {
    chargingLoading.value = true
    currentRecord.value = await chargingRecordStore.fetchCurrentChargingRecord()
    resetDurationTimer()

    if (currentRecord.value) {
      updateDuration()
      durationTimer = window.setInterval(updateDuration, 60000)
    }
  } catch (error) {
    console.error('加载当前充电记录失败:', error)
  } finally {
    chargingLoading.value = false
  }
}

const loadMonthlyStatistics = async () => {
  const now = new Date()

  try {
    monthlyLoading.value = true
    monthlyStats.value = await chargingRecordStore.fetchMonthlyStatistics(
      now.getFullYear(),
      now.getMonth() + 1
    )
  } catch (error) {
    console.error('加载本月充电统计失败:', error)
  } finally {
    monthlyLoading.value = false
  }
}

const handleViewDetail = () => {
  if (currentRecord.value) {
    router.push(`/charging-record/${currentRecord.value.id}`)
  }
}

const handleEndCharging = async () => {
  if (!currentRecord.value) return

  try {
    await ElMessageBox.confirm(
      `将结束本次充电，当前已充电约 ${currentDuration.value}。`,
      '确认结束充电',
      {
        confirmButtonText: '确认结束',
        cancelButtonText: '继续充电',
        type: 'warning'
      }
    )

    endingCharging.value = true
    await chargingRecordStore.finishCharging(currentRecord.value.id)
    await loadCurrentChargingRecord()
    await loadMonthlyStatistics()
    ElMessage.success('充电已结束')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('结束充电失败:', error)
    }
  } finally {
    endingCharging.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([
    vehicleStore.fetchMyVehicles(),
    loadCurrentChargingRecord(),
    loadMonthlyStatistics()
  ])
})

onUnmounted(() => {
  resetDurationTimer()
})
</script>

<style scoped>
.home-dashboard {
  display: flex;
  flex-direction: column;
  gap: clamp(10px, 1.6vh, 18px);
  max-width: 1360px;
}

.home-dashboard,
.home-dashboard * {
  box-sizing: border-box;
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) clamp(308px, 28vw, 392px);
  gap: clamp(18px, 2vw, 32px);
}

.vehicle-card,
.find-pile-card,
.charging-status-card,
.quick-card,
.carbon-card,
.monthly-card {
  border: 1px solid rgba(238, 188, 145, 0.52);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 46px rgba(124, 72, 32, 0.08);
}

.vehicle-card {
  position: relative;
  height: clamp(198px, 24vh, 244px);
  min-height: clamp(198px, 24vh, 244px);
  overflow: hidden;
  background-image:
    linear-gradient(90deg, rgba(255, 255, 255, 0.98) 0 43%, rgba(255, 255, 255, 0.46) 66%),
    url('/images/pic1.png');
  background-position: center, center right;
  background-size: cover, cover;
}

.vehicle-card__content,
.vehicle-empty {
  position: relative;
  z-index: 1;
  max-width: 430px;
  padding: clamp(16px, 2.2vh, 24px) 32px;
}

.section-kicker {
  display: block;
  color: #4c5565;
  font-size: 14px;
  font-weight: 700;
}

.vehicle-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.vehicle-title-row h2,
.vehicle-empty h2 {
  margin: 0;
  color: #17202f;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 0;
}

.default-tag {
  padding: 5px 10px;
  border-radius: 999px;
  color: #ff6f1a;
  background: #fff0e5;
  font-size: 12px;
  font-weight: 900;
}

.vehicle-meta,
.vehicle-empty p {
  margin: 10px 0 0;
  color: #697384;
  font-size: 14px;
}

.range-block {
  margin-top: clamp(10px, 1.8vh, 18px);
}

.range-block span {
  display: block;
  color: #4c5565;
  font-size: 14px;
  font-weight: 700;
}

.range-block strong {
  display: block;
  margin-top: 4px;
  color: #ff6f1a;
  font-size: clamp(28px, 3.4vh, 32px);
  font-weight: 900;
  line-height: 1;
}

.range-block small {
  margin-left: 4px;
  color: #182235;
  font-size: 15px;
  font-weight: 700;
}

.battery-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: clamp(8px, 1.5vh, 14px);
}

.battery-track {
  width: 180px;
  height: 7px;
  overflow: hidden;
  border-radius: 999px;
  background: #ffe1c6;
}

.battery-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #ff6f1a;
}

.battery-row em {
  color: #4c5565;
  font-size: 13px;
  font-style: normal;
  font-weight: 700;
}

.vehicle-empty .orange-button {
  margin-top: 22px;
}

.find-pile-card {
  position: relative;
  height: clamp(198px, 24vh, 244px);
  min-height: clamp(198px, 24vh, 244px);
  overflow: hidden;
  padding: clamp(22px, 3vh, 30px) 28px;
  background:
    radial-gradient(circle at 76% 26%, rgba(255, 111, 26, 0.2), transparent 10%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 247, 239, 0.86));
}

.find-pile-card::after {
  position: absolute;
  right: -24px;
  bottom: -38px;
  width: 210px;
  height: 180px;
  content: '';
  opacity: 0.48;
  background:
    linear-gradient(90deg, transparent 0 46%, rgba(255, 255, 255, 0.9) 47% 52%, transparent 53%),
    linear-gradient(0deg, transparent 0 46%, rgba(255, 255, 255, 0.9) 47% 52%, transparent 53%);
  background-size: 58px 58px;
}

.map-pin {
  position: absolute;
  right: 82px;
  top: 42px;
  color: rgba(255, 111, 26, 0.32);
  font-size: clamp(58px, 7.4vh, 76px);
}

.find-pile-card h2,
.quick-section h2,
.status-card__title,
.monthly-card h2 {
  margin: 0;
  color: #17202f;
  font-size: 20px;
  font-weight: 900;
}

.find-pile-card p {
  margin: 10px 0 clamp(18px, 2.7vh, 28px);
  color: #697384;
  font-size: 15px;
}

.orange-button,
.outline-orange-button {
  min-width: 118px;
  height: 44px;
  border-radius: 9px;
  font-weight: 900;
}

.orange-button {
  border: none;
  color: #fff;
  background: linear-gradient(135deg, #ff7c25, #ff5f00);
  box-shadow: 0 14px 26px rgba(255, 99, 0, 0.22);
}

.orange-button:hover,
.orange-button:focus {
  color: #fff;
  background: linear-gradient(135deg, #ff8b38, #ff5f00);
}

.outline-orange-button {
  border-color: #ff6f1a;
  color: #ff5f00;
  background: rgba(255, 255, 255, 0.72);
}

.charging-status-card {
  overflow: hidden;
}

.status-card__title {
  padding: clamp(10px, 1.4vh, 14px) 22px;
  border-bottom: 1px solid rgba(238, 188, 145, 0.42);
}

.charging-status-card__body {
  display: flex;
  align-items: center;
  gap: 24px;
  min-height: clamp(72px, 9vh, 82px);
  padding: clamp(8px, 1.2vh, 12px) 28px;
}

.status-illustration {
  display: grid;
  width: clamp(72px, 9vw, 96px);
  height: clamp(48px, 6vh, 62px);
  place-items: center;
  border-radius: 18px;
  color: #ff8a2a;
  background:
    radial-gradient(circle at 18% 28%, rgba(255, 138, 42, 0.16), transparent 24%),
    linear-gradient(135deg, #fff0e2, #fff8ef);
  font-size: 34px;
}

.status-illustration--active {
  color: #20a65a;
  background: linear-gradient(135deg, #edfff4, #fff8ef);
}

.status-copy {
  flex: 1;
  min-width: 0;
}

.status-copy h3 {
  margin: 0;
  color: #17202f;
  font-size: 17px;
  font-weight: 900;
}

.status-copy p {
  margin: 8px 0 0;
  color: #4c5565;
  font-size: 14px;
}

.status-actions {
  display: flex;
  gap: 12px;
}

.quick-section h2 {
  margin: 0 0 clamp(6px, 1vh, 12px) 14px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: clamp(8px, 1.2vh, 12px) 24px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 18px;
  width: 100%;
  min-height: clamp(72px, 9vh, 90px);
  padding: clamp(10px, 1.6vh, 16px) 20px;
  cursor: pointer;
  font: inherit;
  text-align: left;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.quick-card:hover,
.quick-card:focus-visible {
  border-color: rgba(255, 111, 26, 0.46);
  box-shadow: 0 20px 48px rgba(124, 72, 32, 0.12);
  transform: translateY(-3px);
}

.quick-card:focus-visible {
  outline: 3px solid rgba(255, 111, 26, 0.22);
  outline-offset: 3px;
}

.quick-card__icon {
  display: grid;
  width: clamp(44px, 5.6vh, 52px);
  height: clamp(44px, 5.6vh, 52px);
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  font-size: 32px;
}

.quick-card__badge {
  display: inline-flex;
}

.quick-card h3 {
  margin: 0;
  color: #17202f;
  font-size: 15px;
  font-weight: 900;
}

.quick-card p {
  margin: 4px 0 0;
  color: #697384;
  font-size: 13px;
  line-height: 1.5;
}

.tone-orange {
  color: #ff6f1a;
  background: #fff0e2;
}

.tone-green {
  color: #38ad52;
  background: #eefeef;
}

.tone-red {
  color: #f25656;
  background: #fff0f0;
}

.tone-purple {
  color: #7263d8;
  background: #f1efff;
}

.tone-blue {
  color: #2f7cf0;
  background: #edf5ff;
}

.tone-yellow {
  color: #e8a31f;
  background: #fff6df;
}

.bottom-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
  gap: clamp(18px, 2vw, 24px);
}

.carbon-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 28px;
  min-height: clamp(112px, 14vh, 136px);
  overflow: hidden;
  padding: clamp(14px, 2vh, 20px) 32px;
  background-image:
    linear-gradient(90deg, rgba(255, 250, 245, 0.78) 0 42%, rgba(255, 250, 245, 0.24) 100%),
    url('/images/pic2.png');
  background-position: center, center;
  background-size: cover, cover;
}

.carbon-card::after {
  display: none;
  content: '';
}

.carbon-card__art {
  position: relative;
  display: grid;
  width: clamp(64px, 8vh, 86px);
  height: clamp(64px, 8vh, 86px);
  flex: 0 0 auto;
  place-items: center;
  border-radius: 28px;
  background: transparent;
  opacity: 0;
}

.carbon-card__art span {
  width: clamp(38px, 5vh, 48px);
  height: clamp(44px, 5.8vh, 56px);
  border-radius: 80% 0 80% 18%;
  background: linear-gradient(135deg, #ff8a2a, #29b865);
  transform: rotate(18deg);
}

.carbon-card h2 {
  margin: 0;
  color: #ff5f00;
  font-size: clamp(18px, 2.4vh, 22px);
  font-weight: 900;
}

.carbon-card p {
  margin: 8px 0 0;
  color: #4c5565;
  font-size: 15px;
}

.carbon-card strong {
  color: #21a851;
  font-size: 18px;
}

.monthly-card {
  min-height: clamp(112px, 14vh, 136px);
  padding: clamp(14px, 2vh, 20px) 28px;
}

.monthly-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-top: clamp(10px, 1.8vh, 18px);
}

.monthly-stat {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 6px 14px;
  align-items: center;
}

.monthly-stat__icon {
  display: grid;
  width: clamp(38px, 5vh, 46px);
  height: clamp(38px, 5vh, 46px);
  grid-row: span 2;
  place-items: center;
  border-radius: 50%;
  font-size: 22px;
}

.monthly-stat__icon--orange {
  color: #ff6f1a;
  background: #fff0e2;
}

.monthly-stat__icon--green {
  color: #38ad52;
  background: #eefeef;
}

.monthly-stat__icon--pink {
  color: #ec5b88;
  background: #fff0f5;
}

.monthly-stat span:not(.monthly-stat__icon) {
  color: #697384;
  font-size: 14px;
}

.monthly-stat strong {
  color: #17202f;
  font-size: clamp(16px, 2vh, 18px);
  font-weight: 900;
}

.monthly-stat small {
  margin-left: 4px;
  color: #4c5565;
  font-size: 12px;
}

@media (max-height: 820px) {
  .home-dashboard {
    gap: 8px;
  }

  .vehicle-card,
  .find-pile-card {
    height: 168px;
    min-height: 168px;
  }

  .vehicle-card__content,
  .vehicle-empty {
    max-width: 390px;
    padding: 14px 24px;
  }

  .section-kicker,
  .range-block span {
    font-size: 12px;
  }

  .vehicle-title-row {
    margin-top: 8px;
  }

  .vehicle-title-row h2,
  .vehicle-empty h2 {
    font-size: 21px;
  }

  .vehicle-meta,
  .vehicle-empty p {
    margin-top: 6px;
    font-size: 12px;
  }

  .range-block {
    margin-top: 8px;
  }

  .range-block strong {
    font-size: 25px;
  }

  .battery-row {
    margin-top: 6px;
  }

  .find-pile-card {
    padding: 18px 22px;
  }

  .map-pin {
    right: 54px;
    top: 28px;
    font-size: 50px;
  }

  .find-pile-card h2,
  .quick-section h2,
  .status-card__title,
  .monthly-card h2 {
    font-size: 18px;
  }

  .find-pile-card p {
    margin: 8px 0 14px;
    font-size: 13px;
  }

  .orange-button,
  .outline-orange-button {
    min-width: 104px;
    height: 36px;
    border-radius: 8px;
  }

  .status-card__title {
    padding: 8px 20px;
  }

  .charging-status-card__body {
    gap: 18px;
    min-height: 58px;
    padding: 6px 22px;
  }

  .status-illustration {
    width: 58px;
    height: 42px;
    border-radius: 14px;
    font-size: 27px;
  }

  .status-copy h3 {
    font-size: 15px;
  }

  .status-copy p {
    margin-top: 4px;
    font-size: 12px;
  }

  .quick-section h2 {
    margin-bottom: 6px;
  }

  .quick-grid {
    gap: 8px 16px;
  }

  .quick-card {
    gap: 12px;
    min-height: 58px;
    padding: 8px 14px;
    border-radius: 12px;
  }

  .quick-card__icon {
    width: 38px;
    height: 38px;
    font-size: 25px;
  }

  .quick-card h3 {
    font-size: 13px;
  }

  .quick-card p {
    margin-top: 2px;
    font-size: 11px;
    line-height: 1.35;
  }

  .carbon-card,
  .monthly-card {
    min-height: 98px;
    padding: 12px 22px;
  }

  .carbon-card {
    gap: 18px;
  }

  .carbon-card__art {
    width: 54px;
    height: 54px;
    border-radius: 20px;
  }

  .carbon-card__art span {
    width: 32px;
    height: 38px;
  }

  .carbon-card h2 {
    font-size: 17px;
  }

  .carbon-card p {
    margin-top: 4px;
    font-size: 13px;
  }

  .monthly-stats {
    gap: 10px;
    margin-top: 10px;
  }

  .monthly-stat {
    grid-template-columns: 34px 1fr;
    gap: 4px 10px;
  }

  .monthly-stat__icon {
    width: 34px;
    height: 34px;
    font-size: 18px;
  }

  .monthly-stat span:not(.monthly-stat__icon) {
    font-size: 12px;
  }

  .monthly-stat strong {
    font-size: 15px;
  }
}
</style>
