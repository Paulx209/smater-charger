import request from '@/utils/request'
import type {
  ReservationInfo,
  ReservationCreateRequest,
  ReservationCancelRequest,
  ReservationQueryParams,
  ReservationListResponse
} from '@/types/reservation'

/**
 * 创建预约
 */
export function createReservation(data: ReservationCreateRequest): Promise<ReservationInfo> {
  return request.post('/reservations', data)
}

/**
 * 查询我的预约列表
 */
export function getMyReservations(params?: ReservationQueryParams): Promise<ReservationListResponse> {
  return request.get('/reservations/my', { params })
}

/**
 * 获取预约详情
 */
export function getReservationById(id: number): Promise<ReservationInfo> {
  return request.get(`/reservations/${id}`)
}

/**
 * 取消预约
 */
export function cancelReservation(id: number, data?: ReservationCancelRequest): Promise<void> {
  return request.put(`/reservations/${id}/cancel`, data)
}

/**
 * 检查充电桩在指定时间段是否可预约
 */
export function checkPileAvailability(
  pileId: number,
  startTime: string,
  endTime: string
): Promise<boolean> {
  return request.get('/reservations/check-availability', {
    params: { pileId, startTime, endTime }
  })
}
