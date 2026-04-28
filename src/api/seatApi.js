import axios from 'axios'

const API_BASE_URL = '/api'

// 获取所有座位
export const getAllSeats = async (date, userId, buildingCode, floorNo) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats`, {
      params: {
        ...(date ? { date } : {}),
        ...(userId ? { userId } : {}),
        ...(buildingCode ? { buildingCode } : {}),
        ...(floorNo ? { floorNo } : {})
      }
    })
    return response.data
  } catch (error) {
    console.error('获取座位列表失败:', error)
    throw error
  }
}

// 根据ID获取座位
export const getSeatById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/${id}`)
    return response.data
  } catch (error) {
    console.error('获取座位详情失败:', error)
    throw error
  }
}

// 根据座位ID获取座位
export const getSeatBySeatId = async (seatId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/seat-id/${seatId}`)
    return response.data
  } catch (error) {
    console.error('获取座位详情失败:', error)
    throw error
  }
}

// 根据状态获取座位
export const getSeatsByStatus = async (status) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/status/${status}`)
    return response.data
  } catch (error) {
    console.error('获取座位列表失败:', error)
    throw error
  }
}

// 获取可用座位
export const getAvailableSeats = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/available`)
    return response.data
  } catch (error) {
    console.error('获取可用座位失败:', error)
    throw error
  }
}

// 获取全部预约记录（管理员）
export const getAllBookings = async (status) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/bookings`, {
      params: {
        ...(status ? { status } : {})
      }
    })
    return response.data
  } catch (error) {
    console.error('获取预约记录失败:', error)
    throw error
  }
}

// 获取某个用户当前预约座位
export const getUserBookedSeats = async (userId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/user/${userId}/bookings`)
    return response.data
  } catch (error) {
    console.error('获取用户预约座位失败:', error)
    throw error
  }
}

// 获取某个座位某天的预约记录
export const getSeatBookingsByDate = async (seatId, date) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/${seatId}/bookings`, {
      params: {
        ...(date ? { date } : {})
      }
    })
    return response.data
  } catch (error) {
    console.error('获取座位预约失败:', error)
    throw error
  }
}

// 预约座位
export const bookSeat = async (seatId, userId, date, startTime, endTime) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/seats/${seatId}/book`, null, {
      params: {
        userId,
        ...(date ? { date } : {}),
        ...(startTime ? { startTime } : {}),
        ...(endTime ? { endTime } : {})
      }
    })
    return response.data
  } catch (error) {
    console.error('预约座位失败:', error)
    throw error
  }
}

// 取消预约（推荐：按 bookingId）
export const cancelBookingById = async (seatId, bookingId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/seats/${seatId}/cancel`, null, {
      params: {
        bookingId
      }
    })
    return response.data
  } catch (error) {
    console.error('按 bookingId 取消预约失败:', error)
    throw error
  }
}

// 取消预约（兼容：按座位+日期+时间段）
export const cancelBooking = async (seatId, date, startTime, endTime) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/seats/${seatId}/cancel`, null, {
      params: {
        ...(date ? { date } : {}),
        ...(startTime ? { startTime } : {}),
        ...(endTime ? { endTime } : {})
      }
    })
    return response.data
  } catch (error) {
    console.error('取消预约失败:', error)
    throw error
  }
}

// 重置座位
export const resetSeat = async (seatId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/seats/${seatId}/reset`)
    return response.data
  } catch (error) {
    console.error('重置座位失败:', error)
    throw error
  }
}

// 获取座位统计信息
export const getSeatStatistics = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/seats/statistics`)
    return response.data
  } catch (error) {
    console.error('获取座位统计失败:', error)
    throw error
  }
}

// 初始化座位
export const initializeSeats = async () => {
  try {
    const response = await axios.post(`${API_BASE_URL}/seats/initialize`)
    return response.data
  } catch (error) {
    console.error('初始化座位失败:', error)
    throw error
  }
}

// 创建座位
export const createSeat = async (seatData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/seats`, seatData)
    return response.data
  } catch (error) {
    console.error('创建座位失败:', error)
    throw error
  }
}

// 更新座位
export const updateSeat = async (id, seatData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/seats/${id}`, seatData)
    return response.data
  } catch (error) {
    console.error('更新座位失败:', error)
    throw error
  }
}

// 删除座位
export const deleteSeat = async (id) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/seats/${id}`)
    return response.data
  } catch (error) {
    console.error('删除座位失败:', error)
    throw error
  }
}

const getAuthToken = () =>
  localStorage.getItem('token') || sessionStorage.getItem('token') || ''

axios.interceptors.request.use(
  (config) => {
    const token = getAuthToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

export default {
  getAllSeats,
  getAllBookings,
  getSeatById,
  getSeatBySeatId,
  getSeatsByStatus,
  getAvailableSeats,
  getUserBookedSeats,
  getSeatBookingsByDate,
  bookSeat,
  cancelBooking,
  cancelBookingById,
  resetSeat,
  getSeatStatistics,
  initializeSeats,
  createSeat,
  updateSeat,
  deleteSeat
}
