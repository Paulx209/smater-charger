/**
 * Token管理工具
 */

const TOKEN_KEY = 'token'

/**
 * 获取Token
 */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 删除Token
 */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 判断是否已登录
 */
export function isAuthenticated(): boolean {
  return !!getToken()
}
