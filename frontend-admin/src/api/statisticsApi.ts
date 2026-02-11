import request from '@/utils/request'
import type { StatisticsOverview, ChargingPileUsage, RevenueStatistics, UserActivity } from '@/types/statistics'

export function getStatisticsOverview(params: {
  rangeType?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<StatisticsOverview>('/admin/statistics/overview', { params })
}

export function getChargingPileUsage(params: {
  rangeType?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<ChargingPileUsage>('/admin/statistics/charging-pile-usage', { params })
}

export function getRevenueStatistics(params: {
  rangeType?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<RevenueStatistics>('/admin/statistics/revenue', { params })
}

export function getUserActivity(params: {
  rangeType?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<UserActivity>('/admin/statistics/user-activity', { params })
}

export function exportStatistics(params: {
  rangeType?: string
  startDate?: string
  endDate?: string
}) {
  return request.get('/admin/statistics/export', {
    params,
    responseType: 'blob'
  })
}
