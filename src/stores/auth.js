import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/authApi'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => !!user.value && !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const userRole = computed(() => user.value?.role || 'GUEST')

  const initializeAuth = () => {
    const storedUser = localStorage.getItem('user') || sessionStorage.getItem('user')
    const storedToken = localStorage.getItem('token') || sessionStorage.getItem('token')

    if (storedUser && storedToken) {
      try {
        user.value = JSON.parse(storedUser)
        token.value = storedToken
      } catch (e) {
        clearAuth()
      }
    }
  }

  const login = async (credentials, rememberMe = false) => {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.login(credentials)
      if (!response?.success || !response?.user || !response?.token) {
        throw new Error(response?.message || '登录失败')
      }

      user.value = response.user
      token.value = response.token

      if (rememberMe) {
        localStorage.setItem('user', JSON.stringify(response.user))
        localStorage.setItem('token', response.token)
      } else {
        sessionStorage.setItem('user', JSON.stringify(response.user))
        sessionStorage.setItem('token', response.token)
      }

      return response
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const register = async (userData) => {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.register(userData)
      if (!response?.success) {
        throw new Error(response?.message || '注册失败')
      }
      return response
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '注册失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    loading.value = true
    error.value = null

    try {
      await authApi.logout()
    } catch (err) {
      // ignore
    } finally {
      clearAuth()
      loading.value = false
    }
  }

  const clearAuth = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    sessionStorage.removeItem('user')
    sessionStorage.removeItem('token')
  }

  const refreshUser = async () => {
    if (!token.value) return

    loading.value = true
    error.value = null

    try {
      const response = await authApi.getCurrentUser()
      if (!response?.success || !response?.user) {
        throw new Error(response?.message || '获取用户信息失败')
      }

      user.value = response.user
      const storage = localStorage.getItem('user') ? localStorage : sessionStorage
      storage.setItem('user', JSON.stringify(response.user))
      return response
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '获取用户信息失败'
      if (err.response?.status === 401) {
        clearAuth()
      }
      throw err
    } finally {
      loading.value = false
    }
  }

  initializeAuth()

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    isAdmin,
    userRole,
    login,
    register,
    logout,
    clearAuth,
    refreshUser
  }
})
