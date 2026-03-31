import request from '@/utils/request'
import type { ReservationInfo, ReservationPage, ReservationQueryParams } from '@/types/reservation'

export function getAdminReservationList(params?: ReservationQueryParams): Promise<ReservationPage> {
  return request.get('/admin/reservations', { params })
}

export function getAdminReservationDetail(id: number): Promise<ReservationInfo> {
  return request.get(`/admin/reservations/${id}`)
}

export function cancelAdminReservation(id: number): Promise<void> {
  return request.put(`/admin/reservations/${id}/cancel`)
}