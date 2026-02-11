<template>
  <div class="statistics-dashboard">
    <el-card class="filter-card">
      <div class="filter-bar">
        <el-radio-group v-model="rangeType" @change="handleRangeChange">
          <el-radio-button value="TODAY">今日</el-radio-button>
          <el-radio-button value="WEEK">本周</el-radio-button>
          <el-radio-button value="MONTH">本月</el-radio-button>
          <el-radio-button value="CUSTOM">自定义</el-radio-button>
        </el-radio-group>

        <el-date-picker
          v-if="rangeType === 'CUSTOM'"
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="margin-left: 16px"
          @change="handleDateChange"
        />

        <div class="filter-actions">
          <el-button type="primary" :icon="Refresh" @click="handleRefresh">刷新</el-button>
          <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        </div>
      </div>
    </el-card>

    <div v-loading="statisticsStore.loading" class="cards-grid">
      <StatisticsCard
        title="总收入"
        :value="overview?.totalRevenue || 0"
        :icon="Money"
        icon-color="#67C23A"
        icon-bg-color="#F0F9FF"
        suffix=" 元"
      />
      <StatisticsCard
        title="日均收入"
        :value="overview?.averageDailyRevenue || 0"
        :icon="TrendCharts"
        icon-color="#409EFF"
        icon-bg-color="#ECF5FF"
        suffix=" 元"
      />
      <StatisticsCard
        title="充电桩总数"
        :value="overview?.totalChargingPileCount || 0"
        :icon="Connection"
        icon-color="#909399"
        icon-bg-color="#F4F4F5"
      />
      <StatisticsCard
        title="在线充电桩"
        :value="overview?.usedChargingPileCount || 0"
        :icon="CircleCheck"
        icon-color="#67C23A"
        icon-bg-color="#F0F9FF"
      />
      <StatisticsCard
        title="充电桩使用率"
        :value="overview?.chargingPileUsageRate || 0"
        :icon="DataAnalysis"
        icon-color="#E6A23C"
        icon-bg-color="#FDF6EC"
        suffix="%"
      />
      <StatisticsCard
        title="活跃用户数"
        :value="overview?.activeUserCount || 0"
        :icon="User"
        icon-color="#409EFF"
        icon-bg-color="#ECF5FF"
      />
      <StatisticsCard
        title="新增用户数"
        :value="overview?.newUserCount || 0"
        :icon="UserFilled"
        icon-color="#67C23A"
        icon-bg-color="#F0F9FF"
      />
      <StatisticsCard
        title="充电总次数"
        :value="overview?.totalChargingCount || 0"
        :icon="Lightning"
        icon-color="#F56C6C"
        icon-bg-color="#FEF0F0"
      />
    </div>

    <!-- PLACEHOLDER_FOR_CHARTS -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Download,
  Money,
  TrendCharts,
  Connection,
  CircleCheck,
  DataAnalysis,
  User,
  UserFilled,
  Lightning
} from '@element-plus/icons-vue'
import { useStatisticsStore } from '@/stores/statistics'
import StatisticsCard from '@/components/StatisticsCard.vue'
import type { StatisticsOverview } from '@/types/statistics'

const statisticsStore = useStatisticsStore()

const rangeType = ref('TODAY')
const dateRange = ref<[Date, Date] | null>(null)
const overview = ref<StatisticsOverview | null>(null)

const handleRangeChange = () => {
  if (rangeType.value !== 'CUSTOM') {
    dateRange.value = null
    loadData()
  }
}

const handleDateChange = () => {
  if (dateRange.value) {
    loadData()
  }
}

const handleRefresh = () => {
  loadData()
}

const handleExport = async () => {
  try {
    const params: any = { rangeType: rangeType.value }
    if (rangeType.value === 'CUSTOM' && dateRange.value) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    await statisticsStore.exportData(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

const loadData = async () => {
  try {
    const params: any = { rangeType: rangeType.value }
    if (rangeType.value === 'CUSTOM' && dateRange.value) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    overview.value = await statisticsStore.fetchOverview(params)
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败，请稍后重试')
  }
}

onMounted(() => {
  loadData()
})

// PLACEHOLDER_FOR_MORE_CODE
</script>

<style scoped>
.statistics-dashboard {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}
</style>
