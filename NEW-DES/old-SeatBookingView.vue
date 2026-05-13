<template>
  <div class="booking-page">
    <header class="booking-header">
      <div class="brand">
        <span class="brand-mark"></span>
        <div>
          <p class="brand-name">考研图书馆</p>
          <p class="brand-tagline">座位预约中心</p>
        </div>
      </div>

      <nav class="booking-nav">
        <button class="nav-item" :class="{ active: activeSection === 'seats' }" @click="activeSection = 'seats'">
          座位预约
        </button>
        <button class="nav-item" :class="{ active: activeSection === 'bookings' }" @click="activeSection = 'bookings'">
          我的预约
        </button>
        <button class="nav-item" :class="{ active: activeSection === 'profile' }" @click="activeSection = 'profile'">
          账号信息
        </button>
      </nav>

      <div class="header-actions">
        <span class="welcome-text">欢迎，{{ userName }}</span>
        <button class="ghost-button" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <main class="booking-content">
      <section v-if="activeSection === 'seats'" class="content-card">
        <div class="card-header">
          <div>
            <h2>座位预约</h2>
            <p>选择楼栋、楼层、日期和时段，实时查看座位状态。</p>
          </div>
          <div class="date-selector">
            <div class="selector-card">
              <div class="selector-head">
                <p class="selector-title">空间筛选</p>
                <span class="selector-current">当前：{{ selectedBuildingCode }} · {{ selectedFloorNo }}层</span>
              </div>
              <div class="selector-grid">
                <label class="selector-field" for="building-select">
                  <span>楼栋</span>
                  <select id="building-select" v-model="selectedBuildingCode">
                    <option v-for="building in buildingOptions" :key="building.value" :value="building.value">
                      {{ building.label }}
                    </option>
                  </select>
                </label>
                <label class="selector-field" for="floor-select">
                  <span>楼层</span>
                  <select id="floor-select" v-model.number="selectedFloorNo">
                    <option v-for="floor in floorOptions" :key="floor" :value="floor">
                      {{ floor }} 层
                    </option>
                  </select>
                </label>
                <label class="selector-field" for="booking-date">
                  <span>预约日期</span>
                  <input id="booking-date" v-model="selectedDate" type="date" :min="minDate" />
                </label>
              </div>
            </div>
          </div>
        </div>

        <div class="seat-status-legend">
          <div class="legend-item"><span class="legend-dot available"></span>可预约</div>
          <div class="legend-item"><span class="legend-dot partially-booked"></span>部分占用</div>
          <div class="legend-item"><span class="legend-dot booked-by-me"></span>我有预约</div>
          <div class="legend-item"><span class="legend-dot full"></span>已满</div>
        </div>

        <div class="feature-filter-panel">
          <div class="filter-card">
            <div class="filter-card-head">
              <p class="filter-card-title">座位筛选</p>
              <span class="filter-card-hint">按属性快速筛选可见座位</span>
            </div>
            <div class="filter-grid">
              <label class="filter-field">
                <span>靠窗</span>
                <select v-model="seatFeatureFilter.nearWindow">
                  <option value="all">全部</option>
                  <option value="yes">仅靠窗</option>
                  <option value="no">不靠窗</option>
                </select>
              </label>
              <label class="filter-field">
                <span>插座</span>
                <select v-model="seatFeatureFilter.hasOutlet">
                  <option value="all">全部</option>
                  <option value="yes">有插座</option>
                  <option value="no">无插座</option>
                </select>
              </label>
              <label class="filter-field">
                <span>安静区</span>
                <select v-model="seatFeatureFilter.quietZone">
                  <option value="all">全部</option>
                  <option value="yes">仅安静区</option>
                  <option value="no">非安静区</option>
                </select>
              </label>
            </div>
          </div>
        </div>

        <div class="booking-time-panel compact">
          <div class="time-selectors inline">
            <label>
              开始时间
              <select v-model="selectedStartTimeOption">
                <option value="" disabled>选择开始时间</option>
                <option v-for="time in availableStartTimes" :key="time" :value="time">{{ time }}</option>
              </select>
            </label>
            <label>
              结束时间
              <select v-model="selectedEndTimeOption" :disabled="!selectedStartTimeOption">
                <option value="" disabled>选择结束时间</option>
                <option v-for="time in availableEndTimes" :key="time" :value="time">{{ time }}</option>
              </select>
            </label>
          </div>
        </div>

        <div class="seat-layout">
          <div class="seat-map">
            <div class="row" v-for="row in seatRows" :key="row">
              <div class="row-label">{{ row }}</div>
              <div
                v-for="col in seatColumns"
                :key="`${row}-${col}`"
                :class="['seat', getSeatUiStatus(row, col), { selected: isSeatSelected(row, col) }]"
                :title="getSeatTooltip(row, col)"
                @click="handleSeatClick(row, col)"
              >
                <span class="seat-number">{{ col }}</span>
                <span class="seat-state-text">{{ getSeatStatusText(row, col) }}</span>
              </div>
            </div>
          </div>

          <aside class="seat-detail-panel">
            <div class="panel-header">
              <h3>座位详情</h3>
              <span class="panel-hint">点击座位查看</span>
            </div>

            <div v-if="selectedSeatId" class="panel-body">
              <div class="seat-info-card">
                <div class="seat-info-main">
                  <p class="seat-code">{{ selectedBuildingCode }}-F{{ selectedFloorNo }}-{{ selectedSeatRow }}-{{ selectedSeatCol }}</p>
                  <p class="seat-state-line">
                    <span class="muted-label">当前状态</span>
                    <span class="status-pill">{{ selectedSeatSummaryText }}</span>
                  </p>
                </div>
              </div>

              <div class="seat-feature-list" v-if="selectedSeatMeta">
                <span class="feature-tag">{{ selectedSeatMeta.nearWindow ? '靠窗' : '非靠窗' }}</span>
                <span class="feature-tag">{{ selectedSeatMeta.hasOutlet ? '有插座' : '无插座' }}</span>
                <span class="feature-tag">{{ selectedSeatMeta.quietZone ? '安静区' : '普通区' }}</span>
              </div>

              <div class="booking-list-card">
                <div class="subsection-header">
                  <h4>已预约时段</h4>
                  <span class="subtle-text">{{ seatDayBookings.length }} 条</span>
                </div>

                <div v-if="seatDayBookings.length === 0" class="empty-state small">暂无预约时段</div>

                <div v-else class="booking-slot-list">
                  <div
                    v-for="booking in seatDayBookings"
                    :key="booking.id"
                    class="booking-slot-item"
                    :class="{ mine: booking.userId === currentUserId }"
                  >
                    <div class="slot-main">
                      <strong>{{ booking.startTime }} - {{ booking.endTime }}</strong>
                    </div>
                    <span class="slot-owner">{{ booking.userId === currentUserId ? '我的预约' : '已预约' }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-actions">
                <button class="primary-action" :disabled="!canSubmitBooking" @click="handleBooking">立即预约</button>
                <button class="ghost-button" :disabled="!selectedBookingOwnedByUser" @click="handleCancelSelectedBooking">
                  取消我在该座位的预约
                </button>
              </div>
            </div>

            <div v-else class="empty-state">请选择座位查看详情</div>
          </aside>
        </div>

        <div class="booking-timeline-bar">
          <div class="timeline-header">
            <h4>当前座位当日时间分布</h4>
            <span class="timeline-hint">08:00 - 22:00</span>
          </div>
          <div class="timeline-track">
            <div
              v-for="hour in timelineHours"
              :key="hour"
              class="timeline-segment"
              :class="getTimelineClass(hour)"
            >
              <span class="segment-label">{{ String(hour).padStart(2, '0') }}</span>
            </div>
          </div>
        </div>
      </section>

      <section v-if="activeSection === 'bookings'" class="content-card">
        <div class="card-header">
          <div>
            <h2>我的预约</h2>
            <p>查看当前与历史预约记录。</p>
          </div>
        </div>

        <div v-if="userBookings.length === 0" class="empty-state">暂无预约记录</div>
        <div v-else class="booking-list">
          <div v-for="booking in userBookings" :key="booking.id" class="booking-item">
            <div>
              <h4>{{ booking.seatId }}</h4>
              <p>{{ formatDate(booking.date) }} · {{ booking.startTime }} - {{ booking.endTime }}</p>
            </div>
            <div class="booking-meta">
              <span class="status-pill">{{ getStatusLabel(booking.status) }}</span>
              <button
                v-if="booking.status === 'BOOKED' && booking.date >= today"
                class="ghost-button"
                @click="handleCancelBookingById(booking.id)"
              >
                取消
              </button>
            </div>
          </div>
        </div>
      </section>

      <section v-if="activeSection === 'profile'" class="content-card">
        <div class="card-header">
          <div>
            <h2>账号信息</h2>
            <p>查看当前登录账号信息。</p>
          </div>
        </div>
        <div class="profile-grid">
          <div>
            <span class="label">姓名</span>
            <p>{{ authStore.user?.name || '未命名' }}</p>
          </div>
          <div>
            <span class="label">邮箱</span>
            <p>{{ authStore.user?.email || '-' }}</p>
          </div>
          <div>
            <span class="label">角色</span>
            <p>{{ authStore.user?.role || 'USER' }}</p>
          </div>
        </div>
      </section>
    </main>

    <div v-if="showNotification" class="notification" :class="notificationType">
      <span>{{ notificationMessage }}</span>
      <button class="close-notification" @click="showNotification = false">&times;</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import * as seatApi from '../api/seatApi'
import { wsManager } from '../utils/websocket'

const router = useRouter()
const authStore = useAuthStore()

const activeSection = ref('seats')
const selectedDate = ref(new Date().toISOString().split('T')[0])
const selectedBuildingCode = ref('B1')
const selectedFloorNo = ref(1)
const selectedStartTimeOption = ref('')
const selectedEndTimeOption = ref('')
const selectedSeatId = ref('')
const selectedSeatRow = ref('')
const selectedSeatCol = ref('')
const seats = ref([])
const seatFeatureFilter = ref({
  nearWindow: 'all',
  hasOutlet: 'all',
  quietZone: 'all'
})
const seatDayBookings = ref([])
const userBookings = ref([])
const availableStartTimes = ref([])
const availableEndTimes = ref([])
const showNotification = ref(false)
const notificationMessage = ref('')
const notificationType = ref('success')
const autoRefreshTimer = ref(null)

const buildingOptions = [
  { label: '1号楼', value: 'B1' },
  { label: '2号楼', value: 'B2' },
  { label: '3号楼', value: 'B3' }
]
const floorOptions = [1, 2, 3]

const seatRows = ref(['A', 'B', 'C', 'D', 'E'])
const seatColumns = ref([1, 2, 3, 4, 5])
const timelineHours = Array.from({ length: 14 }, (_, index) => index + 8)

const minDate = computed(() => new Date().toISOString().split('T')[0])
const today = computed(() => new Date().toISOString().split('T')[0])
const userName = computed(() => authStore.user?.name || '用户')
const currentUserId = computed(() => String(authStore.user?.id || ''))

const selectedSeatSummaryText = computed(() => {
  if (!selectedSeatId.value) return '未选择'
  if (seatDayBookings.value.length === 0) return '当日可预约'

  const myCount = seatDayBookings.value.filter(item => item.userId === currentUserId.value).length
  const daySlots = generateTimeSlots()
  const fullyBlocked = isFullyBlocked(daySlots, seatDayBookings.value)

  if (fullyBlocked) return myCount > 0 ? '该日已满（含我的预约）' : '该日已满'
  if (myCount > 0) return '部分占用（含我的预约）'
  return '部分占用'
})

const selectedSeatMeta = computed(() => {
  if (!selectedSeatId.value) return null
  return seats.value.find(item => item.seatId === selectedSeatId.value) || null
})

const selectedBookingOwnedByUser = computed(() => {
  if (!selectedSeatId.value) return false
  return seatDayBookings.value.some(item => item.userId === currentUserId.value)
})

const canSubmitBooking = computed(() => {
  if (!selectedSeatId.value) return false
  if (!selectedStartTimeOption.value || !selectedEndTimeOption.value) return false

  const earliestStart = getEarliestSelectableStartTime()
  if (selectedDate.value === today.value && selectedStartTimeOption.value < earliestStart) {
    return false
  }

  return !hasOverlap(selectedStartTimeOption.value, selectedEndTimeOption.value, seatDayBookings.value)
})

let latestSeatRequestToken = 0
let latestSeatsRequestToken = 0
let seatStatusUpdateHandler = null

onMounted(async () => {
  await initializePage()
  connectWebSocket()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  if (seatStatusUpdateHandler && typeof wsManager.off === 'function') {
    wsManager.off('seatStatusUpdate', seatStatusUpdateHandler)
  }
  wsManager.disconnect()
})

watch([selectedDate, selectedBuildingCode, selectedFloorNo], async () => {
  resetSeatSelection()
  await Promise.allSettled([loadSeats(), loadUserBookings()])
})

watch(selectedSeatId, async () => {
  selectedStartTimeOption.value = ''
  selectedEndTimeOption.value = ''

  if (!selectedSeatId.value) {
    seatDayBookings.value = []
    availableStartTimes.value = []
    availableEndTimes.value = []
    return
  }

  await loadSeatDayBookings()
})

watch(selectedStartTimeOption, () => {
  selectedEndTimeOption.value = ''
  updateAvailableEndTimes()
})

watch(
  seatFeatureFilter,
  () => {
    if (selectedSeatId.value && selectedSeatMeta.value && !matchesSeatFeatureFilter(selectedSeatMeta.value)) {
      resetSeatSelection()
    }
  },
  { deep: true }
)

async function initializePage() {
  await Promise.allSettled([loadSeats(), loadUserBookings()])
}

function startAutoRefresh() {
  stopAutoRefresh()
  autoRefreshTimer.value = setInterval(async () => {
    if (activeSection.value !== 'seats') return

    await Promise.allSettled([
      loadSeats(),
      loadUserBookings(),
      selectedSeatId.value ? loadSeatDayBookings() : Promise.resolve()
    ])

    if (selectedSeatId.value) {
      updateAvailableStartTimes()
      updateAvailableEndTimes()
    }
  }, 30 * 1000)
}

function stopAutoRefresh() {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
    autoRefreshTimer.value = null
  }
}

