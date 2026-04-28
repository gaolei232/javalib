class WebSocketManager {
  constructor() {
    this.socket = null
    this.listeners = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectTimer = null
    this.currentUrl = ''
    this.manuallyClosed = false
  }

  connect(url) {
    if (!url) return

    if (
      this.socket &&
      (this.socket.readyState === WebSocket.OPEN ||
        this.socket.readyState === WebSocket.CONNECTING)
    ) {
      return
    }

    this.currentUrl = url
    this.manuallyClosed = false
    this.clearReconnectTimer()

    this.socket = new WebSocket(url)

    this.socket.onopen = () => {
      console.log('WebSocket连接已建立')
      this.reconnectAttempts = 0
    }

    this.socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this.emit(data.type, data)
      } catch (error) {
        console.error('WebSocket消息解析失败:', error)
      }
    }

    this.socket.onclose = () => {
      console.log('WebSocket连接已关闭')

      const shouldReconnect = !this.manuallyClosed
      this.socket = null

      if (shouldReconnect) {
        this.reconnect()
      }
    }

    this.socket.onerror = (error) => {
      console.error('WebSocket错误:', error)
    }
  }

  reconnect() {
    if (this.manuallyClosed) return
    if (!this.currentUrl) return
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return

    this.reconnectAttempts++
    this.clearReconnectTimer()

    this.reconnectTimer = setTimeout(() => {
      console.log(
        `尝试重新连接... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`
      )
      this.connect(this.currentUrl)
    }, 3000 * this.reconnectAttempts)
  }

  clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  on(event, callback) {
    if (!event || typeof callback !== 'function') return

    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }

    const currentListeners = this.listeners.get(event)
    const exists = currentListeners.includes(callback)

    if (!exists) {
      currentListeners.push(callback)
    }
  }

  off(event, callback) {
    if (!this.listeners.has(event)) return

    if (!callback) {
      this.listeners.delete(event)
      return
    }

    const nextListeners = this.listeners
      .get(event)
      .filter((listener) => listener !== callback)

    if (nextListeners.length === 0) {
      this.listeners.delete(event)
    } else {
      this.listeners.set(event, nextListeners)
    }
  }

  emit(event, data) {
    if (!this.listeners.has(event)) return

    const callbacks = [...this.listeners.get(event)]
    callbacks.forEach((callback) => {
      try {
        callback(data)
      } catch (error) {
        console.error(`WebSocket事件回调执行失败: ${event}`, error)
      }
    })
  }

  send(message) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message))
    }
  }

  disconnect() {
    this.manuallyClosed = true
    this.clearReconnectTimer()

    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
  }
}

export const wsManager = new WebSocketManager()