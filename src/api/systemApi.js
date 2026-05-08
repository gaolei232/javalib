import axios from 'axios'

const API_BASE_URL = '/api'

export const getConfig = async (key) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/system/config`, { params: { key } })
        return response.data
    } catch (error) {
        console.error('获取系统配置失败:', error)
        throw error
    }
}

export const updateConfig = async (key, value) => {
    try {
        const response = await axios.put(`${API_BASE_URL}/system/config`, { key, value })
        return response.data
    } catch (error) {
        console.error('更新系统配置失败:', error)
        throw error
    }
}

export default { getConfig, updateConfig }
