import request from '@/utils/request'
import type {
  PriceConfigInfo,
  PriceConfigCreateRequest,
  PriceConfigUpdateRequest,
  PriceConfigQueryParams,
  PriceConfigListResponse
} from '@/types/priceConfig'

const ADMIN_PRICE_CONFIG_BASE = '/admin/price-config'

export function createPriceConfig(data: PriceConfigCreateRequest): Promise<PriceConfigInfo> {
  return request.post(ADMIN_PRICE_CONFIG_BASE, data) as Promise<PriceConfigInfo>
}

export function updatePriceConfig(
  id: number,
  data: PriceConfigUpdateRequest
): Promise<PriceConfigInfo> {
  return request.put(`${ADMIN_PRICE_CONFIG_BASE}/${id}`, data) as Promise<PriceConfigInfo>
}

export function deletePriceConfig(id: number): Promise<void> {
  return request.delete(`${ADMIN_PRICE_CONFIG_BASE}/${id}`) as Promise<void>
}

export function getPriceConfigList(
  params?: PriceConfigQueryParams
): Promise<PriceConfigListResponse> {
  return request.get(ADMIN_PRICE_CONFIG_BASE, { params }) as Promise<PriceConfigListResponse>
}

export function getPriceConfigDetail(id: number): Promise<PriceConfigInfo> {
  return request.get(`${ADMIN_PRICE_CONFIG_BASE}/${id}`) as Promise<PriceConfigInfo>
}