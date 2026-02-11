/**
 * 认证相关API接口
 */
import request from '@/utils/request'

// 用户信息接口
export interface UserInfo {
  userId: number
  username: string
  nickname: string
  phone: string
  avatar: string | null
  name: string | null
  warningThreshold: number | null
  status: number
  roles: string[]
}

// 注册请求参数
export interface RegisterRequest {
  username: string
  password: string
  phone: string
  nickname?: string
}

// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应数据
export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

// 更新资料请求参数
export interface UpdateProfileRequest {
  nickname?: string
  avatar?: string
  name?: string
  warningThreshold?: number
}

// 修改密码请求参数
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

/**
 * 用户注册
 */
export function register(data: RegisterRequest) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser(): Promise<UserInfo> {
  return request({
    url: '/auth/current',
    method: 'get'
  })
}

/**
 * 更新用户资料
 */
export function updateProfile(data: UpdateProfileRequest): Promise<UserInfo> {
  return request({
    url: '/auth/profile',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export function changePassword(data: ChangePasswordRequest) {
  return request({
    url: '/auth/password',
    method: 'put',
    data
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
