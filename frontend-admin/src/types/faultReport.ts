export type ChargingPileType = 'AC' | 'DC'

export enum FaultReportStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  RESOLVED = 'RESOLVED'
}

export const FaultReportStatusText: Record<FaultReportStatus, string> = {
  [FaultReportStatus.PENDING]: '待处理',
  [FaultReportStatus.PROCESSING]: '处理中',
  [FaultReportStatus.RESOLVED]: '已解决'
}

export const FaultReportStatusColor: Record<FaultReportStatus, 'info' | 'warning' | 'success'> = {
  [FaultReportStatus.PENDING]: 'info',
  [FaultReportStatus.PROCESSING]: 'warning',
  [FaultReportStatus.RESOLVED]: 'success'
}

export interface FaultReportInfo {
  id: number
  userId: number
  userName?: string
  userPhone?: string
  chargingPileId: number
  pileName?: string
  pileLocation?: string
  pileType?: ChargingPileType
  description: string
  status: FaultReportStatus
  statusDesc: string
  handlerId?: number
  handlerName?: string
  handleRemark?: string
  createdTime: string
  updatedTime: string
}

export interface FaultReportQueryParams {
  chargingPileId?: number
  status?: FaultReportStatus
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export interface FaultReportHandleRequest {
  status: FaultReportStatus
  handleRemark?: string
}

export interface FaultReportListResponse {
  content: FaultReportInfo[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface FaultStatisticsTopPile {
  chargingPileId: number
  pileName: string
  faultCount: number
}

export interface FaultStatisticsResponse {
  totalCount: number
  pendingCount: number
  processingCount: number
  resolvedCount: number
  avgHandleTime: number
  topFaultPiles: FaultStatisticsTopPile[]
}

export const formatPileType = (pileType?: ChargingPileType): string => {
  if (pileType === 'AC') return 'AC（交流慢充）'
  if (pileType === 'DC') return 'DC（直流快充）'
  return '未知类型'
}