function connectWebSocket() {
  wsManager.connect('ws://localhost:8080/ws/seat-status')

  seatStatusUpdateHandler = (data) => {
    if (data?.seat) {
      updateSeatStatus(data.seat)
    }
  }

  wsManager.on('seatStatusUpdate', seatStatusUpdateHandler)
}

function normalizeBooking(raw) {
  return {
    id: raw.id,
    seatId: String(raw.seatId || ''),
    date: raw.bookingDate || raw.date || '',
    startTime: normalizeTime(raw.startTime),
    endTime: normalizeTime(raw.endTime),
    status: raw.status || 'BOOKED',
    userId: String(raw.userId || ''),
    createdAt: raw.createdAt || ''
  }
}

function normalizeTime(value) {
  if (!value) return ''
  return String(value).slice(0, 5)
}

function updateSeatStatus(updatedSeat) {
  if (!updatedSeat?.seatId) return
  if (updatedSeat.buildingCode !== selectedBuildingCode.value || Number(updatedSeat.floorNo) !== Number(selectedFloorNo.value)) {
    return
  }

  const index = seats.value.findIndex(item => item.seatId === updatedSeat.seatId)
  if (index === -1) {
    seats.value = [...seats.value, updatedSeat]
    return
  }

  const next = [...seats.value]
  next[index] = { ...next[index], ...updatedSeat }
  seats.value = next
}

