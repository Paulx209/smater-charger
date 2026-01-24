import request from '@/utils/request'
import type {
  ChargingPileInfo,
  ChargingPileQueryParams,
  NearbyQueryParams,
  PageResponse
} from '@/types/chargingPile'

/**
 * 查询充电桩列表（分页）
 */
export function getChargingPiles(params: ChargingPileQueryParams): Promise<PageResponse<ChargingPileInfo>> {
  return request.get('/charging-piles', { params })
}

/**
 * 获取充电桩详情
 */
export function getChargingPileById(id: number): Promise<ChargingPileInfo> {
  return request.get(`/charging-piles/${id}`)
}

/**
 * 获取附近充电桩（地图模式）
 */
export function getNearbyChargingPiles(params: NearbyQueryParams): Promise<ChargingPileInfo[]> {
  return request.get('/charging-piles/nearby', { params })
}
