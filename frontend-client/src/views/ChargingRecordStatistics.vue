<template>
  <div class="charging-record-statistics-container">
    <el-card class="statistics-card">
      <template #header>
        <div class="card-header">
          <span>充电统计</span>
        </div>
      </template>

      <!-- 加载骨架屏 -->
      <template v-if="loading">
        <div class="skeleton-container">
          <el-skeleton :rows="2" animated class="skeleton-section" />
          <el-skeleton :rows="6" animated class="skeleton-section" />
        </div>
      </template>

      <!-- 错误状态 -->
      <template v-else-if="error">
        <el-result icon="error" title="加载失败" :sub-title="error">
          <template #extra>
            <el-button type="primary" @click="handleRetry">重试</el-button>
          </template>
        </el-result>
      </template>

      <!-- 正常内容 -->
      <template v-else>
        <!-- 时间选择器 -->
        <div class="time-selector">
          <el-form :inline="true" :model="timeForm" class="time-form">
            <el-form-item label="年份">
              <el-date-picker
                v-model="timeForm.year"
                type="year"
                placeholder="选择年份"
                format="YYYY"
                value-format="YYYY"
                :style="{ width: isMobile ? '100%' : '150px' }"
                @change="handleYearChange"
              />
            </el-form-item>

            <el-form-item label="月份">
              <el-select
                v-model="timeForm.month"
                placeholder="选择月份"
                :style="{ width: isMobile ? '100%' : '120px' }"
                @change="handleMonthChange"
              >
                <el-option
                  v-for="month in 12"
                  :key="month"
                  :label="`${month}月`"
                  :value="month"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="loadStatistics" :style="{ width: isMobile ? '100%' : 'auto' }">查询</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 年度统计 -->
        <div v-if="yearlyStatistics" class="section">
          <div class="section-title">
            <el-icon><TrendCharts /></el-icon>
            <span>{{ timeForm.year }} 年度统计</span>
          </div>

          <div class="summary-cards">
            <el-card class="summary-card">
              <div class="summary-label">总充电次数</div>
              <div class="summary-value">{{ yearlyStatistics.totalCount }} 次</div>
            </el-card>

            <el-card class="summary-card">
              <div class="summary-label">总充电量</div>
              <div class="summary-value">{{ yearlyStatistics.totalElectricQuantity.toFixed(2) }} 度</div>
            </el-card>

            <el-card class="summary-card">
              <div class="summary-label">总费用</div>
              <div class="summary-value fee">¥{{ yearlyStatistics.totalFee.toFixed(2) }}</div>
            </el-card>
          </div>

          <!-- 年度月度明细表格 -->
          <div class="statistics-table">
            <el-table :data="yearlyStatistics.records" stripe border>
              <el-table-column prop="month" label="月份" :width="isMobile ? 80 : 100">
                <template #default="{ row }">
                  {{ row.month }}月
                </template>
              </el-table-column>

              <el-table-column prop="count" label="充电次数" :width="isMobile ? 90 : 120">
                <template #default="{ row }">
                  {{ row.count }} 次
                </template>
              </el-table-column>

              <el-table-column prop="electricQuantity" label="充电量" :width="isMobile ? 100 : 150">
                <template #default="{ row }">
                  {{ row.electricQuantity.toFixed(2) }} 度
                </template>
              </el-table-column>

              <el-table-column prop="fee" label="费用">
                <template #default="{ row }">
                  <span class="fee-text">¥{{ row.fee.toFixed(2) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 月度统计 -->
        <div v-if="monthlyStatistics" class="section">
          <div class="section-title">
            <el-icon><Calendar /></el-icon>
            <span>{{ timeForm.year }} 年 {{ timeForm.month }} 月统计</span>
          </div>

          <div class="summary-cards">
            <el-card class="summary-card">
              <div class="summary-label">总充电次数</div>
              <div class="summary-value">{{ monthlyStatistics.totalCount }} 次</div>
            </el-card>

            <el-card class="summary-card">
              <div class="summary-label">总充电量</div>
              <div class="summary-value">{{ monthlyStatistics.totalElectricQuantity.toFixed(2) }} 度</div>
            </el-card>

            <el-card class="summary-card">
              <div class="summary-label">总费用</div>
              <div class="summary-value fee">¥{{ monthlyStatistics.totalFee.toFixed(2) }}</div>
            </el-card>
          </div>

          <!-- 月度日度明细表格 -->
          <div class="statistics-table">
            <el-table :data="monthlyStatistics.records" stripe border>
              <el-table-column prop="date" label="日期" :width="isMobile ? 100 : 150" />

              <el-table-column prop="count" label="充电次数" :width="isMobile ? 90 : 120">
                <template #default="{ row }">
                  {{ row.count }} 次
                </template>
              </el-table-column>

              <el-table-column prop="electricQuantity" label="充电量" :width="isMobile ? 100 : 150">
                <template #default="{ row }">
                  {{ row.electricQuantity.toFixed(2) }} 度
                </template>
              </el-table-column>

              <el-table-column prop="fee" label="费用">
                <template #default="{ row }">
                  <span class="fee-text">¥{{ row.fee.toFixed(2) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!yearlyStatistics && !monthlyStatistics" class="empty-state">
          <el-empty description="暂无统计数据，请选择年份和月份查询" />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, Calendar } from '@element-plus/icons-vue'
import { useChargingRecordStore } from '@/stores/chargingRecord'
import type {
  ChargingRecordStatisticsMonthly,
  ChargingRecordStatisticsYearly
} from '@/types/chargingRecord'

const chargingRecordStore = useChargingRecordStore()

// 加载和错误状态
const loading = ref(false)
const error = ref<string | null>(null)

// 响应式布局
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 时间表单
const timeForm = reactive({
  year: new Date().getFullYear().toString(),
  month: new Date().getMonth() + 1
})

// 统计数据
const yearlyStatistics = ref<ChargingRecordStatisticsYearly | null>(null)
const monthlyStatistics = ref<ChargingRecordStatisticsMonthly | null>(null)

// 年份改变
const handleYearChange = () => {
  yearlyStatistics.value = null
  monthlyStatistics.value = null
}

// 月份改变
const handleMonthChange = () => {
  monthlyStatistics.value = null
}

// 重试
const handleRetry = async () => {
  await loadStatistics()
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    loading.value = true
    error.value = null

    if (timeForm.year) {
      const yearData = await chargingRecordStore.fetchYearlyStatistics(Number(timeForm.year))
      yearlyStatistics.value = yearData
    }

    if (timeForm.year && timeForm.month) {
      const monthData = await chargingRecordStore.fetchMonthlyStatistics(
        Number(timeForm.year),
        timeForm.month
      )
      monthlyStatistics.value = monthData
    }
  } catch (err: any) {
    error.value = err.message || '加载统计数据失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载当前年月的统计数据
onMounted(async () => {
  window.addEventListener('resize', handleResize)
  await loadStatistics()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.charging-record-statistics-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.statistics-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.skeleton-container {
  padding: 20px 0;
}

.skeleton-section {
  margin-bottom: 30px;
}

.time-selector {
  margin-bottom: 30px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.time-form {
  margin-bottom: 0;
}

.section {
  margin-bottom: 40px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  color: #409eff;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.summary-card {
  text-align: center;
  padding: 20px;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.summary-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
}

.summary-value.fee {
  color: #f56c6c;
}

.statistics-table {
  margin-top: 20px;
}

.fee-text {
  font-weight: 600;
  color: #f56c6c;
}

.empty-state {
  padding: 60px 0;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .charging-record-statistics-container {
    padding: 12px;
  }

  .time-selector {
    padding: 12px;
  }

  .time-form {
    display: flex;
    flex-direction: column;
  }

  .time-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .summary-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .summary-card {
    padding: 16px;
  }

  .summary-value {
    font-size: 24px;
  }

  .section-title {
    font-size: 14px;
  }

  .statistics-table {
    overflow-x: auto;
  }

  .statistics-table :deep(.el-table) {
    font-size: 12px;
  }
}
</style>
