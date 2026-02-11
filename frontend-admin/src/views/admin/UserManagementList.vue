<template>
  <div class="user-management-container">
    <el-card class="list-card" v-loading="userManagementStore.loading">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-button
              v-if="selectedUserIds.length > 0"
              type="warning"
              @click="handleBatchOperation"
            >
              批量操作 ({{ selectedUserIds.length }})
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出用户
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索用户名/邮箱"
              clearable
              style="width: 200px"
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
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>

          <el-form-item label="注册日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
              @change="handleDateChange"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户表格 -->
      <el-table
        :data="userManagementStore.users"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="realName" label="真实姓名" width="120" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="{ row }">
            {{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button
              :type="row.enabled ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button type="info" size="small" @click="handleResetPassword(row)">
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 用户详情侧边栏 -->
    <UserDetailDrawer
      v-model:visible="detailDrawerVisible"
      :user-id="currentUserId"
    />

    <!-- 密码重置对话框 -->
    <PasswordResetDialog
      v-model:visible="resetPasswordDialogVisible"
      :user="currentUser"
      @success="handleResetPasswordSuccess"
    />

    <!-- 批量操作对话框 -->
    <BatchStatusDialog
      v-model:visible="batchDialogVisible"
      :user-ids="selectedUserIds"
      @success="handleBatchSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { useUserManagementStore } from '@/stores/userManagement'
import type { UserAdminResponse } from '@/types/userManagement'
import UserDetailDrawer from '@/components/UserDetailDrawer.vue'
import PasswordResetDialog from '@/components/PasswordResetDialog.vue'
import BatchStatusDialog from '@/components/BatchStatusDialog.vue'

const userManagementStore = useUserManagementStore()

// 筛选表单
const filterForm = reactive({
  keyword: undefined as string | undefined,
  status: undefined as number | undefined,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined
})

// 日期范围
const dateRange = ref<[Date, Date] | null>(null)

// 选中的用户ID
const selectedUserIds = ref<number[]>([])

// 详情侧边栏
const detailDrawerVisible = ref(false)
const currentUserId = ref<number | null>(null)

// 密码重置对话框
const resetPasswordDialogVisible = ref(false)
const currentUser = ref<UserAdminResponse | null>(null)

// 批量操作对话框
const batchDialogVisible = ref(false)

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

// 日期范围改变
const handleDateChange = (value: [Date, Date] | null) => {
  if (value) {
    filterForm.startDate = value[0].toISOString().split('T')[0]
    filterForm.endDate = value[1].toISOString().split('T')[0]
  } else {
    filterForm.startDate = undefined
    filterForm.endDate = undefined
  }
}

// 查看详情
const handleViewDetail = (userId: number) => {
  currentUserId.value = userId
  detailDrawerVisible.value = true
}

// 切换用户状态
const handleToggleStatus = async (user: UserAdminResponse) => {
  try {
    const action = user.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userManagementStore.updateStatus(user.id, !user.enabled)
    await handleFilter()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('切换用户状态失败:', error)
    ElMessage.error('切换用户状态失败，请稍后重试')
  }
}

// 重置密码
const handleResetPassword = (user: UserAdminResponse) => {
  currentUser.value = user
  resetPasswordDialogVisible.value = true
}

// 重置密码成功
const handleResetPasswordSuccess = () => {
  resetPasswordDialogVisible.value = false
  currentUser.value = null
}

// 表格选择改变
const handleSelectionChange = (selection: UserAdminResponse[]) => {
  selectedUserIds.value = selection.map(user => user.id)
}

// 批量操作
const handleBatchOperation = () => {
  if (selectedUserIds.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }
  batchDialogVisible.value = true
}

// 批量操作成功
const handleBatchSuccess = async () => {
  batchDialogVisible.value = false
  selectedUserIds.value = []
  await handleFilter()
}

// 导出用户
const handleExport = async () => {
  try {
    await userManagementStore.exportUserList({
      status: filterForm.status
    })
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 筛选
const handleFilter = async () => {
  try {
    userManagementStore.currentPage = 1
    await userManagementStore.fetchUserList({
      keyword: filterForm.keyword,
      status: filterForm.status,
      startDate: filterForm.startDate,
      endDate: filterForm.endDate
    })
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败，请稍后重试')
  }
}

// 重置
const handleReset = async () => {
  filterForm.keyword = undefined
  filterForm.status = undefined
  filterForm.startDate = undefined
  filterForm.endDate = undefined
  dateRange.value = null
  userManagementStore.currentPage = 1
  await handleFilter()
}

// 分页大小改变
const handleSizeChange = async (size: number) => {
  userManagementStore.pageSize = size
  await handleFilter()
}

// 页码改变
const handlePageChange = async (page: number) => {
  userManagementStore.currentPage = page
  await handleFilter()
}

// 组件挂载时加载数据
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
