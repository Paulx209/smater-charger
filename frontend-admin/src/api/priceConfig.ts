import request from '@/utils/request'
import type {
  PriceConfigInfo,
  PriceConfigCreateRequest,
  PriceConfigUpdateRequest,
  PriceConfigQueryParams,
  PriceConfigListResponse,
  PriceEstimateRequest,
  PriceEstimateResponse,
  ChargingPileType
} from '@/types/priceConfig'

/**
 * 创建费用配置
 */
export function createPriceConfig(data: PriceConfigCreateRequest): Promise<PriceConfigInfo> {
  return request.post('/price-config', data)
}

/**
 * 更新费用配置
 */
export function updatePriceConfig(
  id: number,
  data: PriceConfigUpdateRequest
): Promise<PriceConfigInfo> {
  return request.put(`/price-config/${id}`, data)
}

/**
 * 删除费用配置
 */
export function deletePriceConfig(id: number): Promise<void> {
  return request.delete(`/price-config/${id}`)
}

/**
 * 查询费用配置列表
 */
export function getPriceConfigList(
  params?: PriceConfigQueryParams
): Promise<PriceConfigListResponse> {
  return request.get('/price-config', { params })
}

/**
 * 查询费用配置详情
 */
export function getPriceConfigDetail(id: number): Promise<PriceConfigInfo> {
  return request.get(`/price-config/${id}`)
}

/**
 * 获取当前有效费用配置
 */
export function getCurrentPriceConfig(
  chargingPileType: ChargingPileType
): Promise<PriceConfigInfo> {
  return request.get('/price-config/current', {
    params: { chargingPileType }
  })
}

/**
 * 费用预估
 */
export function estimatePrice(data: PriceEstimateRequest): Promise<PriceEstimateResponse> {
  return request.post('/price-config/estimate', data)
}
