import axios from 'axios'

const API_BASE_URL = '/api'

// 获取所有学生统计
export const getAllStudentStats = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats`)
    return response.data
  } catch (error) {
    console.error('获取学生统计列表失败:', error)
    throw error
  }
}

// 根据ID获取学生统计
export const getStudentStatsById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats/${id}`)
    return response.data
  } catch (error) {
    console.error('获取学生统计详情失败:', error)
    throw error
  }
}

// 根据学生ID获取统计
export const getStudentStatsByStudentId = async (studentId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats/student/${studentId}`)
    return response.data
  } catch (error) {
    console.error('获取学生统计详情失败:', error)
    throw error
  }
}

// 根据学生姓名获取统计
export const getStudentStatsByName = async (name) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats/name/${name}`)
    return response.data
  } catch (error) {
    console.error('获取学生统计列表失败:', error)
    throw error
  }
}

// 获取预约次数最多的学生
export const getTopStudents = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats/top`)
    return response.data
  } catch (error) {
    console.error('获取热门学生失败:', error)
    throw error
  }
}

// 获取系统统计信息
export const getSystemStatistics = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats/system`)
    return response.data
  } catch (error) {
    console.error('获取系统统计失败:', error)
    throw error
  }
}

// 获取学生总数
export const getTotalStudents = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats/total-students`)
    return response.data
  } catch (error) {
    console.error('获取学生总数失败:', error)
    throw error
  }
}

// 获取总预约次数
export const getTotalBookings = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/student-stats/total-bookings`)
    return response.data
  } catch (error) {
    console.error('获取总预约次数失败:', error)
    throw error
  }
}

// 创建学生统计
export const createStudentStats = async (statsData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/student-stats`, statsData)
    return response.data
  } catch (error) {
    console.error('创建学生统计失败:', error)
    throw error
  }
}

// 更新学生统计
export const updateStudentStats = async (id, statsData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/student-stats/${id}`, statsData)
    return response.data
  } catch (error) {
    console.error('更新学生统计失败:', error)
    throw error
  }
}

// 记录预约
export const recordBooking = async (studentId, studentName, hours) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/student-stats/booking`, null, {
      params: { studentId, studentName, hours }
    })
    return response.data
  } catch (error) {
    console.error('记录预约失败:', error)
    throw error
  }
}

// 记录取消
export const recordCancellation = async (studentId, studentName) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/student-stats/cancellation`, null, {
      params: { studentId, studentName }
    })
    return response.data
  } catch (error) {
    console.error('记录取消失败:', error)
    throw error
  }
}

// 删除学生统计
export const deleteStudentStats = async (id) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/student-stats/${id}`)
    return response.data
  } catch (error) {
    console.error('删除学生统计失败:', error)
    throw error
  }
}

// 获取认证Token
const getAuthToken = () => {
  const user = localStorage.getItem('user') || sessionStorage.getItem('user')
  if (user) {
    try {
      const parsedUser = JSON.parse(user)
      return parsedUser.token
    } catch (e) {
      return ''
    }
  }
  return ''
}

// 配置axios默认headers
axios.defaults.headers.common['Authorization'] = `Bearer ${getAuthToken()}`

export default {
  getAllStudentStats,
  getStudentStatsById,
  getStudentStatsByStudentId,
  getStudentStatsByName,
  getTopStudents,
  getSystemStatistics,
  getTotalStudents,
  getTotalBookings,
  createStudentStats,
  updateStudentStats,
  recordBooking,
  recordCancellation,
  deleteStudentStats
}
