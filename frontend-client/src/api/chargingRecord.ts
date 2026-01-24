import request from '@/utils/request'
import type {
  ChargingRecordInfo,
  ChargingRecordStartRequest,
  ChargingRecordEndRequest,
  ChargingRecordQueryParams,
  ChargingRecordListResponse,
  ChargingRecordStatisticsMonthly,
  ChargingRecordStatisticsYearly
} from '@/types/chargingRecord'

/**
 * 开始充电
 */
export function startCharging(data: ChargingRecordStartRequest): Promise<ChargingRecordInfo> {
  return request.post('/charging-record/start', data)
}

/**
 * 结束充电
 */
export function endCharging(id: number, data: ChargingRecordEndRequest): Promise<ChargingRecordInfo> {
  return request.post(`/charging-record/end/${id}`, data)
}

/**
 * 查询充电记录列表
 */
export function getChargingRecordList(
  params?: ChargingRecordQueryParams
): Promise<ChargingRecordListResponse> {
  return request.get('/charging-record', { params })
}

/**
 * 查询充电记录详情
 */
export function getChargingRecordDetail(id: number): Promise<ChargingRecordInfo> {
  return request.get(`/charging-record/${id}`)
}

/**
 * 查询当前充电记录
 */
export function getCurrentChargingRecord(): Promise<ChargingRecordInfo | null> {
  return request.get('/charging-record/current')
}

/**
 * 查询月度统计
 */
export function getMonthlyStatistics(
  year: number,
  month: number
): Promise<ChargingRecordStatisticsMonthly> {
  return request.get('/charging-record/statistics/monthly', {
    params: { year, month }
  })
}

/**
 * 查询年度统计
 */
export function getYearlyStatistics(year: number): Promise<ChargingRecordStatisticsYearly> {
  return request.get('/charging-record/statistics/yearly', {
    params: { year }
  })
}
