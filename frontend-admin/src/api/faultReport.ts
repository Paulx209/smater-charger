import request from '@/utils/request'
import type {
  FaultReportInfo,
  FaultReportQueryParams,
  FaultReportHandleRequest,
  FaultReportListResponse,
  FaultStatisticsResponse
} from '@/types/faultReport'

export function getFaultReportList(params?: FaultReportQueryParams): Promise<FaultReportListResponse> {
  return request.get('/fault-report/admin/all', { params })
}

export function getFaultReportDetail(id: number): Promise<FaultReportInfo> {
  return request.get(`/fault-report/admin/${id}`)
}

export function handleFaultReport(
  id: number,
  data: FaultReportHandleRequest
): Promise<FaultReportInfo> {
  return request.put(`/fault-report/admin/${id}/handle`, data)
}

export function getFaultStatistics(
  params?: Pick<FaultReportQueryParams, 'startDate' | 'endDate'>
): Promise<FaultStatisticsResponse> {
  return request.get('/fault-report/admin/statistics', { params })
}
