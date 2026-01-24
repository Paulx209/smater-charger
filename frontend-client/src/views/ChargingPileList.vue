<template>
  <div class="charging-pile-list-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="位置搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入位置关键词"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="充电桩类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="交流慢充" :value="ChargingPileType.AC" />
            <el-option label="直流快充" :value="ChargingPileType.DC" />
          </el-select>
        </el-form-item>

        <el-form-item label="充电桩状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="空闲" :value="ChargingPileStatus.IDLE" />
            <el-option label="充电中" :value="ChargingPileStatus.CHARGING" />
            <el-option label="故障" :value="ChargingPileStatus.FAULT" />
            <el-option label="已预约" :value="ChargingPileStatus.RESERVED" />
            <el-option label="超时占位" :value="ChargingPileStatus.OVERTIME" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card" v-loading="chargingPileStore.loading">
      <template #header>
        <div class="card-header">
          <span>充电桩列表</span>
          <span class="total-count">共 {{ chargingPileStore.total }} 个充电桩</span>
        </div>
      </template>

      <div v-if="chargingPileStore.chargingPiles.length === 0" class="empty-state">
        <el-empty description="暂无充电桩数据" />
      </div>

      <div v-else class="pile-grid">
        <el-card
          v-for="pile in chargingPileStore.chargingPiles"
          :key="pile.id"
          class="pile-card"
          shadow="hover"
          @click="handleViewDetail(pile.id)"
        >
          <div class="pile-header">
            <div class="pile-code">{{ pile.code }}</div>
            <el-tag :type="ChargingPileStatusTagType[pile.status]" size="small">
              {{ pile.statusDesc }}
            </el-tag>
          </div>

          <div class="pile-info">
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span class="info-text">{{ pile.location }}</span>
            </div>

            <div class="info-item">
              <el-icon><Lightning /></el-icon>
              <span class="info-text">{{ pile.typeDesc }} ({{ pile.power }}kW)</span>
            </div>

            <div v-if="pile.distance !== undefined && pile.distance !== null" class="info-item">
              <el-icon><Position /></el-icon>
              <span class="info-text">距离 {{ pile.distance.toFixed(2) }}km</span>
            </div>
          </div>

          <div class="pile-actions">
            <el-button
              type="success"
              size="small"
              :disabled="pile.status !== ChargingPileStatus.IDLE"
              @click.stop="handleReserve(pile.id)"
            >
              <el-icon><Calendar /></el-icon>
              预约
            </el-button>
            <el-button type="primary" size="small" @click.stop="handleViewDetail(pile.id)">
              查看详情
            </el-button>
          </div>
        </el-card>
      </div>

      <div v-if="chargingPileStore.chargingPiles.length > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="chargingPileStore.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Location, Lightning, Position, Calendar } from '@element-plus/icons-vue'
import { useChargingPileStore } from '@/stores/chargingPile'
import {
  ChargingPileType,
  ChargingPileStatus,
  ChargingPileStatusTagType
} from '@/types/chargingPile'

const router = useRouter()
const chargingPileStore = useChargingPileStore()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: undefined as ChargingPileType | undefined,
  status: undefined as ChargingPileStatus | undefined
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索
const handleSearch = async () => {
  currentPage.value = 1
  await chargingPileStore.fetchChargingPiles({
    keyword: searchForm.keyword || undefined,
    type: searchForm.type,
    status: searchForm.status,
    page: currentPage.value,
    size: pageSize.value
  })
}

// 重置
const handleReset = async () => {
  searchForm.keyword = ''
  searchForm.type = undefined
  searchForm.status = undefined
  currentPage.value = 1
  pageSize.value = 10

  chargingPileStore.resetQueryParams()
  await chargingPileStore.fetchChargingPiles({
    page: currentPage.value,
    size: pageSize.value
  })
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/charging-piles/${id}`)
}

// 预约充电桩
const handleReserve = (id: number) => {
  router.push(`/reservations/create/${id}`)
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  pageSize.value = size
  await chargingPileStore.fetchChargingPiles({
    keyword: searchForm.keyword || undefined,
    type: searchForm.type,
    status: searchForm.status,
    page: currentPage.value,
    size: pageSize.value
  })
}

// 当前页改变
const handleCurrentChange = async (page: number) => {
  currentPage.value = page
  await chargingPileStore.fetchChargingPiles({
    keyword: searchForm.keyword || undefined,
    type: searchForm.type,
    status: searchForm.status,
    page: currentPage.value,
    size: pageSize.value
  })
}

// 组件挂载时加载数据
onMounted(async () => {
  await chargingPileStore.fetchChargingPiles({
    page: currentPage.value,
    size: pageSize.value
  })
})
</script>

<style scoped>
.charging-pile-list-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-count {
  color: #909399;
  font-size: 14px;
}

.empty-state {
  padding: 60px 0;
}

.pile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.pile-card {
  cursor: pointer;
  transition: all 0.3s;
}

.pile-card:hover {
  transform: translateY(-4px);
}

.pile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.pile-code {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.pile-info {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.info-item .el-icon {
  margin-right: 8px;
  color: #909399;
}

.info-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pile-actions {
  display: flex;
  justify-content: flex-end;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
