<template>
  <div class="charging-pile-detail-container">
    <el-card v-loading="chargingPileStore.loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span class="header-title">充电桩详情</span>
        </div>
      </template>

      <div v-if="chargingPileStore.currentPile" class="detail-content">
        <!-- 基本信息 -->
        <div class="section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="充电桩编号">
              <span class="pile-code">{{ chargingPileStore.currentPile.code }}</span>
            </el-descriptions-item>

            <el-descriptions-item label="充电桩状态">
              <el-tag :type="ChargingPileStatusTagType[chargingPileStore.currentPile.status]">
                {{ chargingPileStore.currentPile.statusDesc }}
              </el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="充电桩类型">
              {{ chargingPileStore.currentPile.typeDesc }}
            </el-descriptions-item>

            <el-descriptions-item label="充电功率">
              {{ chargingPileStore.currentPile.power }} kW
            </el-descriptions-item>

            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(chargingPileStore.currentPile.createdTime) }}
            </el-descriptions-item>

            <el-descriptions-item label="更新时间" :span="2">
              {{ formatDateTime(chargingPileStore.currentPile.updatedTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 位置信息 -->
        <div class="section">
          <div class="section-title">
            <el-icon><Location /></el-icon>
            <span>位置信息</span>
          </div>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="详细地址">
              {{ chargingPileStore.currentPile.location }}
            </el-descriptions-item>

            <el-descriptions-item label="经纬度">
              经度: {{ chargingPileStore.currentPile.lng }}, 纬度: {{ chargingPileStore.currentPile.lat }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 地图占位（可选） -->
          <div class="map-placeholder">
            <el-icon><MapLocation /></el-icon>
            <span>地图功能待集成</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="section">
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :disabled="chargingPileStore.currentPile.status !== ChargingPileStatus.IDLE"
              @click="handleReserve"
            >
              <el-icon><Calendar /></el-icon>
              预约充电
            </el-button>

            <el-button size="large" @click="handleNavigate">
              <el-icon><Position /></el-icon>
              导航到此
            </el-button>
          </div>

          <div v-if="chargingPileStore.currentPile.status !== ChargingPileStatus.IDLE" class="status-tip">
            <el-alert
              :title="getStatusTip(chargingPileStore.currentPile.status)"
              type="warning"
              :closable="false"
            />
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="充电桩信息不存在" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  InfoFilled,
  Location,
  MapLocation,
  Calendar,
  Position
} from '@element-plus/icons-vue'
import { useChargingPileStore } from '@/stores/chargingPile'
import {
  ChargingPileStatus,
  ChargingPileStatusTagType
} from '@/types/chargingPile'

const router = useRouter()
const route = useRoute()
const chargingPileStore = useChargingPileStore()

// 返回列表
const handleBack = () => {
  router.back()
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取状态提示
const getStatusTip = (status: ChargingPileStatus) => {
  const tips: Record<ChargingPileStatus, string> = {
    [ChargingPileStatus.IDLE]: '',
    [ChargingPileStatus.CHARGING]: '该充电桩正在充电中，暂时无法预约',
    [ChargingPileStatus.FAULT]: '该充电桩故障中，暂时无法使用',
    [ChargingPileStatus.RESERVED]: '该充电桩已被预约，请选择其他充电桩',
    [ChargingPileStatus.OVERTIME]: '该充电桩超时占位中，暂时无法使用'
  }
  return tips[status]
}

// 预约充电
const handleReserve = async () => {
  try {
    await ElMessageBox.confirm('确定要预约该充电桩吗？', '预约确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    // TODO: 调用预约接口
    ElMessage.success('预约功能待实现')
  } catch (error) {
    // 用户取消
  }
}

// 导航到充电桩
const handleNavigate = () => {
  if (!chargingPileStore.currentPile) return

  const { lng, lat, location } = chargingPileStore.currentPile

  // 使用高德地图导航（Web端）
  const url = `https://uri.amap.com/marker?position=${lng},${lat}&name=${encodeURIComponent(location)}`
  window.open(url, '_blank')
}

// 组件挂载时获取充电桩详情
onMounted(async () => {
  const id = Number(route.params.id)

  if (!id || isNaN(id)) {
    ElMessage.error('充电桩ID无效')
    router.push('/charging-piles')
    return
  }

  try {
    await chargingPileStore.fetchChargingPileById(id)
  } catch (error) {
    console.error('获取充电桩详情失败:', error)
    router.push('/charging-piles')
  }
})
</script>

<style scoped>
.charging-pile-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.detail-content {
  padding: 20px 0;
}

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  color: #409eff;
}

.pile-code {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.map-placeholder {
  margin-top: 16px;
  height: 300px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
}

.map-placeholder .el-icon {
  font-size: 48px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 16px;
}

.status-tip {
  margin-top: 16px;
}

.empty-state {
  padding: 60px 0;
}
</style>
