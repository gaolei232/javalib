import axios from 'axios'

const API_BASE_URL = '/api'

// 获取所有学生
export const getAllStudents = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/students`)
    return response.data
  } catch (error) {
    console.error('获取学生列表失败:', error)
    throw error
  }
}

// 根据ID获取学生
export const getStudentById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/students/${id}`)
    return response.data
  } catch (error) {
    console.error('获取学生详情失败:', error)
    throw error
  }
}

// 根据学生ID获取学生
export const getStudentByStudentId = async (studentId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/students/student-id/${studentId}`)
    return response.data
  } catch (error) {
    console.error('获取学生详情失败:', error)
    throw error
  }
}

// 创建学生
export const createStudent = async (studentData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/students`, studentData)
    return response.data
  } catch (error) {
    console.error('创建学生失败:', error)
    throw error
  }
}

// 更新学生
export const updateStudent = async (id, studentData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/students/${id}`, studentData)
    return response.data
  } catch (error) {
    console.error('更新学生失败:', error)
    throw error
  }
}

// 删除学生
export const deleteStudent = async (id) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/students/${id}`)
    return response.data
  } catch (error) {
    console.error('删除学生失败:', error)
    throw error
  }
}

export default {
  getAllStudents,
  getStudentById,
  getStudentByStudentId,
  createStudent,
  updateStudent,
  deleteStudent
}
