// 通用类型定义

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
