import request from '@/utils/request'
import type {
  FaultReportInfo,
  FaultReportCreateRequest,
  FaultReportQueryParams,
  FaultReportListResponse
} from '@/types/faultReport'

export function createFaultReport(data: FaultReportCreateRequest): Promise<FaultReportInfo> {
  return request.post('/fault-report', data)
}

export function getFaultReportList(params?: FaultReportQueryParams): Promise<FaultReportListResponse> {
  return request.get('/fault-report', { params })
}

export function getFaultReportDetail(id: number): Promise<FaultReportInfo> {
  return request.get(`/fault-report/${id}`)
}

export function deleteFaultReport(id: number): Promise<void> {
  return request.delete(`/fault-report/${id}`)
}