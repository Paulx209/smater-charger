// 充电桩类型枚举
export enum ChargingPileType {
  AC = 'AC', // 交流慢充
  DC = 'DC'  // 直流快充
}

// 充电桩状态枚举
export enum ChargingPileStatus {
  IDLE = 'IDLE',           // 空闲
  CHARGING = 'CHARGING',   // 充电中
  FAULT = 'FAULT',         // 故障
  RESERVED = 'RESERVED',   // 已预约
  OVERTIME = 'OVERTIME'    // 超时占位
}

// 充电桩信息接口
export interface ChargingPileInfo {
  id: number
  code: string
  location: string
  lng: number
  lat: number
  type: ChargingPileType
  typeDesc: string
  power: number
  status: ChargingPileStatus
  statusDesc: string
  distance?: number  // 距离（km），可选
  createdTime: string
  updatedTime: string
}

// 充电桩查询参数
export interface ChargingPileQueryParams {
  keyword?: string           // 位置关键词搜索
  type?: ChargingPileType    // 充电桩类型
  status?: ChargingPileStatus // 状态
  lng?: number               // 当前经度（用于距离排序）
  lat?: number               // 当前纬度
  page?: number              // 页码，默认1
  size?: number              // 每页数量，默认10
}

// 附近充电桩查询参数
export interface NearbyQueryParams {
  lng: number                // 必填，中心点经度
  lat: number                // 必填，中心点纬度
  radius?: number            // 可选，搜索半径（km），默认5km
  type?: ChargingPileType    // 可选，充电桩类型
  status?: ChargingPileStatus // 可选，状态筛选
}

// 分页响应接口
export interface PageResponse<T> {
  total: number
  pages: number
  current: number
  records: T[]
}

// 充电桩类型描述映射
export const ChargingPileTypeDesc: Record<ChargingPileType, string> = {
  [ChargingPileType.AC]: '交流慢充',
  [ChargingPileType.DC]: '直流快充'
}

// 充电桩状态描述映射
export const ChargingPileStatusDesc: Record<ChargingPileStatus, string> = {
  [ChargingPileStatus.IDLE]: '空闲',
  [ChargingPileStatus.CHARGING]: '充电中',
  [ChargingPileStatus.FAULT]: '故障',
  [ChargingPileStatus.RESERVED]: '已预约',
  [ChargingPileStatus.OVERTIME]: '超时占位'
}

// 充电桩状态标签类型映射（Element Plus Tag type）
export const ChargingPileStatusTagType: Record<ChargingPileStatus, 'success' | 'primary' | 'warning' | 'danger' | 'info'> = {
  [ChargingPileStatus.IDLE]: 'success',
  [ChargingPileStatus.CHARGING]: 'primary',
  [ChargingPileStatus.FAULT]: 'danger',
  [ChargingPileStatus.RESERVED]: 'warning',
  [ChargingPileStatus.OVERTIME]: 'danger'
}
