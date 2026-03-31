<template>
  <div class="fault-report-statistics">
    <el-card class="filter-card">
      <div class="filter-bar">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
        />

        <div class="filter-actions">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleBack">返回列表</el-button>
        </div>
      </div>
    </el-card>

    <div v-loading="faultReportStore.loading" class="cards-grid">
      <el-card class="stat-card">
        <div class="stat-title">报修总数</div>
        <div class="stat-value">{{ statistics?.totalCount || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">待处理</div>
        <div class="stat-value warning">{{ statistics?.pendingCount || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">处理中</div>
        <div class="stat-value primary">{{ statistics?.processingCount || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">已解决</div>
        <div class="stat-value success">{{ statistics?.resolvedCount || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">平均处理时长</div>
        <div class="stat-value">{{ statistics?.avgHandleTime || 0 }} 小时</div>
      </el-card>
    </div>

    <el-card class="top-piles-card" v-loading="faultReportStore.loading">
      <template #header>
        <div class="table-header">故障频次最高的充电桩</div>
      </template>

      <el-table :data="statistics?.topFaultPiles || []" stripe>
        <el-table-column prop="chargingPileId" label="充电桩 ID" width="140" />
        <el-table-column prop="pileName" label="充电桩名称" min-width="180" />
        <el-table-column prop="faultCount" label="故障次数" width="140" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useFaultReportStore } from '@/stores/faultReport'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const faultReportStore = useFaultReportStore()
const dateRange = ref<[string, string] | null>(null)

const statistics = computed(() => faultReportStore.statistics)

const loadData = async () => {
  await faultReportStore.fetchStatistics({
    startDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1]
  })
}

const handleReset = async () => {
  dateRange.value = null
  await loadData()
}

const handleBack = () => {
  navigateBack(router, '/fault-reports')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.fault-report-statistics {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card,
.top-piles-card {
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
  gap: 12px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  min-height: 140px;
}

.stat-title {
  margin-bottom: 12px;
  color: #909399;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.table-header {
  font-size: 16px;
  font-weight: 600;
}
</style>