function parseSeatLocationFromSeatId(seatId) {
  if (!seatId) return null
  const match = String(seatId).match(/^(B\d+)-F(\d+)-/i)
  if (!match) return null
  return {
    buildingCode: match[1].toUpperCase(),
    floorNo: Number(match[2])
  }
}

function isSeatInSelectedLocation(seat) {
  if (!seat) return false

  const seatBuildingCode = String(seat.buildingCode || '').toUpperCase()
  const seatFloorNo = Number(seat.floorNo)

  if (seatBuildingCode && Number.isFinite(seatFloorNo) && seatFloorNo > 0) {
    return seatBuildingCode === selectedBuildingCode.value && seatFloorNo === Number(selectedFloorNo.value)
  }

  const parsed = parseSeatLocationFromSeatId(seat.seatId)
  if (!parsed) {
    // Legacy ids like "A-1" are ambiguous across building/floor and must be excluded.
    return false
  }

  return parsed.buildingCode === selectedBuildingCode.value && parsed.floorNo === Number(selectedFloorNo.value)
}

async function loadSeats() {
  const requestToken = ++latestSeatsRequestToken
  try {
    const allSeats = await seatApi.getAllSeats(
      selectedDate.value,
      currentUserId.value,
      selectedBuildingCode.value,
      selectedFloorNo.value
    )
    if (requestToken !== latestSeatsRequestToken) return
    const normalizedSeats = Array.isArray(allSeats) ? allSeats : []
    seats.value = normalizedSeats.filter(isSeatInSelectedLocation)
  } catch (error) {
    if (requestToken !== latestSeatsRequestToken) return
    seats.value = []
    showNotificationMessage('加载座位失败', 'error')
  }
}

