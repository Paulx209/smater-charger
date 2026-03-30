<template>
  <el-drawer v-model="drawerVisible" title="用户详情" size="60%" :before-close="handleClose">
    <div v-loading="loading" class="drawer-content">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户 ID">{{ userDetail?.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userDetail?.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userDetail?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userDetail?.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ userDetail?.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag v-if="userDetail" :type="getUserStatusColor(userDetail.status)">
                {{ getUserStatusText(userDetail.status) }}
              </el-tag>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="预警阈值">{{ userDetail?.warningThreshold ?? '-' }} 分钟</el-descriptions-item>
            <el-descriptions-item label="车辆数">{{ userDetail?.statistics.vehicleCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="累计充电次数">{{ userDetail?.statistics.chargingRecordCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="累计消费">{{ userDetail ? formatMoney(userDetail.statistics.totalSpent ?? 0) : '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近充电">{{ formatDateTime(userDetail?.statistics.lastChargingTime) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(userDetail?.createdTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(userDetail?.updatedTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="充电记录" name="charging">
          <el-table :data="chargingRecords" stripe style="width: 100%">
            <el-table-column prop="id" label="记录 ID" width="90" />
            <el-table-column prop="chargingPileCode" label="充电桩编码" width="140" />
            <el-table-column prop="chargingPileLocation" label="充电桩位置" min-width="160" show-overflow-tooltip />
            <el-table-column prop="vehicleLicensePlate" label="车牌号" width="130" />
            <el-table-column label="开始时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
            </el-table-column>
            <el-table-column label="结束时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
            </el-table-column>
            <el-table-column label="电量" width="110">
              <template #default="{ row }">{{ formatElectricity(row.electricQuantity) }}</template>
            </el-table-column>
            <el-table-column label="费用" width="110">
              <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="getChargingStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="chargingRecords.length === 0" class="empty-data">暂无充电记录</div>
        </el-tab-pane>

        <el-tab-pane label="预约记录" name="reservation">
          <el-table :data="reservations" stripe style="width: 100%">
            <el-table-column prop="id" label="预约 ID" width="90" />
            <el-table-column prop="chargingPileCode" label="充电桩编码" width="140" />
            <el-table-column prop="chargingPileLocation" label="充电桩位置" min-width="160" show-overflow-tooltip />
            <el-table-column label="开始时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
            </el-table-column>
            <el-table-column label="结束时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="getReservationStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="reservations.length === 0" class="empty-data">暂无预约记录</div>
        </el-tab-pane>

        <el-tab-pane label="违规记录" name="violation">
          <el-table :data="violations" stripe style="width: 100%">
            <el-table-column prop="id" label="违规 ID" width="90" />
            <el-table-column prop="chargingRecordId" label="充电记录 ID" width="120" />
            <el-table-column prop="violationType" label="违规类型" width="120" />
            <el-table-column prop="chargingPileCode" label="充电桩编码" width="140" />
            <el-table-column prop="chargingPileLocation" label="充电桩位置" min-width="160" show-overflow-tooltip />
            <el-table-column label="充电结束时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.chargingEndTime) }}</template>
            </el-table-column>
            <el-table-column prop="overtimeMinutes" label="超时分钟" width="110" />
            <el-table-column label="告警时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.warningTime) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="violations.length === 0" class="empty-data">暂无违规记录</div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserManagementStore } from '@/stores/userManagement'
import type { ChargingRecord, ReservationRecord, UserAdminResponse, ViolationRecord } from '@/types/user'
import { formatDateTime, formatElectricity, formatMoney, getUserStatusColor, getUserStatusText } from '@/types/user'

interface Props {
  visible: boolean
  userId: number | null
}

interface Emits {
  (e: 'update:visible', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const userManagementStore = useUserManagementStore()

const activeTab = ref('basic')
const loading = ref(false)
const userDetail = ref<UserAdminResponse | null>(null)
const chargingRecords = ref<ChargingRecord[]>([])
const reservations = ref<ReservationRecord[]>([])
const violations = ref<ViolationRecord[]>([])

const drawerVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})

const getChargingStatusType = (status: string) => {
  const statusMap: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    CHARGING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return statusMap[status] || 'info'
}

const getReservationStatusType = (status: string) => {
  const statusMap: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    PENDING: 'warning',
    CONFIRMED: 'success',
    CANCELLED: 'info',
    EXPIRED: 'danger',
    COMPLETED: 'success'
  }
  return statusMap[status] || 'info'
}

const loadUserDetail = async () => {
  if (!props.userId) return
  try {
    loading.value = true
    userDetail.value = await userManagementStore.fetchUserDetail(props.userId)
  } catch {
    ElMessage.error('加载用户详情失败')
  } finally {
    loading.value = false
  }
}

const loadChargingRecords = async () => {
  if (!props.userId) return
  try {
    loading.value = true
    const response = await userManagementStore.fetchChargingRecords(props.userId, { page: 1, size: 10 })
    chargingRecords.value = response.content
  } catch {
    ElMessage.error('加载充电记录失败')
  } finally {
    loading.value = false
  }
}

const loadReservations = async () => {
  if (!props.userId) return
  try {
    loading.value = true
    const response = await userManagementStore.fetchReservations(props.userId, { page: 1, size: 10 })
    reservations.value = response.content
  } catch {
    ElMessage.error('加载预约记录失败')
  } finally {
    loading.value = false
  }
}

const loadViolations = async () => {
  if (!props.userId) return
  try {
    loading.value = true
    const response = await userManagementStore.fetchViolations(props.userId, { page: 1, size: 10 })
    violations.value = response.content
  } catch {
    ElMessage.error('加载违规记录失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = async (tabName: string | number) => {
  switch (tabName) {
    case 'basic':
      await loadUserDetail()
      break
    case 'charging':
      await loadChargingRecords()
      break
    case 'reservation':
      await loadReservations()
      break
    case 'violation':
      await loadViolations()
      break
  }
}

const handleClose = () => {
  drawerVisible.value = false
  activeTab.value = 'basic'
  userDetail.value = null
  chargingRecords.value = []
  reservations.value = []
  violations.value = []
}

watch(
  () => [props.visible, props.userId] as const,
  async ([visible, userId]) => {
    if (visible && userId) {
      activeTab.value = 'basic'
      await loadUserDetail()
    }
  }
)
</script>

<style scoped>
.drawer-content {
  padding: 20px;
}

.empty-data {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}
</style>