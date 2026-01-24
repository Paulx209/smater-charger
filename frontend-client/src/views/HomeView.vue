<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎使用智能充电桩管理系统</h1>
          <p>为您提供便捷的充电桩查询、预约和管理服务</p>
        </div>
        <div class="welcome-icon">
          <el-icon :size="80" color="#409eff">
            <Lightning />
          </el-icon>
        </div>
      </div>
    </el-card>

    <!-- 快速功能入口 -->
    <div class="quick-actions">
      <el-card class="action-card" shadow="hover" @click="navigateTo('/charging-piles')">
        <div class="action-content">
          <el-icon :size="48" color="#409eff">
            <Search />
          </el-icon>
          <h3>充电桩查询</h3>
          <p>查找附近的充电桩，查看实时状态</p>
        </div>
      </el-card>

      <el-card class="action-card" shadow="hover" @click="navigateTo('/charging-piles')">
        <div class="action-content">
          <el-icon :size="48" color="#67c23a">
            <Calendar />
          </el-icon>
          <h3>预约充电</h3>
          <p>提前预约充电桩，避免排队等待</p>
        </div>
      </el-card>

      <el-card class="action-card" shadow="hover" @click="navigateTo('/profile')">
        <div class="action-content">
          <el-icon :size="48" color="#e6a23c">
            <User />
          </el-icon>
          <h3>个人中心</h3>
          <p>管理个人信息，查看充电记录</p>
        </div>
      </el-card>

      <el-card class="action-card" shadow="hover">
        <div class="action-content">
          <el-icon :size="48" color="#f56c6c">
            <Bell />
          </el-icon>
          <h3>占位预警</h3>
          <p>及时接收充电完成提醒</p>
        </div>
      </el-card>
    </div>

    <!-- 系统特色 -->
    <el-card class="features-card">
      <template #header>
        <div class="card-header">
          <span>系统特色</span>
        </div>
      </template>

      <div class="features-grid">
        <div class="feature-item">
          <el-icon :size="32" color="#409eff">
            <Location />
          </el-icon>
          <h4>实时定位</h4>
          <p>精准定位附近充电桩，显示距离和导航</p>
        </div>

        <div class="feature-item">
          <el-icon :size="32" color="#67c23a">
            <View />
          </el-icon>
          <h4>状态可视</h4>
          <p>实时查看充电桩状态，空闲/充电中一目了然</p>
        </div>

        <div class="feature-item">
          <el-icon :size="32" color="#e6a23c">
            <Clock />
          </el-icon>
          <h4>智能预警</h4>
          <p>充电完成自动提醒，避免超时占位</p>
        </div>

        <div class="feature-item">
          <el-icon :size="32" color="#f56c6c">
            <DataAnalysis />
          </el-icon>
          <h4>数据统计</h4>
          <p>充电记录详细统计，费用清晰透明</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  Lightning,
  Search,
  Calendar,
  User,
  Bell,
  Location,
  View,
  Clock,
  DataAnalysis
} from '@element-plus/icons-vue'

const router = useRouter()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-card :deep(.el-card__body) {
  padding: 40px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h1 {
  margin: 0 0 16px 0;
  font-size: 32px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  font-size: 18px;
  opacity: 0.9;
}

.welcome-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.action-content {
  text-align: center;
  padding: 20px;
}

.action-content .el-icon {
  margin-bottom: 16px;
}

.action-content h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #303133;
}

.action-content p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.features-card {
  margin-bottom: 30px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
  padding: 20px 0;
}

.feature-item {
  text-align: center;
}

.feature-item .el-icon {
  margin-bottom: 12px;
}

.feature-item h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.feature-item p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
  }

  .welcome-icon {
    margin-top: 20px;
  }

  .welcome-text h1 {
    font-size: 24px;
  }

  .welcome-text p {
    font-size: 16px;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>