async function loadUserBookings() {
  try {
    if (!currentUserId.value) {
      userBookings.value = []
      return
    }

    const bookings = await seatApi.getUserBookedSeats(currentUserId.value)
    userBookings.value = (Array.isArray(bookings) ? bookings : []).map(normalizeBooking)
  } catch (error) {
    userBookings.value = []
  }
}

async function loadSeatDayBookings() {
  if (!selectedSeatId.value) return

  const requestToken = ++latestSeatRequestToken

  try {
    const bookings = await seatApi.getSeatBookingsByDate(selectedSeatId.value, selectedDate.value)
    if (requestToken !== latestSeatRequestToken) return

    seatDayBookings.value = (Array.isArray(bookings) ? bookings : [])
      .map(normalizeBooking)
      .filter(item => item.status === 'BOOKED')
      .sort((a, b) => a.startTime.localeCompare(b.startTime))

    updateAvailableStartTimes()
  } catch (error) {
    if (requestToken !== latestSeatRequestToken) return
    seatDayBookings.value = []
    updateAvailableStartTimes()
    availableEndTimes.value = []
  }
}

function generateTimeSlots() {
  const slots = []
  const startMinutes = 8 * 60
  const endMinutes = 22 * 60

  for (let minute = startMinutes; minute <= endMinutes; minute += 30) {
    const hour = String(Math.floor(minute / 60)).padStart(2, '0')
    const min = String(minute % 60).padStart(2, '0')
    slots.push(`${hour}:${min}`)
  }

  return slots
}

