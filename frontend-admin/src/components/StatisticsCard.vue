<template>
  <el-card class="statistics-card" shadow="hover">
    <div class="card-content">
      <div class="card-icon" :style="{ backgroundColor: iconBgColor }">
        <component :is="icon" :style="{ color: iconColor }" />
      </div>
      <div class="card-info">
        <div class="card-title">{{ title }}</div>
        <div class="card-value">{{ formattedValue }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title: string
  value: number | string
  icon: any
  iconColor?: string
  iconBgColor?: string
  suffix?: string
}

const props = withDefaults(defineProps<Props>(), {
  iconColor: '#409EFF',
  iconBgColor: '#ECF5FF',
  suffix: ''
})

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString() + props.suffix
  }
  return props.value + props.suffix
})
</script>

<style scoped>
.statistics-card {
  height: 100%;
}

.card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
</style>
