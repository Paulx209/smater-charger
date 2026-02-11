<template>
  <div class="charging-pile-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Charging /></el-icon>
            充电桩管理
          </h1>
          <p class="page-subtitle">管理和监控所有充电桩设备</p>
        </div>
        <div class="action-section">
          <el-button type="primary" size="large" @click="handleCreate" class="create-btn">
            <el-icon><Plus /></el-icon>
            新增充电桩
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-card class="filter-card" shadow="never">
        <el-form :model="queryParams" inline class="filter-form">
          <el-form-item label="充电桩类型">
            <el-select
              v-model="queryParams.type"
              placeholder="请选择类型"
              clearable
              class="filter-select"
            >
              <el-option label="慢充" value="SLOW" />
              <el-option label="快充" value="FAST" />
              <el-option label="超级快充" value="SUPER_FAST" />
            </el-select>
          </el-form-item>

          <el-form-item label="充电桩状态">
            <el-select
              v-model="queryParams.status"
              placeholder="请选择状态"
              clearable
              class="filter-select"
            >
              <el-option label="可用" value="AVAILABLE" />
              <el-option label="占用中" value="OCCUPIED" />
              <el-option label="维护中" value="MAINTENANCE" />
              <el-option label="故障" value="FAULT" />
            </el-select>
          </el-form-item>

          <el-form-item label="关键词">
            <el-input
              v-model="queryParams.keyword"
              placeholder="搜索名称或位置"
              clearable
              class="filter-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 批量操作和导入导出 -->
        <div class="batch-actions">
          <el-button
            type="danger"
            plain
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除 ({{ selectedIds.length }})
          </el-button>
          <el-button type="success" plain @click="handleImport">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
          <el-button type="warning" plain @click="handleExport">
            <el-icon><Download /></el-icon>
            批量导出
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card stat-card-primary">
            <div class="stat-icon">
              <el-icon><Charging /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ total }}</div>
              <div class="stat-label">总充电桩数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-success">
            <div class="stat-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ availableCount }}</div>
              <div class="stat-label">可用充电桩</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-warning">
            <div class="stat-icon">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ maintenanceCount }}</div>
              <div class="stat-label">维护中</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-danger">
            <div class="stat-icon">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ faultCount }}</div>
              <div class="stat-label">故障设备</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card class="table-card" shadow="never">
        <el-table
          v-loading="loading"
          :data="chargingPiles"
          stripe
          class="data-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="name" label="充电桩名称" min-width="150">
            <template #default="{ row }">
              <div class="pile-name">
                <el-icon class="pile-icon"><Charging /></el-icon>
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" min-width="200" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" effect="light">
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" effect="dark">
                <el-icon class="status-icon">
                  <component :is="getStatusIcon(row.status)" />
                </el-icon>
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="power" label="功率" width="120" align="center">
            <template #default="{ row }">
              <span class="power-text">{{ row.power }} kW</span>
            </template>
          </el-table-column>
          <el-table-column prop="currentPower" label="当前功率" width="120" align="center">
            <template #default="{ row }">
              <el-progress
                :percentage="(row.currentPower / row.power) * 100"
                :color="getPowerColor(row.currentPower, row.power)"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="{ row }">
              <el-button-group class="action-buttons">
                <el-button size="small" type="primary" link @click="handleView(row.id)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button size="small" type="success" link @click="handleEdit(row.id)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button size="small" type="warning" link @click="handleStatusChange(row)">
                  <el-icon><Switch /></el-icon>
                  状态
                </el-button>
                <el-button size="small" type="danger" link @click="handleDelete(row.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSearch"
            @current-change="handleSearch"
            class="custom-pagination"
          />
        </div>
      </el-card>
    </div>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入充电桩" width="500px">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImportConfirm" :loading="loading">
          确认导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 状态修改对话框 -->
    <el-dialog v-model="statusDialogVisible" title="修改充电桩状态" width="400px">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentPile?.status)">
            {{ getStatusLabel(currentPile?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusForm.status" placeholder="请选择新状态">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="占用中" value="OCCUPIED" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="故障" value="FAULT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusConfirm" :loading="loading">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Charging,
  Plus,
  Search,
  Refresh,
  Delete,
  Upload,
  Download,
  View,
  Edit,
  Switch,
  CircleCheck,
  Tools,
  WarningFilled,
  UploadFilled
} from '@element-plus/icons-vue'
import { useChargingPileStore } from '@/stores/chargingPile'
import type { ChargingPile } from '@/api/chargingPile'

const router = useRouter()
const chargingPileStore = useChargingPileStore()

// 状态
const loading = computed(() => chargingPileStore.loading)
const chargingPiles = computed(() => chargingPileStore.chargingPiles)
const total = computed(() => chargingPileStore.total)

// 查询参数
const queryParams = ref({
  type: undefined as 'SLOW' | 'FAST' | 'SUPER_FAST' | undefined,
  status: undefined as 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'FAULT' | undefined,
  keyword: '',
  page: 1,
  size: 10
})

// 选中的ID列表
const selectedIds = ref<number[]>([])

// 导入对话框
const importDialogVisible = ref(false)
const uploadFile = ref<File | null>(null)

// 状态修改对话框
const statusDialogVisible = ref(false)
const currentPile = ref<ChargingPile | null>(null)
const statusForm = ref({
  status: '' as 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'FAULT' | ''
})

// 统计数据
const availableCount = computed(() =>
  chargingPiles.value.filter((p) => p.status === 'AVAILABLE').length
)
const maintenanceCount = computed(() =>
  chargingPiles.value.filter((p) => p.status === 'MAINTENANCE').length
)
const faultCount = computed(() =>
  chargingPiles.value.filter((p) => p.status === 'FAULT').length
)

// 初始化
onMounted(() => {
  handleSearch()
})

// 查询
const handleSearch = async () => {
  await chargingPileStore.fetchChargingPiles(queryParams.value)
}

// 重置
const handleReset = () => {
  queryParams.value = {
    type: undefined,
    status: undefined,
    keyword: '',
    page: 1,
    size: 10
  }
  handleSearch()
}

// 新增
const handleCreate = () => {
  router.push('/charging-pile/add')
}

// 查看详情
const handleView = (id: number) => {
  router.push(`/charging-pile/${id}`)
}

// 编辑
const handleEdit = (id: number) => {
  router.push(`/charging-pile/${id}/edit`)
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该充电桩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await chargingPileStore.deleteChargingPile(id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个充电桩吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await chargingPileStore.batchDeleteChargingPiles({ ids: selectedIds.value })
    ElMessage.success(`成功删除 ${result.successCount} 个充电桩`)
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除失败')
    }
  }
}

// 导入
const handleImport = () => {
  importDialogVisible.value = true
  uploadFile.value = null
}

const handleFileChange = (file: any) => {
  uploadFile.value = file.raw
}

const handleImportConfirm = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  try {
    const result = await chargingPileStore.importChargingPiles(uploadFile.value)
    ElMessage.success(`成功导入 ${result.successCount} 个充电桩`)
    importDialogVisible.value = false
    handleSearch()
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  }
}

