<template>
  <div class="user-management-container">
    <el-card class="list-card" v-loading="userManagementStore.loading">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-button v-if="selectedUserIds.length > 0" type="warning" @click="handleBatchOperation">
              批量修改状态 ({{ selectedUserIds.length }})
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出用户
            </el-button>
          </div>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="用户名 / 手机号 / 昵称"
              clearable
              style="width: 220px"
              @keyup.enter="handleFilter"
            />
          </el-form-item>

          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
              @change="handleFilter"
            >
              <el-option label="启用" :value="UserStatus.ENABLED" />
              <el-option label="禁用" :value="UserStatus.DISABLED" />
            </el-select>
          </el-form-item>

          <el-form-item label="创建时间">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px"
              @change="handleDateChange"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="userManagementStore.users" stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getUserStatusColor(row.status)">
              {{ getUserStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="车辆数" width="90">
          <template #default="{ row }">{{ row.statistics?.vehicleCount ?? 0 }}</template>
        </el-table-column>

        <el-table-column prop="createdTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
        </el-table-column>

        <el-table-column label="最近充电" width="180">
          <template #default="{ row }">{{ formatDateTime(row.statistics?.lastChargingTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row.id)">详情</el-button>
            <el-button :type="isUserEnabled(row.status) ? 'warning' : 'success'" size="small" @click="handleToggleStatus(row)">
              {{ isUserEnabled(row.status) ? '禁用' : '启用' }}
            </el-button>
            <el-button type="info" size="small" @click="handleResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="userManagementStore.total > 0" class="pagination">
        <el-pagination
          v-model:current-page="userManagementStore.currentPage"
          v-model:page-size="userManagementStore.pageSize"
          :total="userManagementStore.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <UserDetailDrawer v-model:visible="detailDrawerVisible" :user-id="currentUserId" />

    <PasswordResetDialog v-model:visible="resetPasswordDialogVisible" :user="currentUser" @success="handleResetPasswordSuccess" />

    <BatchStatusDialog v-model:visible="batchDialogVisible" :user-ids="selectedUserIds" @success="handleBatchSuccess" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserDetailDrawer from '@/components/UserDetailDrawer.vue'
import PasswordResetDialog from '@/components/PasswordResetDialog.vue'
import BatchStatusDialog from '@/components/BatchStatusDialog.vue'
import { useUserManagementStore } from '@/stores/userManagement'
import type { UserAdminResponse } from '@/types/user'
import { UserStatus, formatDateTime, getUserStatusColor, getUserStatusText, isUserEnabled } from '@/types/user'

const userManagementStore = useUserManagementStore()

const filterForm = reactive({
  keyword: undefined as string | undefined,
  status: undefined as UserStatus | undefined,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined
})

const dateRange = ref<[Date, Date] | null>(null)
const selectedUserIds = ref<number[]>([])
const detailDrawerVisible = ref(false)
const currentUserId = ref<number | null>(null)
const resetPasswordDialogVisible = ref(false)
const currentUser = ref<UserAdminResponse | null>(null)
const batchDialogVisible = ref(false)

const handleDateChange = (value: [Date, Date] | null) => {
  if (value) {
    filterForm.startDate = value[0].toISOString().split('T')[0]
    filterForm.endDate = value[1].toISOString().split('T')[0]
    return
  }
  filterForm.startDate = undefined
  filterForm.endDate = undefined
}

const handleViewDetail = (userId: number) => {
  currentUserId.value = userId
  detailDrawerVisible.value = true
}

const handleToggleStatus = async (user: UserAdminResponse) => {
  const nextStatus = isUserEnabled(user.status) ? UserStatus.DISABLED : UserStatus.ENABLED
  const actionText = nextStatus === UserStatus.ENABLED ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${actionText}用户 “${user.username}” 吗？`, `${actionText}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await userManagementStore.updateStatus(user.id, { status: nextStatus })
    await handleFilter()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error(`${actionText}用户状态失败`)
  }
}

const handleResetPassword = (user: UserAdminResponse) => {
  currentUser.value = user
  resetPasswordDialogVisible.value = true
}

const handleResetPasswordSuccess = () => {
  resetPasswordDialogVisible.value = false
  currentUser.value = null
}

const handleSelectionChange = (selection: UserAdminResponse[]) => {
  selectedUserIds.value = selection.map((user) => user.id)
}

const handleBatchOperation = () => {
  if (selectedUserIds.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }
  batchDialogVisible.value = true
}

const handleBatchSuccess = async () => {
  batchDialogVisible.value = false
  selectedUserIds.value = []
  await handleFilter()
}

const handleExport = async () => {
  try {
    await userManagementStore.exportUserList({ status: filterForm.status })
  } catch {
    ElMessage.error('导出用户失败')
  }
}

const handleFilter = async () => {
  try {
    userManagementStore.currentPage = 1
    await userManagementStore.fetchUserList({
      keyword: filterForm.keyword,
      status: filterForm.status,
      startDate: filterForm.startDate,
      endDate: filterForm.endDate,
      page: 1,
      size: userManagementStore.pageSize
    })
  } catch {
    ElMessage.error('查询用户失败')
  }
}

const handleReset = async () => {
  filterForm.keyword = undefined
  filterForm.status = undefined
  filterForm.startDate = undefined
  filterForm.endDate = undefined
  dateRange.value = null
  userManagementStore.currentPage = 1
  await handleFilter()
}

const handleSizeChange = async (size: number) => {
  userManagementStore.pageSize = size
  await userManagementStore.fetchUserList({
    keyword: filterForm.keyword,
    status: filterForm.status,
    startDate: filterForm.startDate,
    endDate: filterForm.endDate,
    page: 1,
    size
  })
}

const handlePageChange = async (page: number) => {
  userManagementStore.currentPage = page
  await userManagementStore.fetchUserList({
    keyword: filterForm.keyword,
    status: filterForm.status,
    startDate: filterForm.startDate,
    endDate: filterForm.endDate,
    page,
    size: userManagementStore.pageSize
  })
}

onMounted(async () => {
  await userManagementStore.fetchUserList()
})
</script>

<style scoped>
.user-management-container {
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
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>