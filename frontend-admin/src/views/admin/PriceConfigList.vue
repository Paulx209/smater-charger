<template>
  <div class="price-config-list-container">
    <el-card class="list-card" v-loading="priceConfigStore.loading">
      <template #header>
        <div class="card-header">
          <span>费用配置管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="充电桩类型">
            <el-select
              v-model="filterForm.chargingPileType"
              placeholder="全部类型"
              clearable
              style="width: 150px"
              @change="handleFilter"
            >
              <el-option label="AC交流慢充" value="AC" />
              <el-option label="DC直流快充" value="DC" />
            </el-select>
          </el-form-item>

          <el-form-item label="激活状态">
            <el-select
              v-model="filterForm.isActive"
              placeholder="全部状态"
              clearable
              style="width: 150px"
              @change="handleFilter"
            >
              <el-option label="激活" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 费用配置列表 -->
      <el-table
        :data="priceConfigStore.priceConfigs"
        stripe
        style="width: 100%"
        class="price-config-table"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="充电桩类型" width="140">
          <template #default="{ row }">
            <el-tag :type="row.chargingPileType === 'AC' ? 'success' : 'warning'">
              {{ getTypeText(row.chargingPileType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="每度电价格" width="130">
          <template #default="{ row }">
            {{ formatUnitPrice(row.pricePerKwh) }}
          </template>
        </el-table-column>

        <el-table-column label="服务费" width="130">
          <template #default="{ row }">
            {{ formatUnitPrice(row.serviceFee) }}
          </template>
        </el-table-column>

        <el-table-column label="总计" width="130">
          <template #default="{ row }">
            <span class="total-price">
              {{ formatUnitPrice(calculateTotal(row.pricePerKwh, row.serviceFee)) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="生效时间范围" min-width="300">
          <template #default="{ row }">
            {{ formatTimeRange(row.startTime, row.endTime) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.isActive)">
              {{ getStatusText(row.isActive) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row.id)"
            >
              编辑
            </el-button>
            <el-button
              :type="row.isActive === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleActive(row)"
            >
              {{ row.isActive === 1 ? '停用' : '激活' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="priceConfigStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="priceConfigStore.currentPage"
          v-model:page-size="priceConfigStore.pageSize"
          :total="priceConfigStore.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { usePriceConfigStore } from '@/stores/priceConfig'
import {
  ChargingPileTypeText,
  formatUnitPrice,
  formatTimeRange,
  getActiveStatusTagType,
  getActiveStatusText,
  calculateTotalUnitPrice,
  type ChargingPileType,
  type PriceConfigInfo
} from '@/types/priceConfig'

const router = useRouter()
const priceConfigStore = usePriceConfigStore()

// 筛选表单
const filterForm = reactive({
  chargingPileType: undefined as ChargingPileType | undefined,
  isActive: undefined as number | undefined
})

// 获取类型文本
const getTypeText = (type: ChargingPileType) => {
  return ChargingPileTypeText[type]
}

// 获取状态标签类型
const getStatusTagType = (isActive: number) => {
  return getActiveStatusTagType(isActive)
}

// 获取状态文本
const getStatusText = (isActive: number) => {
  return getActiveStatusText(isActive)
}

// 计算总价
const calculateTotal = (pricePerKwh: number, serviceFee: number) => {
  return calculateTotalUnitPrice(pricePerKwh, serviceFee)
}

// 新增配置
const handleCreate = () => {
  router.push('/admin/price-config/add')
}

// 编辑配置
const handleEdit = (id: number) => {
  router.push(`/admin/price-config/${id}/edit`)
}

// 切换激活状态
const handleToggleActive = async (row: PriceConfigInfo) => {
  try {
    const action = row.isActive === 1 ? '停用' : '激活'
    await ElMessageBox.confirm(
      `确定要${action}该费用配置吗？`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await priceConfigStore.toggleActive(row.id, row.isActive === 1 ? 0 : 1)
  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') {
      return
    }
    console.error('切换激活状态失败:', error)
  }
}

// 删除配置
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该费用配置吗？此操作不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await priceConfigStore.removePriceConfig(id)
  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') {
      return
    }
    console.error('删除费用配置失败:', error)
  }
}

// 筛选
const handleFilter = async () => {
  try {
    await priceConfigStore.fetchPriceConfigList({
      chargingPileType: filterForm.chargingPileType,
      isActive: filterForm.isActive
    })
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// 重置
const handleReset = async () => {
  filterForm.chargingPileType = undefined
  filterForm.isActive = undefined
  priceConfigStore.currentPage = 1
  await handleFilter()
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  priceConfigStore.pageSize = size
  await handleFilter()
}

// 页码改变
const handlePageChange = async (page: number) => {
  priceConfigStore.currentPage = page
  await handleFilter()
}

// 组件挂载时加载数据
onMounted(async () => {
  await priceConfigStore.fetchPriceConfigList()
})
</script>

<style scoped>
.price-config-list-container {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.filter-form {
  margin-bottom: 0;
}

.price-config-table {
  margin-bottom: 20px;
}

.total-price {
  font-weight: 600;
  color: #409eff;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
