<template>
  <div class="statistics-dashboard">
    <section class="dashboard-hero">
      <div>
        <p class="hero-tag">Overview</p>
        <h1 class="hero-title">运营统计看板</h1>
        <p class="hero-description">
          聚合充电桩使用率、营收表现、活跃用户和充电次数，作为管理端默认首页。
        </p>
      </div>

      <el-card class="toolbar-card" shadow="never">
        <div class="toolbar-grid">
          <div class="toolbar-main">
            <el-radio-group v-model="rangeType" @change="handleRangeChange">
              <el-radio-button
                v-for="option in rangeOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </el-radio-button>
            </el-radio-group>

            <el-date-picker
              v-if="rangeType === 'CUSTOM'"
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
            />
          </div>

          <div class="toolbar-actions">
            <el-button type="primary" :icon="Refresh" @click="handleRefresh">刷新数据</el-button>
            <el-button type="success" :icon="Download" @click="handleExport">导出报表</el-button>
          </div>
        </div>

        <div class="toolbar-meta">
          <span>统计区间：{{ overviewPeriod }}</span>
          <span>模式：{{ rangeTypeLabel }}</span>
        </div>
      </el-card>
    </section>

    <div v-loading="statisticsStore.loading" class="dashboard-content">
      <section class="metrics-grid">
        <StatisticsCard
          title="总营收"
          :value="overview?.totalRevenue || 0"
          :icon="Money"
          icon-color="#16a34a"
          icon-bg-color="#dcfce7"
          suffix=" 元"
        />
        <StatisticsCard
          title="日均营收"
          :value="overview?.averageDailyRevenue || 0"
          :icon="TrendCharts"
          icon-color="#2563eb"
          icon-bg-color="#dbeafe"
          suffix=" 元"
        />
        <StatisticsCard
          title="总充电次数"
          :value="overview?.totalChargingCount || 0"
          :icon="Lightning"
          icon-color="#ea580c"
          icon-bg-color="#ffedd5"
        />
        <StatisticsCard
          title="活跃用户数"
          :value="overview?.activeUserCount || 0"
          :icon="User"
          icon-color="#7c3aed"
          icon-bg-color="#ede9fe"
        />
      </section>

      <section class="analysis-grid">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <h3>充电桩运行态势</h3>
                <p>查看当前统计区间内的设备总体使用情况。</p>
              </div>
              <el-tag type="primary" effect="dark">{{ usageRateText }}</el-tag>
            </div>
          </template>

          <div class="usage-overview">
            <div class="usage-ring">
              <el-progress
                type="dashboard"
                :percentage="normalizedUsageRate"
                :stroke-width="14"
                color="#2563eb"
              />
            </div>

            <div class="usage-breakdown">
              <div class="breakdown-item">
                <span class="item-label">总充电桩</span>
                <strong class="item-value">{{ overview?.totalChargingPileCount || 0 }}</strong>
              </div>
              <div class="breakdown-item">
                <span class="item-label">使用中充电桩</span>
                <strong class="item-value">{{ overview?.usedChargingPileCount || 0 }}</strong>
              </div>
              <div class="breakdown-item">
                <span class="item-label">空闲充电桩</span>
                <strong class="item-value">{{ idleChargingPileCount }}</strong>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <h3>经营摘要</h3>
                <p>用更适合后台桌面的方式展示关键运营指标。</p>
              </div>
            </div>
          </template>

          <div class="summary-list">
            <div class="summary-row">
              <span>总营收</span>
              <strong>{{ currencyText(overview?.totalRevenue) }}</strong>
            </div>
            <div class="summary-row">
              <span>日均营收</span>
              <strong>{{ currencyText(overview?.averageDailyRevenue) }}</strong>
            </div>
            <div class="summary-row">
              <span>活跃用户</span>
              <strong>{{ numberText(overview?.activeUserCount) }}</strong>
            </div>
            <div class="summary-row">
              <span>新增用户</span>
              <strong>{{ numberText(overview?.newUserCount) }}</strong>
            </div>
            <div class="summary-row">
              <span>总充电次数</span>
              <strong>{{ numberText(overview?.totalChargingCount) }}</strong>
            </div>
          </div>
        </el-card>
      </section>

      <section class="insight-grid">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <h3>管理建议</h3>
                <p>基于当前汇总数据给出简洁的运营提示。</p>
              </div>
            </div>
          </template>

          <ul class="insight-list">
            <li>
              当前充电桩使用率为 <strong>{{ usageRateText }}</strong>，可结合高峰时段继续观察设备分布。
            </li>
            <li>
              统计区间内活跃用户数为 <strong>{{ numberText(overview?.activeUserCount) }}</strong>，新增用户数为
              <strong>{{ numberText(overview?.newUserCount) }}</strong>。
            </li>
            <li>
              累计充电次数 <strong>{{ numberText(overview?.totalChargingCount) }}</strong>，说明当前平台使用保持活跃。
            </li>
          </ul>
        </el-card>

        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <h3>统计区间信息</h3>
                <p>当前看板所使用的时间范围和筛选条件。</p>
              </div>
            </div>
          </template>

          <div class="summary-list">
            <div class="summary-row">
              <span>统计模式</span>
              <strong>{{ rangeTypeLabel }}</strong>
            </div>
            <div class="summary-row">
              <span>开始时间</span>
              <strong>{{ formatDateTimeDisplay(overview?.startTime) }}</strong>
            </div>
            <div class="summary-row">
              <span>结束时间</span>
              <strong>{{ formatDateTimeDisplay(overview?.endTime) }}</strong>
            </div>
            <div class="summary-row">
              <span>导出状态</span>
              <strong>可直接导出 Excel</strong>
            </div>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataAnalysis,
  Download,
  Lightning,
  Money,
  Refresh,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import { useStatisticsStore } from '@/stores/statistics'
