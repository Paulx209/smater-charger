/**
 * 用户状态管理
 */
import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { setToken, removeToken } from '@/utils/auth'
import {
  login as loginApi,
  logout as logoutApi,
  getCurrentUser,
  type LoginRequest,
  type UserInfo
} from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  /**
   * 登录
   */
  const login = async (loginForm: LoginRequest) => {
    try {
      const data = await loginApi(loginForm)

      // 保存token
      token.value = data.token
      setToken(data.token)

      // 保存用户信息
      userInfo.value = data.userInfo

      ElMessage.success('登录成功')
      return data
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  /**
   * 登出
   */
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除状态
      token.value = ''
      userInfo.value = null
      removeToken()
      ElMessage.success('已退出登录')
    }
  }

  /**
   * 获取用户信息
   */
  const getUserInfo = async () => {
    try {
      const data = await getCurrentUser()
      userInfo.value = data
      return data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  /**
   * 更新用户信息
   */
  const updateUserInfo = (data: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...data }
    }
  }

  return {
    token,
    userInfo,
    login,
    logout,
    getUserInfo,
    updateUserInfo
  }
})
