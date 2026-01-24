<template>
  <div class="about-container">
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="24" color="#409eff">
            <InfoFilled />
          </el-icon>
          <span>关于系统</span>
        </div>
      </template>

      <div class="about-content">
        <div class="system-info">
          <h2>智能充电桩管理系统</h2>
          <p class="version">版本: v1.0.0</p>
          <p class="description">
            智能充电桩管理系统是一个为电动汽车车主提供便捷充电服务的综合平台。
            系统通过实时监控充电桩状态、智能预约管理和超时占位预警等功能，
            有效解决了充电桩资源分配不均和超时占位等问题，提升了充电桩的使用效率。
          </p>
        </div>

        <el-divider />

        <div class="features-section">
          <h3>核心功能</h3>
          <div class="features-list">
            <div class="feature-item">
              <el-icon color="#409eff"><CircleCheck /></el-icon>
              <div class="feature-text">
                <strong>充电桩查询</strong>
                <span>支持多条件筛选，快速找到合适的充电桩</span>
              </div>
            </div>

            <div class="feature-item">
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
              <div class="feature-text">
                <strong>实时状态监控</strong>
                <span>实时显示充电桩状态，避免无效奔波</span>
              </div>
            </div>

            <div class="feature-item">
              <el-icon color="#e6a23c"><CircleCheck /></el-icon>
              <div class="feature-text">
                <strong>预约充电</strong>
                <span>提前预约充电桩，合理安排充电时间</span>
              </div>
            </div>

            <div class="feature-item">
              <el-icon color="#f56c6c"><CircleCheck /></el-icon>
              <div class="feature-text">
                <strong>超时占位预警</strong>
                <span>充电完成自动提醒，避免产生违规记录</span>
              </div>
            </div>

            <div class="feature-item">
              <el-icon color="#909399"><CircleCheck /></el-icon>
              <div class="feature-text">
                <strong>充电记录管理</strong>
                <span>详细记录每次充电信息，费用清晰透明</span>
              </div>
            </div>

            <div class="feature-item">
              <el-icon color="#409eff"><CircleCheck /></el-icon>
              <div class="feature-text">
                <strong>地图导航</strong>
                <span>一键导航到充电桩位置，快速到达</span>
              </div>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="tech-stack-section">
          <h3>技术栈</h3>
          <div class="tech-grid">
            <el-tag size="large" type="primary">Vue 3</el-tag>
            <el-tag size="large" type="success">TypeScript</el-tag>
            <el-tag size="large" type="warning">Element Plus</el-tag>
            <el-tag size="large" type="danger">Pinia</el-tag>
            <el-tag size="large" type="info">Vue Router</el-tag>
            <el-tag size="large">Axios</el-tag>
            <el-tag size="large" type="primary">Spring Boot</el-tag>
            <el-tag size="large" type="success">MySQL</el-tag>
            <el-tag size="large" type="warning">Redis</el-tag>
            <el-tag size="large" type="danger">JWT</el-tag>
          </div>
        </div>

        <el-divider />

        <div class="contact-section">
          <h3>联系我们</h3>
          <div class="contact-info">
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>邮箱: support@smartcharger.com</span>
            </div>
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>电话: 400-123-4567</span>
            </div>
            <div class="contact-item">
              <el-icon><Location /></el-icon>
              <span>地址: 北京市朝阳区xxx路xxx号</span>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="copyright">
          <p>© 2024 智能充电桩管理系统. All rights reserved.</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {
  InfoFilled,
  CircleCheck,
  Message,
  Phone,
  Location
} from '@element-plus/icons-vue'
</script>

<style scoped>
.about-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.about-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.about-content {
  padding: 20px 0;
}

.system-info {
  text-align: center;
  margin-bottom: 20px;
}

.system-info h2 {
  margin: 0 0 12px 0;
  font-size: 28px;
  color: #303133;
}

.version {
  margin: 0 0 20px 0;
  font-size: 14px;
  color: #909399;
}

.description {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  text-align: left;
}

.features-section h3,
.tech-stack-section h3,
.contact-section h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.features-list {
  display: grid;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.feature-item:hover {
  background-color: #ecf5ff;
  transform: translateX(4px);
}

.feature-item .el-icon {
  font-size: 20px;
  margin-top: 2px;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature-text strong {
  font-size: 16px;
  color: #303133;
}

.feature-text span {
  font-size: 14px;
  color: #606266;
}

.tech-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: #606266;
}

.contact-item .el-icon {
  font-size: 20px;
  color: #409eff;
}

.copyright {
  text-align: center;
  margin-top: 20px;
}

.copyright p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

@media (max-width: 768px) {
  .system-info h2 {
    font-size: 24px;
  }

  .description {
    font-size: 14px;
  }

  .tech-grid {
    justify-content: center;
  }
}
</style>
