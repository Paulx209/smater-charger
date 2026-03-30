import request from '@/utils/request'
import type {
  PriceConfigInfo,
  PriceEstimateRequest,
  PriceEstimateResponse,
  ChargingPileType
} from '@/types/priceConfig'

export function getCurrentPriceConfig(
  chargingPileType: ChargingPileType
): Promise<PriceConfigInfo> {
  return request.get('/price-config/current', {
    params: { chargingPileType }
  }) as Promise<PriceConfigInfo>
}

export function estimatePrice(data: PriceEstimateRequest): Promise<PriceEstimateResponse> {
  return request.post('/price-config/estimate', data) as Promise<PriceEstimateResponse>
}