// 导出
const handleExport = async () => {
  try {
    await chargingPileStore.exportChargingPiles({
      type: queryParams.value.type,
      status: queryParams.value.status
    })
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

// 状态修改
const handleStatusChange = (pile: ChargingPile) => {
  currentPile.value = pile
  statusForm.value.status = pile.status
  statusDialogVisible.value = true
}

const handleStatusConfirm = async () => {
  if (!currentPile.value || !statusForm.value.status) {
    return
  }

  try {
    await chargingPileStore.updateChargingPileStatus(currentPile.value.id!, {
      status: statusForm.value.status
    })
    ElMessage.success('状态修改成功')
    statusDialogVisible.value = false
    handleSearch()
  } catch (error: any) {
    ElMessage.error(error.message || '状态修改失败')
  }
}

// 表格选择
const handleSelectionChange = (selection: ChargingPile[]) => {
  selectedIds.value = selection.map((item) => item.id!).filter((id) => id !== undefined)
}

// 辅助函数
const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    SLOW: '慢充',
    FAST: '快充',
    SUPER_FAST: '超级快充'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, any> = {
    SLOW: 'info',
    FAST: 'success',
    SUPER_FAST: 'danger'
  }
  return map[type] || ''
}

const getStatusLabel = (status?: string) => {
  const map: Record<string, string> = {
    AVAILABLE: '可用',
    OCCUPIED: '占用中',
    MAINTENANCE: '维护中',
    FAULT: '故障'
  }
  return status ? map[status] || status : ''
}

const getStatusTagType = (status?: string) => {
  const map: Record<string, any> = {
    AVAILABLE: 'success',
    OCCUPIED: 'warning',
    MAINTENANCE: 'info',
    FAULT: 'danger'
  }
  return status ? map[status] || '' : ''
}

const getStatusIcon = (status: string) => {
  const map: Record<string, any> = {
    AVAILABLE: CircleCheck,
    OCCUPIED: Charging,
    MAINTENANCE: Tools,
    FAULT: WarningFilled
  }
  return map[status] || CircleCheck
}

const getPowerColor = (current: number, max: number) => {
  const percentage = (current / max) * 100
  if (percentage < 30) return '#67c23a'
  if (percentage < 70) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped lang="scss">
.charging-pile-list-container {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

// 页面头部
.page-header {
  margin-bottom: 24px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
  }

  .title-section {
    .page-title {
      display: flex;
      align-items: center;
      font-size: 32px;
      font-weight: 700;
      color: #1a1a1a;
      margin: 0 0 8px 0;

      .title-icon {
        margin-right: 12px;
        font-size: 36px;
        color: #667eea;
      }
    }

    .page-subtitle {
      font-size: 14px;
      color: #666;
      margin: 0;
      padding-left: 48px;
    }
  }

  .create-btn {
    height: 48px;
    padding: 0 32px;
    font-size: 16px;
    border-radius: 24px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
    }
  }
}

// 筛选区域
.filter-section {
  margin-bottom: 24px;

  .filter-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .filter-form {
    margin-bottom: 16px;

    .filter-select,
    .filter-input {
      width: 200px;
    }
  }

  .batch-actions {
    display: flex;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}

// 统计卡片
.stats-section {
  margin-bottom: 24px;

  .stat-card {
    display: flex;
    align-items: center;
    padding: 24px;
    border-radius: 16px;
    background: white;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }

    .stat-icon {
      width: 64px;
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16px;
      font-size: 32px;
      margin-right: 20px;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        color: #666;
      }
    }

    &.stat-card-primary {
      .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }
      .stat-value {
        color: #667eea;
      }
    }

    &.stat-card-success {
      .stat-icon {
        background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
        color: white;
      }
      .stat-value {
        color: #67c23a;
      }
    }

    &.stat-card-warning {
      .stat-icon {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: white;
      }
      .stat-value {
        color: #e6a23c;
      }
    }

    &.stat-card-danger {
      .stat-icon {
        background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
        color: white;
      }
      .stat-value {
        color: #f56c6c;
      }
    }
  }
}