import StatisticsCard from '@/components/StatisticsCard.vue'
import type { StatisticsOverview } from '@/types/statistics'

type RangeType = 'TODAY' | 'WEEK' | 'MONTH' | 'CUSTOM'

const statisticsStore = useStatisticsStore()

const rangeType = ref<RangeType>('TODAY')
const dateRange = ref<[string, string] | null>(null)
const overview = ref<StatisticsOverview | null>(null)

const rangeOptions: Array<{ value: RangeType; label: string }> = [
  { value: 'TODAY', label: '今天' },
  { value: 'WEEK', label: '本周' },
  { value: 'MONTH', label: '本月' },
  { value: 'CUSTOM', label: '自定义' }
]

const rangeTypeLabel = computed(() => {
  return rangeOptions.find((item) => item.value === rangeType.value)?.label ?? '今天'
})

const normalizedUsageRate = computed(() => {
  return Math.max(0, Math.min(100, Number(overview.value?.chargingPileUsageRate ?? 0)))
})

const usageRateText = computed(() => `${normalizedUsageRate.value.toFixed(1)}%`)

const idleChargingPileCount = computed(() => {
  const total = overview.value?.totalChargingPileCount ?? 0
  const used = overview.value?.usedChargingPileCount ?? 0
  return Math.max(total - used, 0)
})

const overviewPeriod = computed(() => {
  if (!overview.value?.startTime || !overview.value?.endTime) {
    return '等待加载'
  }
  return `${overview.value.startTime} 至 ${overview.value.endTime}`
})

