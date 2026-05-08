import axios from 'axios'

const API_BASE_URL = '/api'

// 获取所有用户
export const getAllUsers = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/users`)
    return response.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
    throw error
  }
}

// 根据ID获取用户
export const getUserById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/users/${id}`)
    return response.data
  } catch (error) {
    console.error('获取用户详情失败:', error)
    throw error
  }
}

// 创建用户
export const createUser = async (userData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/users`, userData)
    return response.data
  } catch (error) {
    console.error('创建用户失败:', error)
    throw error
  }
}

// 更新用户
export const updateUser = async (id, userData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/users/${id}`, userData)
    return response.data
  } catch (error) {
    console.error('更新用户失败:', error)
    throw error
  }
}

// 获取用户诚信指数
export const getUserIntegrity = async (id) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/users/${id}/integrity`)
    return response.data
  } catch (error) {
    console.error('获取用户诚信指数失败:', error)
    throw error
  }
}

// 删除用户
export const deleteUser = async (id) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/users/${id}`)
    return response.data
  } catch (error) {
    console.error('删除用户失败:', error)
    throw error
  }
}

export default {
  getAllUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  getUserIntegrity
}