// 表格区域
.table-section {
  .table-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .data-table {
    :deep(.el-table__header) {
      th {
        background: #f8f9fa;
        color: #1a1a1a;
        font-weight: 600;
      }
    }

    .pile-name {
      display: flex;
      align-items: center;
      gap: 8px;

      .pile-icon {
        color: #667eea;
      }
    }

    .power-text {
      font-weight: 600;
      color: #667eea;
    }

    .status-icon {
      margin-right: 4px;
    }

    .action-buttons {
      display: flex;
      gap: 4px;
    }
  }

  .pagination-section {
    display: flex;
    justify-content: flex-end;
    margin-top: 24px;

    .custom-pagination {
      :deep(.el-pagination__total),
      :deep(.el-pagination__jump) {
        color: #666;
      }
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);

  .el-dialog__header {
    padding: 24px 24px 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px 24px;
    border-top: 1px solid #f0f0f0;
  }
}

// 上传组件样式
:deep(.el-upload-dragger) {
  border-radius: 12px;
  border: 2px dashed #d9d9d9;
  transition: all 0.3s ease;

  &:hover {
    border-color: #667eea;
  }

  .el-icon--upload {
    font-size: 64px;
    color: #667eea;
    margin: 40px 0 16px;
  }

  .el-upload__text {
    font-size: 14px;
    color: #666;

    em {
      color: #667eea;
      font-style: normal;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .charging-pile-list-container {
    padding: 16px;
  }

  .page-header .header-content {
    flex-direction: column;
    gap: 16px;
  }

  .stats-section {
    :deep(.el-col) {
      margin-bottom: 16px;
    }
  }
}
</style>
