// 充电桩类型枚举
export enum ChargingPileType {
  AC = 'AC',  // 交流慢充
  DC = 'DC'   // 直流快充
}

// 充电桩类型文本映射
export const ChargingPileTypeText: Record<ChargingPileType, string> = {
  [ChargingPileType.AC]: 'AC交流慢充',
  [ChargingPileType.DC]: 'DC直流快充'
}

// 费用配置信息接口
export interface PriceConfigInfo {
  id: number
  chargingPileType: ChargingPileType
  pricePerKwh: number          // 每度电价格
  serviceFee: number           // 服务费
  startTime?: string           // 生效开始时间
  endTime?: string             // 生效结束时间
  isActive: number             // 1=激活，0=停用
  createdTime: string
  updatedTime: string
}

// 创建费用配置请求
export interface PriceConfigCreateRequest {
  chargingPileType: ChargingPileType
  pricePerKwh: number
  serviceFee: number
  startTime?: string
  endTime?: string
  isActive?: number
}

// 更新费用配置请求
export interface PriceConfigUpdateRequest {
  pricePerKwh?: number
  serviceFee?: number
  isActive?: number
}

// 查询参数
export interface PriceConfigQueryParams {
  chargingPileType?: ChargingPileType
  isActive?: number
  page?: number
  size?: number
}

// 费用配置列表响应
export interface PriceConfigListResponse {
  content: PriceConfigInfo[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 费用预估请求
export interface PriceEstimateRequest {
  chargingPileType: ChargingPileType
  electricQuantity: number
}

// 费用预估响应
export interface PriceEstimateResponse {
  electricQuantity: number
  pricePerKwh: number
  serviceFee: number
  totalPrice: number
  breakdown: {
    electricityFee: number
    serviceFee: number
  }
}

// 格式化金额，保留2位小数
export function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}

// 格式化单价
export function formatUnitPrice(price: number): string {
  return `¥${price.toFixed(2)}/度`
}

// 格式化时间范围
export function formatTimeRange(startTime?: string, endTime?: string): string {
  if (!startTime && !endTime) return '长期有效'

  const formatDateTime = (dateTime: string) => {
    const date = new Date(dateTime)
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }

  if (!startTime) return `至 ${formatDateTime(endTime!)}`
  if (!endTime) return `${formatDateTime(startTime)} 起`
  return `${formatDateTime(startTime)} 至 ${formatDateTime(endTime)}`
}

// 激活状态标签类型
export function getActiveStatusTagType(isActive: number): string {
  return isActive === 1 ? 'success' : 'info'
}

// 激活状态文本
export function getActiveStatusText(isActive: number): string {
  return isActive === 1 ? '激活' : '停用'
}

// 计算总单价
export function calculateTotalUnitPrice(pricePerKwh: number, serviceFee: number): number {
  return pricePerKwh + serviceFee
}
