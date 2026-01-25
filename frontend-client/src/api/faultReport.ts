import request from '@/utils/request'
import type {
  FaultReportInfo,
  FaultReportCreateRequest,
  FaultReportUpdateRequest,
  FaultReportQueryParams,
  FaultReportListResponse
} from '@/types/faultReport'

/**
 * 故障报修API
 */

// 创建故障报修
export const createFaultReport = (data: FaultReportCreateRequest) => {
  return request<FaultReportInfo>({
    url: '/api/fault-reports',
    method: 'post',
    data
  })
}

// 获取故障报修列表
export const getFaultReportList = (params: FaultReportQueryParams) => {
  return request<FaultReportListResponse>({
    url: '/api/fault-reports',
    method: 'get',
    params
  })
}

// 获取我的故障报修列表
export const getMyFaultReportList = (params: FaultReportQueryParams) => {
  return request<FaultReportListResponse>({
    url: '/api/fault-reports/my',
    method: 'get',
    params
  })
}

// 获取故障报修详情
export const getFaultReportDetail = (id: number) => {
  return request<FaultReportInfo>({
    url: `/api/fault-reports/${id}`,
    method: 'get'
  })
}

// 更新故障报修
export const updateFaultReport = (id: number, data: FaultReportUpdateRequest) => {
  return request<FaultReportInfo>({
    url: `/api/fault-reports/${id}`,
    method: 'put',
    data
  })
}

// 删除故障报修
export const deleteFaultReport = (id: number) => {
  return request<void>({
    url: `/api/fault-reports/${id}`,
    method: 'delete'
  })
}

// 上传故障图片
export const uploadFaultImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request<{ url: string }>({
    url: '/api/fault-reports/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取充电桩的故障报修统计
export const getFaultReportStatsByPile = (pileId: number) => {
  return request<{
    total: number
    pending: number
    processing: number
    resolved: number
    closed: number
  }>({
    url: `/api/fault-reports/stats/pile/${pileId}`,
    method: 'get'
  })
}
