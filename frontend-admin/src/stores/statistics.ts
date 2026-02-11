import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as statisticsApi from '@/api/statisticsApi'
import type { StatisticsOverview } from '@/types/statistics'

export const useStatisticsStore = defineStore('statistics', () => {
  const overview = ref<StatisticsOverview | null>(null)
  const loading = ref(false)

  async function fetchOverview(params: {
    rangeType?: string
    startDate?: string
    endDate?: string
  }) {
    loading.value = true
    try {
      overview.value = await statisticsApi.getStatisticsOverview(params)
      return overview.value
    } finally {
      loading.value = false
    }
  }

  async function exportData(params: {
    rangeType?: string
    startDate?: string
    endDate?: string
  }) {
    const blob = await statisticsApi.exportStatistics(params)
    const url = window.URL.createObjectURL(blob as Blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `statistics_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
  }

  return {
    overview,
    loading,
    fetchOverview,
    exportData
  }
})
