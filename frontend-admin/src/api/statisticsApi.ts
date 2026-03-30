import axios from 'axios'
import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import type { StatisticsOverview, ChargingPileUsage, RevenueStatistics, UserActivity } from '@/types/statistics'

interface StatisticsQueryParams {
  rangeType?: string
  startDate?: string
  endDate?: string
}

export function getStatisticsOverview(params: StatisticsQueryParams): Promise<StatisticsOverview> {
  return request({
    url: '/admin/statistics/overview',
    method: 'get',
    params
  }) as Promise<StatisticsOverview>
}

export function getChargingPileUsage(params: StatisticsQueryParams): Promise<ChargingPileUsage> {
  return request({
    url: '/admin/statistics/charging-pile-usage',
    method: 'get',
    params
  }) as Promise<ChargingPileUsage>
}

export function getRevenueStatistics(params: StatisticsQueryParams): Promise<RevenueStatistics> {
  return request({
    url: '/admin/statistics/revenue',
    method: 'get',
    params
  }) as Promise<RevenueStatistics>
}

export function getUserActivity(params: StatisticsQueryParams): Promise<UserActivity> {
  return request({
    url: '/admin/statistics/user-activity',
    method: 'get',
    params
  }) as Promise<UserActivity>
}

export async function exportStatistics(params: StatisticsQueryParams): Promise<Blob> {
  const token = getToken()
  const response = await axios.get('/api/admin/statistics/export', {
    params,
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined
  })
  return response.data as Blob
}