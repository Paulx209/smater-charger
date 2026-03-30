import request from '@/utils/request'
import type { ChargingRecordInfo, ChargingRecordPage, ChargingRecordQueryParams } from '@/types/chargingRecord'

export function getAdminChargingRecordList(
  params?: ChargingRecordQueryParams
): Promise<ChargingRecordPage> {
  return request.get('/charging-record/admin/all', { params })
}

export function getAdminChargingRecordDetail(id: number): Promise<ChargingRecordInfo> {
  return request.get(`/charging-record/admin/${id}`)
}