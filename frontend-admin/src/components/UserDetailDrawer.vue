<template>
  <el-drawer
    v-model="drawerVisible"
    title="用户详情"
    size="60%"
    :before-close="handleClose"
  >
    <div v-loading="loading" class="drawer-content">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">
              {{ userDetail?.id }}
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              {{ userDetail?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userDetail?.email }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ userDetail?.phone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="真实姓名">
              {{ userDetail?.realName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="userDetail?.enabled ? 'success' : 'danger'">
                {{ userDetail?.enabled ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">
              {{ userDetail?.createdTime ? formatDateTime(userDetail.createdTime) : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="最后登录">
              {{ userDetail?.lastLoginTime ? formatDateTime(userDetail.lastLoginTime) : '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 充电记录 -->
        <el-tab-pane label="充电记录" name="charging">
          <el-table :data="chargingRecords" stripe style="width: 100%">
            <el-table-column prop="id" label="记录ID" width="80" />
            <el-table-column prop="pileName" label="充电桩" min-width="120" />
            <el-table-column prop="pileLocation" label="位置" min-width="150" show-overflow-tooltip />
            <el-table-column prop="startTime" label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="180">
              <template #default="{ row }">
                {{ row.endTime ? formatDateTime(row.endTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="electricQuantity" label="电量(kWh)" width="110" />
            <el-table-column prop="fee" label="费用(元)" width="100">
              <template #default="{ row }">
                ¥{{ row.fee.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getChargingStatusType(row.status)">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="chargingRecords.length === 0" class="empty-data">
            暂无充电记录
          </div>
        </el-tab-pane>

        <!-- 预约记录 -->
        <el-tab-pane label="预约记录" name="reservation">
          <el-table :data="reservations" stripe style="width: 100%">
            <el-table-column prop="id" label="预约ID" width="80" />
            <el-table-column prop="pileName" label="充电桩" min-width="120" />
            <el-table-column prop="pileLocation" label="位置" min-width="150" show-overflow-tooltip />
            <el-table-column prop="startTime" label="预约开始" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="预约结束" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getReservationStatusType(row.status)">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="reservations.length === 0" class="empty-data">
            暂无预约记录
          </div>
        </el-tab-pane>

        <!-- 违规记录 -->
        <el-tab-pane label="违规记录" name="violation">
          <el-table :data="violations" stripe style="width: 100%">
            <el-table-column prop="id" label="违规ID" width="80" />
            <el-table-column prop="type" label="违规类型" width="120" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="violationTime" label="违规时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.violationTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="penaltyAmount" label="罚款金额" width="110">
              <template #default="{ row }">
                {{ row.penaltyAmount ? `¥${row.penaltyAmount.toFixed(2)}` : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="violations.length === 0" class="empty-data">
            暂无违规记录
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserManagementStore } from '@/stores/userManagement'
import type {
  UserAdminResponse,
  ChargingRecordInfo,
  ReservationInfo,
  ViolationInfo
} from '@/types/userManagement'

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
const chargingRecords = ref<ChargingRecordInfo[]>([])
const reservations = ref<ReservationInfo[]>([])
const violations = ref<ViolationInfo[]>([])

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取充电状态标签类型
const getChargingStatusType = (status: string) => {
  const statusMap: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    'CHARGING': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

// 获取预约状态标签类型
const getReservationStatusType = (status: string) => {
  const statusMap: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CANCELLED': 'info',
    'EXPIRED': 'danger',
    'COMPLETED': 'success'
  }
  return statusMap[status] || 'info'
}

// 加载用户详情
const loadUserDetail = async () => {
  if (!props.userId) return

  try {
    loading.value = true
    userDetail.value = await userManagementStore.fetchUserDetail(props.userId)
  } catch (error) {
    console.error('加载用户详情失败:', error)
    ElMessage.error('加载用户详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载充电记录
const loadChargingRecords = async () => {
  if (!props.userId) return

  try {
    loading.value = true
    const response = await userManagementStore.fetchChargingRecords(props.userId)
    chargingRecords.value = response.content
  } catch (error) {
    console.error('加载充电记录失败:', error)
    ElMessage.error('加载充电记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载预约记录
const loadReservations = async () => {
  if (!props.userId) return

  try {
    loading.value = true
    const response = await userManagementStore.fetchReservations(props.userId)
    reservations.value = response.content
  } catch (error) {
    console.error('加载预约记录失败:', error)
    ElMessage.error('加载预约记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载违规记录
const loadViolations = async () => {
  if (!props.userId) return

  try {
    loading.value = true
    const response = await userManagementStore.fetchViolations(props.userId)
    violations.value = response.content
  } catch (error) {
    console.error('加载违规记录失败:', error)
    ElMessage.error('加载违规记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = (tabName: string) => {
  switch (tabName) {
    case 'basic':
      loadUserDetail()
      break
    case 'charging':
      loadChargingRecords()
      break
    case 'reservation':
      loadReservations()
      break
    case 'violation':
      loadViolations()
      break
  }
}

// 关闭侧边栏
const handleClose = () => {
  drawerVisible.value = false
  activeTab.value = 'basic'
  userDetail.value = null
  chargingRecords.value = []
  reservations.value = []
  violations.value = []
}

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  if (newVal && props.userId) {
    loadUserDetail()
  }
})
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