function roundUpToNextHalfHour(date = new Date()) {
  const rounded = new Date(date)
  rounded.setSeconds(0)
  rounded.setMilliseconds(0)

  const minutes = rounded.getMinutes()
  if (minutes === 0 || minutes === 30) {
    return `${String(rounded.getHours()).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
  }

  if (minutes < 30) {
    rounded.setMinutes(30)
  } else {
    rounded.setHours(rounded.getHours() + 1)
    rounded.setMinutes(0)
  }

  return `${String(rounded.getHours()).padStart(2, '0')}:${String(rounded.getMinutes()).padStart(2, '0')}`
}

function getEarliestSelectableStartTime() {
  if (selectedDate.value !== today.value) {
    return '08:00'
  }

  const nowRounded = roundUpToNextHalfHour(new Date())
  if (nowRounded < '08:00') return '08:00'
  if (nowRounded > '21:30') return '22:00'
  return nowRounded
}

function toMinutes(time) {
  const [hour, minute] = String(time).split(':').map(Number)
  return hour * 60 + minute
}

function intervalsOverlap(startA, endA, startB, endB) {
  return toMinutes(startA) < toMinutes(endB) && toMinutes(startB) < toMinutes(endA)
}

function hasOverlap(startTime, endTime, bookings) {
  return bookings.some(item => intervalsOverlap(startTime, endTime, item.startTime, item.endTime))
}

function isFullyBlocked(allSlots, bookings) {
  const candidateStarts = allSlots.slice(0, -1)
  return candidateStarts.every(start => {
    const startIndex = allSlots.indexOf(start)
    const next = allSlots[startIndex + 1]
    return hasOverlap(start, next, bookings)
  })
}

function updateAvailableStartTimes() {
  const allSlots = generateTimeSlots()
  const starts = allSlots.slice(0, -1)
  const earliestStart = getEarliestSelectableStartTime()

  availableStartTimes.value = starts.filter(start => {
    if (start < earliestStart) return false

    const startIndex = allSlots.indexOf(start)
    const end = allSlots[startIndex + 1]
    return !hasOverlap(start, end, seatDayBookings.value)
  })

  if (!availableStartTimes.value.includes(selectedStartTimeOption.value)) {
    selectedStartTimeOption.value = ''
  }

  updateAvailableEndTimes()
}

function updateAvailableEndTimes() {
  if (!selectedStartTimeOption.value) {
    availableEndTimes.value = []
    return
  }

  const allSlots = generateTimeSlots()
  const startIndex = allSlots.indexOf(selectedStartTimeOption.value)

  if (startIndex === -1) {
    availableEndTimes.value = []
    return
  }

  const candidates = []
  for (let i = startIndex + 1; i < allSlots.length; i += 1) {
    const end = allSlots[i]
    const hasConflict = hasOverlap(selectedStartTimeOption.value, end, seatDayBookings.value)
    if (hasConflict) break
    candidates.push(end)
  }

  availableEndTimes.value = candidates

  if (!availableEndTimes.value.includes(selectedEndTimeOption.value)) {
    selectedEndTimeOption.value = ''
  }
}

function getSeatByPosition(row, col) {
  return seats.value.find(item => item.row === row && Number(item.col) === Number(col)) || null
}

function matchBooleanFilter(filterValue, actualValue) {
  if (filterValue === 'all') return true
  if (filterValue === 'yes') return Boolean(actualValue)
  return !Boolean(actualValue)
}

function matchesSeatFeatureFilter(seat) {
  if (!seat) return false
  return (
    matchBooleanFilter(seatFeatureFilter.value.nearWindow, seat.nearWindow) &&
    matchBooleanFilter(seatFeatureFilter.value.hasOutlet, seat.hasOutlet) &&
    matchBooleanFilter(seatFeatureFilter.value.quietZone, seat.quietZone)
  )
}

function getSeatBookingsForDay(seatId) {
  return userBookings.value.filter(
    item =>
      item.seatId === seatId &&
      item.date === selectedDate.value &&
      String(item.status || '').toUpperCase() === 'BOOKED'
  )
}

function getSeatUiStatus(row, col) {
  const seat = getSeatByPosition(row, col)
  if (!seat) return 'available'
  if (!matchesSeatFeatureFilter(seat)) return 'filtered-out'

  const myBookings = getSeatBookingsForDay(seat.seatId)
  if (myBookings.length > 0) return 'booked-by-me'

  const status = String(seat.status || 'AVAILABLE')
  if (status === 'FUTURE_BOOKED' || status === 'BOOKED') return 'partially-booked'
  if (status === 'USED') return 'full'
  return 'available'
}

function getSeatStatusText(row, col) {
  const seat = getSeatByPosition(row, col)
  if (!seat) return '可约'
  if (!matchesSeatFeatureFilter(seat)) return '已筛除'

  const myBookings = getSeatBookingsForDay(seat.seatId)
  if (myBookings.length > 0) return '我已约'

  const map = {
    AVAILABLE: '可约',
    BOOKED: '部分占用',
    FUTURE_BOOKED: '未来占用',
    USED: '已满'
  }

  return map[seat.status] || '可约'
}

function getSeatTooltip(row, col) {
  const seat = getSeatByPosition(row, col)
  const displayCode = `${selectedBuildingCode.value}-F${selectedFloorNo.value}-${row}-${col}`

  if (!seat) return `座位 ${displayCode}\n状态：可预约`
  if (!matchesSeatFeatureFilter(seat)) return `座位 ${displayCode}\n状态：不满足筛选条件`

  const myBookings = getSeatBookingsForDay(seat.seatId)
  if (myBookings.length > 0) {
    const times = myBookings.map(item => `${item.startTime}-${item.endTime}`).join('，')
    return `座位 ${displayCode}\n状态：我已预约\n时段：${times}`
  }

  return `座位 ${displayCode}\n状态：${getStatusLabel(seat.status)}`
}

function isSeatSelected(row, col) {
  const seat = getSeatByPosition(row, col)
  return !!seat && selectedSeatId.value === seat.seatId
}

function handleSeatClick(row, col) {
  const seat = getSeatByPosition(row, col)
  if (!seat) return
  if (!matchesSeatFeatureFilter(seat)) return

  selectedSeatId.value = seat.seatId
  selectedSeatRow.value = row
  selectedSeatCol.value = String(col)
}

function resetSeatSelection() {
  // Invalidate pending day-booking requests to prevent stale data from older seat/floor context.
  latestSeatRequestToken += 1
  selectedSeatId.value = ''
  selectedSeatRow.value = ''
  selectedSeatCol.value = ''
  seatDayBookings.value = []
  availableStartTimes.value = []
  availableEndTimes.value = []
  selectedStartTimeOption.value = ''
  selectedEndTimeOption.value = ''
}

async function handleBooking() {
  if (!selectedSeatId.value) {
    showNotificationMessage('请先选择座位', 'error')
    return
  }

  if (!currentUserId.value) {
    showNotificationMessage('请先登录后再预约', 'error')
    return
  }

  if (!canSubmitBooking.value) {
    showNotificationMessage('所选时间段不可预约，请重新选择', 'error')
    return
  }

  let result
  try {
    result = await seatApi.bookSeat(
      selectedSeatId.value,
      currentUserId.value,
      selectedDate.value,
      selectedStartTimeOption.value,
      selectedEndTimeOption.value
    )
  } catch (error) {
    showNotificationMessage('预约失败，请重试', 'error')
    return
  }

  if (result?.success === false) {
    showNotificationMessage(result.message || '预约失败，请重试', 'error')
    return
  }

  showNotificationMessage('预约成功', 'success')

  await Promise.allSettled([
    loadSeats(),
    loadUserBookings(),
    loadSeatDayBookings()
  ])

  selectedStartTimeOption.value = ''
  selectedEndTimeOption.value = ''
}

async function handleCancelSelectedBooking() {
  if (!selectedSeatId.value) return

  const target = seatDayBookings.value.find(item => item.userId === currentUserId.value)
  if (!target) {
    showNotificationMessage('未找到当前座位的个人预约记录', 'error')
    return
  }

  await cancelBookingRecord(target)
}

async function handleCancelBookingById(bookingId) {
  const target = userBookings.value.find(item => item.id === bookingId)
  if (!target) {
    showNotificationMessage('未找到对应预约记录', 'error')
    return
  }

  await cancelBookingRecord(target)
}

async function cancelBookingRecord(booking) {
  try {
    await seatApi.cancelBookingById(booking.seatId, booking.id)
    showNotificationMessage('取消预约成功', 'success')

    await Promise.allSettled([
      loadSeats(),
      loadUserBookings(),
      selectedSeatId.value === booking.seatId ? loadSeatDayBookings() : Promise.resolve()
    ])
  } catch (error) {
    showNotificationMessage('取消预约失败，请重试', 'error')
  }
}

async function handleLogout() {
  userBookings.value = []
  seats.value = []
  resetSeatSelection()
  await authStore.logout()
  router.push('/login')
}

function showNotificationMessage(message, type = 'success') {
  notificationMessage.value = message
  notificationType.value = type
  showNotification.value = true

  setTimeout(() => {
    showNotification.value = false
  }, 3000)
}

function getStatusLabel(status) {
  const statusMap = {
    AVAILABLE: '可用',
    BOOKED: '已预约',
    FUTURE_BOOKED: '未来预约',
    USED: '已使用',
    CANCELLED: '已取消',
    EXPIRED: '已过期'
  }
  return statusMap[status] || status || '-'
}

function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

function getTimelineClass(hour) {
  if (!selectedSeatId.value) return 'available'
  const slotStart = `${String(hour).padStart(2, '0')}:00`
  const slotEnd = `${String(hour + 1).padStart(2, '0')}:00`

  const overlapping = seatDayBookings.value.filter(item =>
    intervalsOverlap(slotStart, slotEnd, item.startTime, item.endTime)
  )

  if (overlapping.length === 0) return 'available'
  if (overlapping.some(item => item.userId === currentUserId.value)) return 'booked-by-me'
  return 'booked'
}
</script>

<style scoped>
@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes notificationPop {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes pulseRing {
  0% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.22);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(99, 102, 241, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0);
  }
}

.booking-page {
  min-height: 100vh;
  background: var(--surface-base);
  padding: 2rem 5vw 3rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.booking-header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 1.5rem;
  background: var(--surface-card);
  padding: 1.5rem 2rem;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  animation: fadeSlideUp 0.32s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.brand-mark {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: var(--brand-gradient);
}

.brand-name {
  font-weight: 700;
  color: var(--text-primary);
}

.brand-tagline {
  color: var(--text-subtle);
  font-size: 0.85rem;
}

.booking-nav {
  display: flex;
  gap: 0.75rem;
  background: var(--surface-elevated);
  padding: 0.4rem;
  border-radius: 999px;
}

.nav-item {
  background: transparent;
  box-shadow: none;
  color: var(--text-muted);
  padding: 0.5rem 1.4rem;
  border-radius: 999px;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease,
    color 0.18s ease;
}

.nav-item:hover {
  transform: translateY(-1px);
}

.nav-item:active {
  transform: translateY(0) scale(0.98);
}

.nav-item.active {
  background: #fff;
  color: var(--text-primary);
  box-shadow: var(--shadow-border);
}

.header-actions {
  justify-self: end;
  display: flex;
  align-items: center;
  gap: 1rem;
  color: var(--text-muted);
}

.ghost-button {
  background: #fff;
  color: var(--brand-accent);
  box-shadow: var(--shadow-border);
}

.primary-action {
  background: var(--brand-accent, #4f46e5);
  color: #fff;
}

.primary-action,
.ghost-button {
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease,
    color 0.18s ease;
}

.primary-action:hover,
.ghost-button:hover {
  transform: translateY(-1px);
}

.primary-action:active,
.ghost-button:active {
  transform: translateY(0) scale(0.98);
}

.booking-content {
  display: grid;
  gap: 2rem;
}

.content-card,
.seat-detail-panel,
.booking-time-panel,
.booking-timeline-bar {
  animation: fadeSlideUp 0.32s ease;
}

.content-card {
  background: var(--surface-card);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow-card);
  display: grid;
  gap: 2rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
}

.date-selector {
  min-width: min(560px, 100%);
}

.date-selector label,
.time-selectors label {
  color: var(--text-muted);
  font-weight: 600;
}

.selector-card {
  display: grid;
  gap: 0.85rem;
  padding: 1rem 1.05rem;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 252, 0.95));
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow:
    0 12px 24px rgba(15, 23, 42, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.selector-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.selector-title {
  margin: 0;
  color: var(--text-primary);
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.selector-current {
  font-size: 0.78rem;
  padding: 0.3rem 0.62rem;
  border-radius: 999px;
  color: #3730a3;
  background: rgba(99, 102, 241, 0.13);
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.selector-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.75rem;
}

.selector-field {
  display: grid;
  gap: 0.42rem;
}

.selector-field span {
  font-size: 0.8rem;
  color: var(--text-subtle);
  font-weight: 700;
  letter-spacing: 0.01em;
}

.seat-status-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  color: var(--text-muted);
}

.feature-filter-panel {
  width: 100%;
}

.filter-card {
  display: grid;
  gap: 0.85rem;
  padding: 1rem 1.05rem;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 252, 0.95));
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow:
    0 12px 24px rgba(15, 23, 42, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.filter-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.filter-card-title {
  margin: 0;
  color: var(--text-primary);
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.filter-card-hint {
  font-size: 0.78rem;
  padding: 0.3rem 0.62rem;
  border-radius: 999px;
  color: #3730a3;
  background: rgba(99, 102, 241, 0.13);
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(140px, 1fr));
  gap: 0.8rem;
}

.filter-field {
  display: grid;
  gap: 0.45rem;
}

.filter-field span {
  font-size: 0.8rem;
  color: var(--text-subtle);
  font-weight: 700;
  letter-spacing: 0.01em;
}

.filter-grid select {
  min-height: 42px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 12px;
  padding: 0 0.95rem;
  background: #fff;
  color: var(--text-primary);
  box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.04);
  appearance: none;
  -webkit-appearance: none;
  background-image:
    linear-gradient(45deg, transparent 50%, #64748b 50%),
    linear-gradient(135deg, #64748b 50%, transparent 50%);
  background-position:
    calc(100% - 18px) calc(50% - 2px),
    calc(100% - 12px) calc(50% - 2px);
  background-size: 6px 6px, 6px 6px;
  background-repeat: no-repeat;
  padding-right: 2.25rem;
}

.filter-grid select:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.65);
  box-shadow:
    0 0 0 4px rgba(99, 102, 241, 0.12),
    inset 0 1px 2px rgba(15, 23, 42, 0.04);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 6px;
}

.legend-dot.available { background: #22c55e; }
.legend-dot.partially-booked { background: #f59e0b; }
.legend-dot.booked-by-me { background: #6366f1; }
.legend-dot.full { background: #64748b; }

.booking-time-panel.compact {
  background: var(--surface-elevated);
  padding: 1rem 1.25rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.time-selectors {
  display: grid;
  gap: 1rem;
}

.time-selectors.inline {
  grid-template-columns: repeat(2, minmax(180px, 240px));
  align-items: end;
}

.time-selectors label {
  display: grid;
  gap: 0.5rem;
}

.time-selectors select,
.date-selector input,
.date-selector select {
  width: 100%;
  min-height: 46px;
  height: 46px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 12px;
  padding: 0 0.95rem;
  background: #fff;
  color: var(--text-primary);
  box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.04);
  appearance: none;
  -webkit-appearance: none;
}

.time-selectors select {
  background-image:
    linear-gradient(45deg, transparent 50%, #64748b 50%),
    linear-gradient(135deg, #64748b 50%, transparent 50%);
  background-position:
    calc(100% - 18px) calc(50% - 2px),
    calc(100% - 12px) calc(50% - 2px);
  background-size: 6px 6px, 6px 6px;
  background-repeat: no-repeat;
  padding-right: 2.25rem;
}

.date-selector select {
  background-image:
    linear-gradient(45deg, transparent 50%, #64748b 50%),
    linear-gradient(135deg, #64748b 50%, transparent 50%);
  background-position:
    calc(100% - 18px) calc(50% - 2px),
    calc(100% - 12px) calc(50% - 2px);
  background-size: 6px 6px, 6px 6px;
  background-repeat: no-repeat;
  padding-right: 2.25rem;
}

.time-selectors select:focus,
.date-selector input:focus,
.date-selector select:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.65);
  box-shadow:
    0 0 0 4px rgba(99, 102, 241, 0.12),
    inset 0 1px 2px rgba(15, 23, 42, 0.04);
}

.time-selectors select:disabled {
  color: var(--text-subtle);
  background-color: #f8fafc;
  cursor: not-allowed;
}

.seat-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 0.85fr);
  gap: 1.5rem;
  align-items: start;
}

.seat-map {
  display: grid;
  gap: 0.7rem;
}

.seat-detail-panel {
  background: var(--surface-elevated);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  box-shadow: var(--shadow-card);
  display: grid;
  gap: 1rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-muted);
}

.panel-hint {
  font-size: 0.85rem;
}

.panel-body {
  display: grid;
  gap: 1rem;
}

.seat-info-card {
  background: rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  padding: 1rem 1.1rem;
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.seat-code {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.seat-state-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.muted-label {
  color: var(--text-subtle);
  font-size: 0.9rem;
}

.subsection-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.subtle-text {
  color: var(--text-subtle);
  font-size: 0.85rem;
}

.booking-list-card {
  display: grid;
  gap: 0.75rem;
}

.booking-slot-list {
  display: grid;
  gap: 0.65rem;
  max-height: 260px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.booking-slot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 0.85rem 1rem;
  border-radius: 14px;
  background: rgba(245, 158, 11, 0.08);
  border: 1px solid rgba(245, 158, 11, 0.18);
  animation: fadeSlideUp 0.28s ease;
}

.booking-slot-item.mine {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
}

.slot-owner {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-subtle);
}

.detail-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.row {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.row-label {
  width: 26px;
  text-align: right;
  color: var(--text-subtle);
}

.seat {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid transparent;
  cursor: pointer;
  gap: 0.2rem;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    background 0.22s ease,
    border-color 0.22s ease;
}

.seat:hover {
  transform: translateY(-4px) scale(1.03);
  box-shadow: var(--shadow-border);
}

.seat:active {
  transform: scale(0.96);
}

.seat-number {
  font-size: 0.85rem;
}

.seat.available {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
  border-color: rgba(34, 197, 94, 0.3);
}

.seat.partially-booked {
  background: rgba(245, 158, 11, 0.15);
  color: #b45309;
}

.seat.booked-by-me {
  background: rgba(99, 102, 241, 0.15);
  color: #4338ca;
}

.seat.full {
  background: rgba(100, 116, 139, 0.18);
  color: #334155;
}

.seat.filtered-out {
  background: rgba(203, 213, 225, 0.22);
  color: #94a3b8;
  border-color: rgba(148, 163, 184, 0.3);
  cursor: not-allowed;
}

.seat.selected {
  transform: translateY(-3px) scale(1.03);
  box-shadow: var(--shadow-border);
  border-color: rgba(99, 102, 241, 0.5);
  animation: pulseRing 1.4s ease infinite;
}

.booking-timeline-bar {
  background: var(--surface-elevated);
  padding: 1.2rem 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  display: grid;
  gap: 0.8rem;
}

.seat-feature-list {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.feature-tag {
  display: inline-flex;
  align-items: center;
  padding: 0.28rem 0.65rem;
  border-radius: 999px;
  font-size: 0.8rem;
  color: #334155;
  background: rgba(148, 163, 184, 0.18);
  border: 1px solid rgba(148, 163, 184, 0.24);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-muted);
  font-weight: 600;
}

.timeline-track {
  display: grid;
  grid-template-columns: repeat(14, minmax(0, 1fr));
  gap: 0.25rem;
}

.timeline-segment {
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 600;
  color: #0f172a;
  background: rgba(148, 163, 184, 0.25);
}

.timeline-segment.available {
  background: rgba(34, 197, 94, 0.18);
  color: #166534;
}

.timeline-segment.booked {
  background: rgba(245, 158, 11, 0.28);
  color: #92400e;
}

.timeline-segment.booked-by-me {
  background: rgba(99, 102, 241, 0.3);
  color: #3730a3;
}

.segment-label {
  transform: scale(0.9);
}

.booking-list {
  display: grid;
  gap: 1rem;
}

.booking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--surface-elevated);
  padding: 1rem 1.5rem;
  border-radius: var(--radius-md);
  animation: fadeSlideUp 0.28s ease;
}

.status-pill {
  padding: 0.35rem 0.8rem;
  border-radius: 999px;
  background: rgba(99, 102, 241, 0.12);
  color: var(--brand-accent);
  font-weight: 600;
  font-size: 0.8rem;
}

.booking-meta {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1.5rem;
}

.profile-grid .label {
  color: var(--text-subtle);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.1rem;
}

.notification {
  position: fixed;
  top: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  border-radius: 16px;
  color: #fff;
  display: flex;
  gap: 0.8rem;
  align-items: center;
  box-shadow: var(--shadow-card);
  z-index: 1200;
  animation: notificationPop 0.22s ease;
}

.notification.success {
  background: #22c55e;
}

.notification.error {
  background: #ef4444;
}

.close-notification {
  background: rgba(255, 255, 255, 0.2);
  box-shadow: none;
}

.empty-state {
  padding: 1rem;
  text-align: center;
  color: var(--text-subtle);
}

.empty-state.small {
  padding: 0.75rem;
}

@media (max-width: 900px) {
  .booking-header {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .header-actions {
    justify-self: start;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .date-selector {
    width: 100%;
  }

  .selector-grid {
    grid-template-columns: 1fr 1fr;
  }

  .time-selectors.inline {
    grid-template-columns: 1fr;
    width: 100%;
  }

  .feature-filter-panel {
    width: 100%;
  }

  .filter-grid {
    grid-template-columns: 1fr;
  }

  .seat-layout {
    grid-template-columns: 1fr;
  }

  .booking-nav,
  .header-actions {
    width: 100%;
  }
}

@media (max-width: 600px) {
  .booking-page {
    padding: 1.5rem 1.2rem 2rem;
  }

  .booking-header {
    padding: 1.2rem 1.4rem;
  }

  .booking-nav {
    width: 100%;
    overflow-x: auto;
    justify-content: flex-start;
    padding: 0.35rem;
  }

  .nav-item {
    white-space: nowrap;
    flex: 1 0 auto;
  }

  .content-card {
    padding: 1.5rem;
  }

  .selector-grid {
    grid-template-columns: 1fr;
  }

  .selector-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-card-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .seat-map {
    overflow-x: auto;
    padding-bottom: 0.5rem;
  }

  .row {
    min-width: 520px;
  }

  .booking-timeline-bar {
    overflow-x: auto;
  }

  .timeline-track {
    min-width: 520px;
  }

  .detail-actions,
  .booking-item,
  .booking-slot-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .seat-state-line {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .ghost-button,
  .primary-action {
    width: 100%;
    text-align: center;
  }

  .booking-meta {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .booking-item > div:first-child,
  .booking-item > div:last-child {
    width: 100%;
  }
}

@media (max-width: 420px) {
  .booking-page {
    padding: 1rem 0.75rem 1.5rem;
  }

  .booking-header {
    padding: 1rem;
  }

  .content-card,
  .seat-detail-panel,
  .booking-time-panel,
  .booking-timeline-bar {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .row {
    min-width: 468px;
  }

  .timeline-track {
    min-width: 468px;
  }

  .seat {
    width: 48px;
    height: 48px;
    font-size: 0.68rem;
  }

  .seat-number {
    font-size: 0.78rem;
  }

  .notification {
    top: 1rem;
    right: 0.75rem;
    left: 0.75rem;
    padding: 0.9rem 1rem;
  }
}
</style>


