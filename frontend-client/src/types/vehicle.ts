// 车辆信息接口
export interface VehicleInfo {
  id: number
  userId: number
  licensePlate: string
  brand?: string
  model?: string
  batteryCapacity?: number
  isDefault: number  // 0=否，1=是
  createdTime: string
  updatedTime: string
}

// 添加车辆请求
export interface VehicleCreateRequest {
  licensePlate: string
  brand?: string
  model?: string
  batteryCapacity?: number
  isDefault?: number  // 0=否，1=是
}

// 更新车辆请求
export interface VehicleUpdateRequest {
  licensePlate?: string
  brand?: string
  model?: string
  batteryCapacity?: number
}

// 车牌号正则表达式
export const LICENSE_PLATE_PATTERN = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$/

// 车牌号验证函数
export function validateLicensePlate(licensePlate: string): boolean {
  return LICENSE_PLATE_PATTERN.test(licensePlate)
}