const formatDateTimeDisplay = (value?: string | null) => {
  if (!value) {
    return '-'
  }

  const normalized = value.replace('T', ' ')
  if (normalized.includes(' ')) {
    return normalized
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const buildQueryParams = () => {
  const params: { rangeType: RangeType; startDate?: string; endDate?: string } = {
    rangeType: rangeType.value
  }

  if (rangeType.value === 'CUSTOM' && dateRange.value) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }

  return params
}

const numberText = (value?: number | null) => {
  return Number(value ?? 0).toLocaleString()
}

const currencyText = (value?: number | null) => {
  return `${Number(value ?? 0).toLocaleString()} 元`
}

const loadData = async () => {
  try {
    overview.value = await statisticsStore.fetchOverview(buildQueryParams())
  } catch (error) {
    console.error('加载统计概览失败:', error)
    ElMessage.error('加载统计概览失败，请稍后重试。')
  }
}

const handleRangeChange = async () => {
  if (rangeType.value !== 'CUSTOM') {
    dateRange.value = null
    await loadData()
  }
}

const handleDateChange = async () => {
  if (rangeType.value === 'CUSTOM' && dateRange.value) {
    await loadData()
  }
}

const handleRefresh = async () => {
  await loadData()
}

const handleExport = async () => {
  try {
    await statisticsStore.exportData(buildQueryParams())
    ElMessage.success('统计报表导出成功。')
  } catch (error) {
    console.error('导出统计报表失败:', error)
    ElMessage.error('导出统计报表失败，请稍后重试。')
  }
}

onMounted(async () => {
  await loadData()
})
</script>

<style scoped>
.statistics-dashboard {
  width: min(100%, 1680px);
  margin: 0 auto;
  padding: 28px 20px 32px;
  box-sizing: border-box;
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(420px, 0.95fr);
  gap: 20px;
  align-items: stretch;
  margin-bottom: 24px;
}

.hero-tag {
  margin: 0 0 12px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-title {
  margin: 0;
  color: #18222f;
  font-size: 38px;
  font-weight: 700;
  line-height: 1.15;
}

.hero-description {
  max-width: 720px;
  margin: 18px 0 0;
  color: #5f7085;
  font-size: 16px;
  line-height: 1.9;
}

.toolbar-card {
  border: 1px solid #e4ebf5;
  border-radius: 20px;
}

.toolbar-grid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-main {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-meta {
  margin-top: 16px;
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  color: #6b7a8c;
  font-size: 13px;
}

.dashboard-content {
  min-height: 240px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 18px;
}

.analysis-grid,
.insight-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 18px;
}

.panel-card {
  border: 1px solid #e4ebf5;
  border-radius: 20px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.panel-header h3 {
  margin: 0;
  color: #1c2733;
  font-size: 18px;
  font-weight: 700;
}

.panel-header p {
  margin: 8px 0 0;
  color: #738395;
  font-size: 13px;
  line-height: 1.7;
}

.usage-overview {
  display: grid;
  grid-template-columns: minmax(240px, 320px) minmax(0, 1fr);
  align-items: center;
  gap: 24px;
}

.usage-ring {
  display: flex;
  justify-content: center;
}

.usage-breakdown {
  display: grid;
  gap: 14px;
}

.breakdown-item,
.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.breakdown-item {
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fbff;
  border: 1px solid #e6edf7;
}

.item-label,
.summary-row span {
  color: #637487;
  font-size: 14px;
}

.item-value,
.summary-row strong {
  color: #1b2632;
  font-size: 22px;
  font-weight: 700;
}

.summary-list {
  display: grid;
  gap: 16px;
}

.summary-row {
  padding-bottom: 14px;
  border-bottom: 1px solid #edf2f7;
}

.summary-row:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.insight-list {
  margin: 0;
  padding-left: 20px;
  color: #4c5d70;
  line-height: 1.9;
}

.insight-list li + li {
  margin-top: 10px;
}

@media (max-width: 1280px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1080px) {
  .dashboard-hero,
  .analysis-grid,
  .insight-grid,
  .usage-overview {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .statistics-dashboard {
    padding: 18px 14px 24px;
  }

  .hero-title {
    font-size: 30px;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    width: 100%;
  }

  .toolbar-actions .el-button {
    flex: 1;
  }
}
</style>
