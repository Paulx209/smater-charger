export interface StatisticsOverview {
  rangeType: string
  startTime: string
  endTime: string
  totalChargingPileCount: number
  usedChargingPileCount: number
  chargingPileUsageRate: number
  totalRevenue: number
  averageDailyRevenue: number
  activeUserCount: number
  newUserCount: number
  totalChargingCount: number
}

export interface ChargingPileUsage {
  rangeType: string
  startTime: string
  endTime: string
  totalChargingPileCount: number
  usedChargingPileCount: number
  chargingPileUsageRate: number
  idleCount: number
  chargingCount: number
  faultCount: number
  reservedCount: number
  overtimeCount: number
}

export interface RevenueStatistics {
  rangeType: string
  startTime: string
  endTime: string
  totalRevenue: number
  averageDailyRevenue: number
  totalChargingCount: number
  dailyRecords: DailyRevenueRecord[]
}

export interface DailyRevenueRecord {
  date: string
  revenue: number
  chargingCount: number
}

export interface UserActivity {
  rangeType: string
  startTime: string
  endTime: string
  activeUserCount: number
  newUserCount: number
  dailyRecords: DailyActivityRecord[]
}

export interface DailyActivityRecord {
  date: string
  activeUserCount: number
  newUserCount: number
}
