<template>
  <!--
    座位预约页 — 极简编辑 × 温暖学术
    作用：核心座位预约界面，含楼栋/楼层/日期筛选、时段选择、座位地图、详情面板、时间线
    接口设计：完整照搬原版 seatApi 全部接口、WebSocket 实时推送
  -->
  <div class="booking-page">
    <!-- ── 顶部导航 ── -->
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark">LS</span>
        <div class="brand-text">
          <span class="brand-name">考研图书馆</span>
          <span class="brand-sub">座位预约中心</span>
        </div>
      </div>

      <nav class="tab-bar">
        <button :class="['tab', { active: activeSection === 'seats' }]" @click="activeSection = 'seats'">座位预约</button>
        <button :class="['tab', { active: activeSection === 'bookings' }]" @click="activeSection = 'bookings'">我的预约</button>
        <button :class="['tab', { active: activeSection === 'profile' }]" @click="activeSection = 'profile'">账号信息</button>
      </nav>

      <div class="user-area">
        <span class="user-name">欢迎，{{ userName }}</span>
        <button class="btn-secondary" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <!-- ── 座位预约 ── -->
    <section v-if="activeSection === 'seats'" class="section">
      <!-- 筛选面板 -->
      <button class="filter-toggle" @click="showFilters = !showFilters">
        <span>{{ showFilters ? '收起筛选' : '展开筛选' }}</span>
        <span class="filter-toggle-icon">{{ showFilters ? '▲' : '▼' }}</span>
      </button>
      <transition name="filter-collapse">
        <div v-show="showFilters" class="filter-bar">
        <label class="filter-field">
          <span class="filter-label">楼栋</span>
          <select v-model="selectedBuildingCode">
            <option v-for="b in buildingOptions" :key="b.value" :value="b.value">{{ b.label }}</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-label">楼层</span>
          <select v-model.number="selectedFloorNo">
            <option v-for="f in floorOptions" :key="f" :value="f">{{ f }} 层</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-label">日期</span>
          <input v-model="selectedDate" type="date" :min="minDate" />
        </label>
        <span class="filter-sep"></span>
        <label class="filter-field">
          <span class="filter-label">靠窗</span>
          <select v-model="seatFeatureFilter.nearWindow">
            <option value="all">全部</option>
            <option value="yes">仅靠窗</option>
            <option value="no">不靠窗</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-label">插座</span>
          <select v-model="seatFeatureFilter.hasOutlet">
            <option value="all">全部</option>
            <option value="yes">有插座</option>
            <option value="no">无插座</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-label">安静区</span>
          <select v-model="seatFeatureFilter.quietZone">
            <option value="all">全部</option>
            <option value="yes">仅安静区</option>
            <option value="no">非安静区</option>
          </select>
        </label>
      </div>
      </transition>

      <!-- 图例 -->
      <div class="legend">
        <span class="legend-chip"><i class="dot available"></i>可预约</span>
        <span class="legend-chip"><i class="dot partial"></i>部分占用</span>
        <span class="legend-chip"><i class="dot mine"></i>我有预约</span>
        <span class="legend-chip"><i class="dot full"></i>已满</span>
      </div>

      <!-- 时间段选择 -->
      <div class="time-row">
        <label class="filter-field">
          <span class="filter-label">开始时间</span>
          <select v-model="selectedStartTimeOption">
            <option value="" disabled>选择开始时间</option>
            <option v-for="t in availableStartTimes" :key="t" :value="t">{{ t }}</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-label">结束时间</span>
          <select v-model="selectedEndTimeOption" :disabled="!selectedStartTimeOption">
            <option value="" disabled>选择结束时间</option>
            <option v-for="t in availableEndTimes" :key="t" :value="t">{{ t }}</option>
          </select>
        </label>
      </div>

      <!-- 座位地图 + 详情 -->
      <div class="seat-shell">
        <div class="seat-map card">
          <div class="row" v-for="row in seatRows" :key="row">
            <span class="row-label">{{ row }}</span>
            <button
              v-for="col in seatColumns"
              :key="`${row}-${col}`"
              :class="['seat', getSeatUiStatus(row, col), { selected: isSeatSelected(row, col) }]"
              :title="getSeatTooltip(row, col)"
              @click="handleSeatClick(row, col)"
            >
              <span class="seat-num">{{ col }}</span>
              <span class="seat-txt">{{ getSeatStatusText(row, col) }}</span>
            </button>
          </div>
        </div>

        <aside class="detail-panel card">
          <div class="panel-head">
            <h3>座位详情</h3>
            <span class="panel-hint">点击座位查看</span>
          </div>

          <template v-if="selectedSeatId">
            <div class="detail-info">
              <p class="seat-code">{{ selectedBuildingCode }}-F{{ selectedFloorNo }}-{{ selectedSeatRow }}-{{ selectedSeatCol }}</p>
              <div class="seat-state">
                <span class="muted">当前状态</span>
                <span class="tag" :class="selectedSeatStatusClass">{{ selectedSeatSummaryText }}</span>
              </div>
            </div>

            <div class="feature-tags" v-if="selectedSeatMeta">
              <span class="feature-tag">{{ selectedSeatMeta.nearWindow ? '靠窗' : '非靠窗' }}</span>
              <span class="feature-tag">{{ selectedSeatMeta.hasOutlet ? '有插座' : '无插座' }}</span>
              <span class="feature-tag">{{ selectedSeatMeta.quietZone ? '安静区' : '普通区' }}</span>
            </div>

            <div class="booking-slots">
              <div class="slots-head">
                <h4>已预约时段</h4>
                <span class="muted">{{ seatDayBookings.length }} 条</span>
              </div>
              <div v-if="seatDayBookings.length === 0" class="empty">暂无预约时段</div>
              <div v-else class="slot-list">
                <div
                  v-for="b in seatDayBookings"
                  :key="b.id"
                  :class="['slot', { mine: b.userId === currentUserId }]"
                >
                  <strong>{{ b.startTime }} - {{ b.endTime }}</strong>
                  <span>{{ b.userId === currentUserId ? '我的预约' : '已预约' }}</span>
                </div>
              </div>
            </div>

            <div class="detail-btns">
              <button class="btn-primary" :disabled="!canSubmitBooking" @click="handleBooking">立即预约</button>
              <button class="btn-secondary" :disabled="!selectedBookingOwnedByUser" @click="handleCancelSelectedBooking">取消我的预约</button>
            </div>
          </template>

          <div v-else class="empty">请选择座位查看详情</div>
        </aside>
      </div>

      <!-- 时间线 -->
      <div class="timeline card">
        <div class="timeline-head">
          <h4>当前座位当日时间分布</h4>
          <span class="muted">08:00 - 22:00</span>
        </div>
        <div class="timeline-track">
          <div
            v-for="h in timelineHours"
            :key="h"
            :class="['timeline-cell', getTimelineClass(h)]"
          >
            {{ String(h).padStart(2, '0') }}
          </div>
        </div>
      </div>
    </section>

    <!-- ── 我的预约 ── -->
    <section v-if="activeSection === 'bookings'" class="section">
      <div class="card">
        <div class="card-head">
          <h2>我的预约</h2>
          <p>查看当前与历史预约记录。</p>
        </div>
        <div v-if="userBookings.length === 0" class="empty">暂无预约记录</div>
        <div v-else class="booking-list">
          <div v-for="b in userBookings" :key="b.id" class="booking-row">
            <div>
              <h4>{{ b.seatId }}</h4>
              <p>{{ formatDate(b.date) }} · {{ b.startTime }} - {{ b.endTime }}</p>
            </div>
            <div class="booking-actions">
    <span class="tag tag-default">{{ getStatusLabel(b.status) }}</span>
    <template v-if="b.status === 'BOOKED'">
        <span v-if="getBookingTimeStatus(b) === 'pending'" class="tag tag-default">待开始</span>
        <button v-if="getBookingTimeStatus(b) === 'can_checkin'" class="btn-checkin" @click="handleCheckin(b)">打卡签到</button>
        <span v-if="b.checkedIn" class="tag tag-success">已打卡</span>
    </template>
    <span v-if="b.status === 'EXPIRED' && !b.checkedIn" class="tag tag-danger">无效预约</span>
    <span v-if="b.status === 'EXPIRED' && b.checkedIn" class="tag tag-success">已完成</span>
    <button v-if="b.status === 'BOOKED' && b.date >= today && !b.checkedIn" class="btn-danger" @click="handleCancelBookingById(b.id)">取消</button>
</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── 账号信息 ── -->
<section v-if="activeSection === 'profile'" class="section">
  <div class="card">
    <div class="card-head">
      <h2>账号信息</h2>
      <p>查看当前登录账号信息。</p>
    </div>
    <div class="profile-grid">
      <div class="profile-item"><span class="profile-label">姓名</span><p>{{ authStore.user?.name || '未命名' }}</p></div>
      <div class="profile-item"><span class="profile-label">邮箱</span><p>{{ authStore.user?.email || '-' }}</p></div>
      <div class="profile-item"><span class="profile-label">角色</span><p>{{ authStore.user?.role || 'USER' }}</p></div>
    </div>
  </div>

  <!-- 预约统计 -->
  <div class="card">
    <div class="card-head"><h2>预约统计</h2></div>
    <div class="stats-row">
      <div class="stat-item"><span class="stat-num">{{ integrityStats.totalBookings }}</span><span class="stat-lbl">总预约</span></div>
      <div class="stat-item"><span class="stat-num">{{ integrityStats.validBookings }}</span><span class="stat-lbl">有效预约</span></div>
      <div class="stat-item"><span class="stat-num">{{ integrityStats.invalidBookings }}</span><span class="stat-lbl">无效预约</span></div>
    </div>
    <div class="integrity-bar-wrap">
      <div class="integrity-bar-head"><span>诚信指数</span><strong>{{ Math.round(integrityStats.integrityScore * 100) }}%</strong></div>
      <div class="integrity-bar"><div class="integrity-fill" :style="{ width: Math.round(integrityStats.integrityScore * 100) + '%' }"></div></div>
    </div>
  </div>

  <!-- 修改密码 -->
  <div class="card">
    <div class="card-head"><h2>修改密码</h2></div>
    <div class="form-stack">
      <label>旧密码 <input v-model="oldPassword" type="password" placeholder="请输入旧密码" /></label>
      <label>新密码 <input v-model="newPassword" type="password" placeholder="请输入新密码" /></label>
      <label>确认密码 <input v-model="confirmPassword" type="password" placeholder="请再次输入新密码" /></label>
      <button class="btn-primary" @click="handleChangePassword">修改密码</button>
    </div>
  </div>
</section>

    <!-- ── 通知 ── -->
    <transition name="notif">
      <div v-if="showNotification" :class="['notification', notificationType]">
        <span>{{ notificationMessage }}</span>
        <button class="notif-close" @click="showNotification = false">&times;</button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import * as seatApi from '../api/seatApi'
import { wsManager } from '../utils/websocket'
import { authApi } from '../api/authApi'
import * as userApi from '../api/userApi'

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
const seatFeatureFilter = ref({ nearWindow: 'all', hasOutlet: 'all', quietZone: 'all' })
const seatDayBookings = ref([])
const userBookings = ref([])
const availableStartTimes = ref([])
const availableEndTimes = ref([])
const showFilters = ref(true)
const showNotification = ref(false)
const notificationMessage = ref('')
const notificationType = ref('success')
const autoRefreshTimer = ref(null)

// Password change
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

// Integrity stats
const integrityStats = ref({ totalBookings: 0, validBookings: 0, invalidBookings: 0, integrityScore: 1.0 })

const buildingOptions = [
  { label: '1号楼', value: 'B1' },
  { label: '2号楼', value: 'B2' },
  { label: '3号楼', value: 'B3' }
]
const floorOptions = [1, 2, 3]
const seatRows = ref(['A', 'B', 'C', 'D', 'E'])
const seatColumns = ref([1, 2, 3, 4, 5])
const timelineHours = Array.from({ length: 14 }, (_, i) => i + 8)

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

const selectedSeatStatusClass = computed(() => {
  if (!selectedSeatId.value) return ''
  if (seatDayBookings.value.length === 0) return 'tag-success'
  const myCount = seatDayBookings.value.filter(item => item.userId === currentUserId.value).length
  if (myCount > 0) return 'tag-default'
  return 'tag-warning'
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
  if (selectedDate.value === today.value && selectedStartTimeOption.value < earliestStart) return false
  return !hasOverlap(selectedStartTimeOption.value, selectedEndTimeOption.value, seatDayBookings.value)
})

let latestSeatRequestToken = 0
let latestSeatsRequestToken = 0
let seatStatusUpdateHandler = null

onMounted(async () => {
  await initializePage()
  await loadIntegrityStats()
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

watch(seatFeatureFilter, () => {
  if (selectedSeatId.value && selectedSeatMeta.value && !matchesSeatFeatureFilter(selectedSeatMeta.value)) {
    resetSeatSelection()
  }
}, { deep: true })

async function initializePage() {
  await Promise.allSettled([loadSeats(), loadUserBookings()])
}

function startAutoRefresh() {
  stopAutoRefresh()
  autoRefreshTimer.value = setInterval(async () => {
    if (activeSection.value !== 'seats') return
    await Promise.allSettled([
      loadSeats(), loadUserBookings(),
      selectedSeatId.value ? loadSeatDayBookings() : Promise.resolve()
    ])
    if (selectedSeatId.value) { updateAvailableStartTimes(); updateAvailableEndTimes() }
  }, 30 * 1000)
}

function stopAutoRefresh() {
  if (autoRefreshTimer.value) { clearInterval(autoRefreshTimer.value); autoRefreshTimer.value = null }
}

function connectWebSocket() {
  wsManager.connect('ws://localhost:8081/ws/seat-status')
  seatStatusUpdateHandler = (data) => {
    if (data?.seat) updateSeatStatus(data.seat)
  }
  wsManager.on('seatStatusUpdate', seatStatusUpdateHandler)
}

function normalizeBooking(raw) {
    return {
        id: raw.id, seatId: String(raw.seatId || ''),
        date: raw.bookingDate || raw.date || '',
        startTime: normalizeTime(raw.startTime), endTime: normalizeTime(raw.endTime),
        status: raw.status || 'BOOKED', userId: String(raw.userId || ''),
        createdAt: raw.createdAt || '', checkedIn: Boolean(raw.checkedIn)
    }
}

function normalizeTime(value) {
  if (!value) return ''
  return String(value).slice(0, 5)
}

function updateSeatStatus(updatedSeat) {
  if (!updatedSeat?.seatId) return
  if (updatedSeat.buildingCode !== selectedBuildingCode.value || Number(updatedSeat.floorNo) !== Number(selectedFloorNo.value)) return
  const idx = seats.value.findIndex(item => item.seatId === updatedSeat.seatId)
  if (idx === -1) { seats.value = [...seats.value, updatedSeat]; return }
  const next = [...seats.value]
  next[idx] = { ...next[idx], ...updatedSeat }
  seats.value = next
}

function parseSeatLocationFromSeatId(seatId) {
  if (!seatId) return null
  const match = String(seatId).match(/^(B\d+)-F(\d+)-/i)
  if (!match) return null
  return { buildingCode: match[1].toUpperCase(), floorNo: Number(match[2]) }
}

function isSeatInSelectedLocation(seat) {
  if (!seat) return false
  const seatBuildingCode = String(seat.buildingCode || '').toUpperCase()
  const seatFloorNo = Number(seat.floorNo)
  if (seatBuildingCode && Number.isFinite(seatFloorNo) && seatFloorNo > 0) {
    return seatBuildingCode === selectedBuildingCode.value && seatFloorNo === Number(selectedFloorNo.value)
  }
  const parsed = parseSeatLocationFromSeatId(seat.seatId)
  if (!parsed) return false
  return parsed.buildingCode === selectedBuildingCode.value && parsed.floorNo === Number(selectedFloorNo.value)
}

async function loadSeats() {
  const requestToken = ++latestSeatsRequestToken
  try {
    const allSeats = await seatApi.getAllSeats(selectedDate.value, currentUserId.value, selectedBuildingCode.value, selectedFloorNo.value)
    if (requestToken !== latestSeatsRequestToken) return
    seats.value = (Array.isArray(allSeats) ? allSeats : []).filter(isSeatInSelectedLocation)
  } catch { if (requestToken !== latestSeatsRequestToken) return; seats.value = [] }
}

async function loadUserBookings() {
  try {
    if (!currentUserId.value) { userBookings.value = []; return }
    const bookings = await seatApi.getUserBookedSeats(currentUserId.value)
    userBookings.value = (Array.isArray(bookings) ? bookings : []).map(normalizeBooking)
  } catch { userBookings.value = [] }
}

async function loadIntegrityStats() {
    if (!currentUserId.value) return
    try {
        const stats = await userApi.getUserIntegrity(currentUserId.value)
        integrityStats.value = stats
    } catch { /* ignore */ }
}

async function loadSeatDayBookings() {
  if (!selectedSeatId.value) return
  const requestToken = ++latestSeatRequestToken
  try {
    const bookings = await seatApi.getSeatBookingsByDate(selectedSeatId.value, selectedDate.value)
    if (requestToken !== latestSeatRequestToken) return
    seatDayBookings.value = (Array.isArray(bookings) ? bookings : [])
      .map(normalizeBooking).filter(item => item.status === 'BOOKED')
      .sort((a, b) => a.startTime.localeCompare(b.startTime))
    updateAvailableStartTimes()
  } catch { if (requestToken !== latestSeatRequestToken) return; seatDayBookings.value = []; updateAvailableStartTimes(); availableEndTimes.value = [] }
}

function generateTimeSlots() {
  const slots = []
  for (let m = 8 * 60; m <= 22 * 60; m += 30) {
    slots.push(`${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`)
  }
  return slots
}

function roundUpToNextHalfHour(date = new Date()) {
  const r = new Date(date); r.setSeconds(0); r.setMilliseconds(0)
  const mins = r.getMinutes()
  if (mins === 0 || mins === 30) return `${String(r.getHours()).padStart(2, '0')}:${String(mins).padStart(2, '0')}`
  if (mins < 30) r.setMinutes(30); else { r.setHours(r.getHours() + 1); r.setMinutes(0) }
  return `${String(r.getHours()).padStart(2, '0')}:${String(r.getMinutes()).padStart(2, '0')}`
}

function getEarliestSelectableStartTime() {
  if (selectedDate.value !== today.value) return '08:00'
  const nowRounded = roundUpToNextHalfHour(new Date())
  if (nowRounded < '08:00') return '08:00'
  if (nowRounded > '21:30') return '22:00'
  return nowRounded
}

function toMinutes(time) { const [h, m] = String(time).split(':').map(Number); return h * 60 + m }
function intervalsOverlap(sA, eA, sB, eB) { return toMinutes(sA) < toMinutes(eB) && toMinutes(sB) < toMinutes(eA) }
function hasOverlap(start, end, bookings) { return bookings.some(item => intervalsOverlap(start, end, item.startTime, item.endTime)) }

function isFullyBlocked(allSlots, bookings) {
  const starts = allSlots.slice(0, -1)
  return starts.every(s => { const i = allSlots.indexOf(s); return hasOverlap(s, allSlots[i + 1], bookings) })
}

function updateAvailableStartTimes() {
  const allSlots = generateTimeSlots(); const starts = allSlots.slice(0, -1); const earliest = getEarliestSelectableStartTime()
  availableStartTimes.value = starts.filter(s => s >= earliest && !hasOverlap(s, allSlots[allSlots.indexOf(s) + 1], seatDayBookings.value))
  if (!availableStartTimes.value.includes(selectedStartTimeOption.value)) selectedStartTimeOption.value = ''
  updateAvailableEndTimes()
}

function updateAvailableEndTimes() {
  if (!selectedStartTimeOption.value) { availableEndTimes.value = []; return }
  const allSlots = generateTimeSlots(); const si = allSlots.indexOf(selectedStartTimeOption.value)
  if (si === -1) { availableEndTimes.value = []; return }
  const candidates = []
  for (let i = si + 1; i < allSlots.length; i++) { if (hasOverlap(selectedStartTimeOption.value, allSlots[i], seatDayBookings.value)) break; candidates.push(allSlots[i]) }
  availableEndTimes.value = candidates
  if (!availableEndTimes.value.includes(selectedEndTimeOption.value)) selectedEndTimeOption.value = ''
}

function getSeatByPosition(row, col) { return seats.value.find(item => item.row === row && Number(item.col) === Number(col)) || null }
function matchBooleanFilter(fv, av) { if (fv === 'all') return true; if (fv === 'yes') return Boolean(av); return !Boolean(av) }
function matchesSeatFeatureFilter(seat) {
  if (!seat) return false
  return matchBooleanFilter(seatFeatureFilter.value.nearWindow, seat.nearWindow) &&
    matchBooleanFilter(seatFeatureFilter.value.hasOutlet, seat.hasOutlet) &&
    matchBooleanFilter(seatFeatureFilter.value.quietZone, seat.quietZone)
}

function getSeatBookingsForDay(seatId) {
  return userBookings.value.filter(item => item.seatId === seatId && item.date === selectedDate.value && String(item.status || '').toUpperCase() === 'BOOKED')
}

function getSeatUiStatus(row, col) {
  const seat = getSeatByPosition(row, col)
  if (!seat) return 'avail'; if (!matchesSeatFeatureFilter(seat)) return 'filtered'
  const myBookings = getSeatBookingsForDay(seat.seatId); if (myBookings.length > 0) return 'myseat'
  const status = String(seat.status || 'AVAILABLE')
  if (status === 'FUTURE_BOOKED' || status === 'BOOKED') return 'partial'
  if (status === 'USED') return 'fullseat'
  return 'avail'
}

function getSeatStatusText(row, col) {
  const seat = getSeatByPosition(row, col)
  if (!seat) return '可约'; if (!matchesSeatFeatureFilter(seat)) return '已筛除'
  const myBookings = getSeatBookingsForDay(seat.seatId); if (myBookings.length > 0) return '我已约'
  const map = { AVAILABLE: '可约', BOOKED: '部分占用', FUTURE_BOOKED: '未来占用', USED: '已满' }
  return map[seat.status] || '可约'
}

function getSeatTooltip(row, col) {
  const seat = getSeatByPosition(row, col); const dc = `${selectedBuildingCode.value}-F${selectedFloorNo.value}-${row}-${col}`
  if (!seat) return `座位 ${dc}\n状态：可预约`
  if (!matchesSeatFeatureFilter(seat)) return `座位 ${dc}\n状态：不满足筛选条件`
  const myBookings = getSeatBookingsForDay(seat.seatId)
  if (myBookings.length > 0) return `座位 ${dc}\n状态：我已预约\n时段：${myBookings.map(item => `${item.startTime}-${item.endTime}`).join('，')}`
  return `座位 ${dc}\n状态：${getStatusLabel(seat.status)}`
}

function isSeatSelected(row, col) { const seat = getSeatByPosition(row, col); return !!seat && selectedSeatId.value === seat.seatId }

function handleSeatClick(row, col) {
  const seat = getSeatByPosition(row, col)
  if (!seat || !matchesSeatFeatureFilter(seat)) return
  selectedSeatId.value = seat.seatId; selectedSeatRow.value = row; selectedSeatCol.value = String(col)
}

function resetSeatSelection() {
  latestSeatRequestToken += 1
  selectedSeatId.value = ''; selectedSeatRow.value = ''; selectedSeatCol.value = ''
  seatDayBookings.value = []; availableStartTimes.value = []; availableEndTimes.value = []
  selectedStartTimeOption.value = ''; selectedEndTimeOption.value = ''
}

async function handleBooking() {
  if (!selectedSeatId.value) { showNotificationMessage('请先选择座位', 'error'); return }
  if (!currentUserId.value) { showNotificationMessage('请先登录后再预约', 'error'); return }
  if (!canSubmitBooking.value) { showNotificationMessage('所选时间段不可预约，请重新选择', 'error'); return }
  let result
  try { result = await seatApi.bookSeat(selectedSeatId.value, currentUserId.value, selectedDate.value, selectedStartTimeOption.value, selectedEndTimeOption.value) }
  catch { showNotificationMessage('预约失败，请重试', 'error'); return }
  if (result?.success === false) { showNotificationMessage(result.message || '预约失败，请重试', 'error'); return }
  showNotificationMessage('预约成功', 'success')
  await Promise.allSettled([loadSeats(), loadUserBookings(), loadSeatDayBookings()])
  selectedStartTimeOption.value = ''; selectedEndTimeOption.value = ''
}

async function handleCancelSelectedBooking() {
  if (!selectedSeatId.value) return
  const target = seatDayBookings.value.find(item => item.userId === currentUserId.value)
  if (!target) { showNotificationMessage('未找到当前座位的个人预约记录', 'error'); return }
  await cancelBookingRecord(target)
}

async function handleCancelBookingById(bookingId) {
  const target = userBookings.value.find(item => item.id === bookingId)
  if (!target) { showNotificationMessage('未找到对应预约记录', 'error'); return }
  await cancelBookingRecord(target)
}

async function cancelBookingRecord(booking) {
  try { await seatApi.cancelBookingById(booking.seatId, booking.id) } catch { showNotificationMessage('取消预约失败，请重试', 'error'); return }
  showNotificationMessage('取消预约成功', 'success')
  await Promise.allSettled([loadSeats(), loadUserBookings(), selectedSeatId.value === booking.seatId ? loadSeatDayBookings() : Promise.resolve()])
}

async function handleCheckin(booking) {
    if (!currentUserId.value) { showNotificationMessage('请先登录', 'error'); return }
    try {
        const result = await seatApi.checkin(booking.id, currentUserId.value)
        if (result?.success) {
            showNotificationMessage('打卡成功', 'success')
            await Promise.allSettled([loadUserBookings(), loadIntegrityStats()])
        } else {
            showNotificationMessage(result?.message || '打卡失败', 'error')
        }
    } catch { showNotificationMessage('打卡失败，请重试', 'error') }
}

async function handleLogout() {
  userBookings.value = []; seats.value = []; resetSeatSelection()
  await authStore.logout(); router.push('/login')
}

async function handleChangePassword() {
    if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
        showNotificationMessage('请填写所有密码字段', 'error'); return
    }
    if (newPassword.value !== confirmPassword.value) {
        showNotificationMessage('两次输入的新密码不一致', 'error'); return
    }
    if (newPassword.value.length < 6) {
        showNotificationMessage('新密码长度至少6位', 'error'); return
    }
    try {
        const result = await authApi.changePassword(oldPassword.value, newPassword.value)
        if (result?.success) {
            showNotificationMessage('密码修改成功', 'success')
            oldPassword.value = ''; newPassword.value = ''; confirmPassword.value = ''
        } else {
            showNotificationMessage(result?.message || '密码修改失败', 'error')
        }
    } catch {
        showNotificationMessage('密码修改失败，请重试', 'error')
    }
}

function showNotificationMessage(message, type = 'success') {
  notificationMessage.value = message; notificationType.value = type; showNotification.value = true
  setTimeout(() => { showNotification.value = false }, 3000)
}

function getStatusLabel(status) {
  const m = { AVAILABLE: '可用', BOOKED: '已预约', FUTURE_BOOKED: '未来预约', USED: '已使用', CANCELLED: '已取消', EXPIRED: '已过期' }
  return m[status] || status || '-'
}

function formatDate(dateString) {
  if (!dateString) return '-'
  const d = new Date(dateString); if (Number.isNaN(d.getTime())) return '-'
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function getTimelineClass(hour) {
  if (!selectedSeatId.value) return 'tl-avail'
  const s = `${String(hour).padStart(2, '0')}:00`; const e = `${String(hour + 1).padStart(2, '0')}:00`
  const overlapping = seatDayBookings.value.filter(item => intervalsOverlap(s, e, item.startTime, item.endTime))
  if (overlapping.length === 0) return 'tl-avail'
  if (overlapping.some(item => item.userId === currentUserId.value)) return 'tl-mine'
  return 'tl-booked'
}

function getBookingTimeStatus(booking) {
    if (booking.checkedIn) return 'checked_in'
    if (booking.status === 'EXPIRED') return 'expired_no_checkin'
    if (booking.status === 'CANCELLED') return 'cancelled'
    if (booking.status === 'BOOKED') {
        const now = new Date()
        const todayStr = now.toISOString().split('T')[0]
        if (booking.date !== todayStr) return 'future'
        const currentTime = now.getHours() * 60 + now.getMinutes()
        const startMin = toMinutes(booking.startTime)
        const endMin = toMinutes(booking.endTime)
        if (currentTime < startMin) return 'pending'
        if (currentTime >= startMin && currentTime <= endMin) return 'can_checkin'
        return 'pending'
    }
    return 'unknown'
}
</script>

<style scoped>
@keyframes fadeUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes notifIn { from { opacity: 0; transform: translateY(-8px) scale(0.97); } to { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes pulse { 0%,100% { box-shadow: 0 0 0 0 rgba(180,83,9,0.18); } 50% { box-shadow: 0 0 0 6px rgba(180,83,9,0); } }

.booking-page {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--paper-white) 0%, var(--paper-warm) 100%);
  padding: 1.5rem 2rem 3rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* ── Topbar ── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  background: var(--card-pure);
  border-radius: var(--radius-xl);
  padding: 1rem 1.5rem;
  border: 1px solid var(--border-warm);
  animation: fadeUp 0.3s ease;
}

.brand { display: flex; align-items: center; gap: 0.75rem; }
.brand-mark { width: 38px; height: 38px; border-radius: 10px; background: var(--amber-gradient); display: grid; place-items: center; color: #fff; font-family: var(--font-display); font-weight: 800; font-size: 1rem; }
.brand-text { display: flex; flex-direction: column; }
.brand-name { font-weight: 700; color: var(--text-body); font-size: 0.95rem; }
.brand-sub { font-size: 0.72rem; color: var(--text-subtle); }

.tab-bar { display: flex; gap: 0.35rem; background: var(--paper-warm); padding: 0.3rem; border-radius: var(--radius-pill); }
.tab { flex: 1; padding: 0.45rem 1.2rem; border-radius: var(--radius-pill); font-weight: 500; font-size: 0.85rem; color: var(--text-soft); background: transparent; transition: all var(--ease-smooth); text-align: center; }
.tab:hover { color: var(--text-body); }
.tab.active { background: var(--card-pure); color: var(--text-body); font-weight: 700; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }

.user-area { display: flex; align-items: center; gap: 0.8rem; }
.user-name { font-size: 0.85rem; color: var(--text-soft); }

/* ── Filter bar ── */
.filter-bar {
  display: flex; align-items: end; gap: 0.8rem; flex-wrap: wrap;
  background: var(--card-pure); border-radius: var(--radius-xl); padding: 1rem 1.5rem; border: 1px solid var(--border-warm);
}
.filter-field { display: grid; gap: 0.3rem; }
.filter-label { font-size: 0.7rem; font-weight: 700; color: var(--text-subtle); text-transform: uppercase; letter-spacing: 0.06em; }
.filter-field select, .filter-field input { min-height: 40px; padding: 0.45rem 0.75rem; font-size: 0.88rem; }
.filter-sep { width: 1px; height: 40px; background: var(--border-warm); align-self: end; }

/* ── Filter toggle (mobile) ── */
.filter-toggle {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  width: 100%;
  padding: 0.55rem 1rem;
  border-radius: var(--radius-md);
  background: var(--card-pure);
  color: var(--text-soft);
  font-size: 0.85rem;
  font-weight: 600;
  border: 1px solid var(--border-warm);
  cursor: pointer;
  transition: all var(--ease-smooth);
}
.filter-toggle:hover { color: var(--amber); border-color: var(--amber); }
.filter-toggle-icon { font-size: 0.7rem; }

.filter-collapse-enter-active { transition: all 0.25s ease; overflow: hidden; }
.filter-collapse-leave-active { transition: all 0.2s ease; overflow: hidden; }
.filter-collapse-enter-from,
.filter-collapse-leave-to { opacity: 0; max-height: 0; margin-bottom: 0; }
.filter-collapse-enter-to,
.filter-collapse-leave-from { opacity: 1; max-height: 500px; }

/* ── Legend ── */
.legend { display: flex; gap: 1.2rem; flex-wrap: wrap; }
.legend-chip { display: flex; align-items: center; gap: 0.4rem; font-size: 0.82rem; color: var(--text-muted); }
.dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.dot.available { background: #d4a574; }
.dot.partial { background: #f0c060; }
.dot.mine { background: var(--amber); }
.dot.full { background: var(--text-subtle); }

/* ── Time row ── */
.time-row { display: flex; gap: 1rem; align-items: end; }

/* ── Seat shell ── */
.seat-shell { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 1.25rem; align-items: start; }

/* ── Card ── */
.card { background: var(--card-pure); border-radius: var(--radius-xl); padding: 1.5rem; border: 1px solid var(--border-warm); }

/* ── Seat map ── */
.seat-map { display: grid; gap: 0.5rem; }
.row { display: flex; align-items: center; gap: 0.4rem; }
.row-label { text-align: right; font-size: 0.8rem; color: var(--text-subtle); font-weight: 600; min-width: 22px; }

.seat {
  width: 48px; height: 48px; border-radius: 12px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 0.1rem; font-weight: 600; font-size: 0.7rem;
  cursor: pointer; border: 2px solid transparent; transition: all var(--ease-smooth);
}
.seat:hover { transform: translateY(-3px) scale(1.04); }
.seat:active { transform: scale(0.95); }
.seat-num { font-size: 0.85rem; }

.seat.avail { background: rgba(212, 165, 116, 0.15); color: #6b5b4b; border-color: rgba(212, 165, 116, 0.3); }
.seat.partial { background: var(--honey); color: var(--honey-text); border-color: var(--honey-border); }
.seat.myseat { background: var(--amber-soft); color: var(--amber); border-color: var(--amber); }
.seat.fullseat { background: rgba(139, 115, 85, 0.15); color: var(--text-subtle); }
.seat.filtered { background: rgba(203, 213, 225, 0.2); color: var(--text-subtle); border-color: var(--border-warm); cursor: not-allowed; }
.seat.selected { transform: translateY(-3px) scale(1.04); border-color: var(--amber); animation: pulse 1.4s ease infinite; }

/* ── Detail panel ── */
.detail-panel { display: grid; gap: 1rem; }
.panel-head { display: flex; justify-content: space-between; align-items: center; }
.panel-head h3 { font-size: 0.95rem; color: var(--text-body); }
.panel-hint { font-size: 0.8rem; color: var(--text-subtle); }
.muted { color: var(--text-subtle); font-size: 0.82rem; }

.detail-info { background: var(--paper-white); border-radius: var(--radius-md); padding: 0.9rem 1rem; border: 1px solid var(--border-warm); }
.seat-code { font-size: 1.1rem; font-weight: 700; color: var(--text-body); margin-bottom: 0.4rem; }
.seat-state { display: flex; justify-content: space-between; align-items: center; }

.feature-tags { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.feature-tag { padding: 0.2rem 0.55rem; border-radius: var(--radius-pill); font-size: 0.76rem; color: var(--text-muted); background: var(--paper-warm); border: 1px solid var(--border-warm); }

.booking-slots { display: grid; gap: 0.5rem; }
.slots-head { display: flex; justify-content: space-between; align-items: center; }
.slots-head h4 { font-size: 0.85rem; color: var(--text-muted); }
.slot-list { display: grid; gap: 0.4rem; max-height: 220px; overflow-y: auto; }
.slot { display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; border-radius: var(--radius-sm); background: var(--honey); border: 1px solid var(--honey-border); font-size: 0.83rem; animation: fadeUp 0.25s ease; }
.slot.mine { background: var(--amber-soft); border-color: var(--amber); }
.slot strong { color: var(--honey-text); }
.slot.mine strong { color: var(--amber); }

.detail-btns { display: flex; gap: 0.75rem; flex-wrap: wrap; }
.detail-btns .btn-primary { flex: 1; }

.empty { padding: 1rem; text-align: center; color: var(--text-subtle); font-size: 0.88rem; }

/* ── Timeline ── */
.timeline-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.6rem; }
.timeline-head h4 { font-size: 0.85rem; color: var(--text-muted); }
.timeline-track { display: grid; grid-template-columns: repeat(14, minmax(0, 1fr)); gap: 0.2rem; }
.timeline-cell { height: 26px; border-radius: 6px; display: grid; place-items: center; font-size: 0.65rem; font-weight: 600; }
.tl-avail { background: rgba(212, 165, 116, 0.15); color: #6b5b4b; }
.tl-booked { background: var(--honey); color: var(--honey-text); }
.tl-mine { background: var(--amber-soft); color: var(--amber); }

/* ── Bookings list ── */
.card-head { margin-bottom: 1.5rem; }
.card-head h2 { font-size: 1.2rem; margin-bottom: 0.3rem; }
.booking-list { display: grid; gap: 0.8rem; }
.booking-row { display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.5rem; border-radius: var(--radius-lg); background: var(--paper-warm); border: 1px solid var(--border-warm); animation: fadeUp 0.28s ease; }
.booking-row h4 { font-size: 0.95rem; margin-bottom: 0.2rem; }
.booking-actions { display: flex; align-items: center; gap: 0.6rem; }

/* ── Profile ── */
.profile-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 1.5rem; }
.profile-label { font-size: 0.75rem; color: var(--text-subtle); text-transform: uppercase; letter-spacing: 0.1em; }
.profile-item p { margin-top: 0.3rem; font-weight: 600; color: var(--text-body); }

/* ── Notification ── */
.notification { position: fixed; top: 1.5rem; right: 1.5rem; z-index: 1100; padding: 0.9rem 1.2rem; border-radius: var(--radius-lg); display: flex; align-items: center; gap: 0.8rem; color: #fff; font-weight: 600; font-size: 0.9rem; box-shadow: var(--shadow-elevated); animation: notifIn 0.2s ease; }
.notification.success { background: var(--amber); }
.notification.error { background: #ef4444; }
.notif-close { background: rgba(255,255,255,0.2); box-shadow: none; color: #fff; padding: 0.3rem 0.6rem; border-radius: 999px; }
.notif-enter-active, .notif-leave-active { transition: all 0.25s ease; }
.notif-enter-from, .notif-leave-to { opacity: 0; transform: translateY(-8px); }

/* ── Section ── */
.section { display: grid; gap: 1.25rem; animation: fadeUp 0.3s ease; }

/* Stats row */
.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-bottom: 1.25rem; }
.stat-item { text-align: center; padding: 0.8rem; border-radius: var(--radius-md); background: var(--paper-white); border: 1px solid var(--border-warm); }
.stat-num { display: block; font-size: 1.5rem; font-weight: 800; color: var(--text-body); }
.stat-lbl { display: block; font-size: 0.75rem; color: var(--text-subtle); margin-top: 0.2rem; }

/* Integrity bar */
.integrity-bar-wrap { margin-top: 0.5rem; }
.integrity-bar-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; font-size: 0.85rem; }
.integrity-bar-head strong { color: var(--amber); }
.integrity-bar { height: 8px; border-radius: 999px; background: var(--paper-warm); overflow: hidden; }
.integrity-fill { height: 100%; border-radius: 999px; background: var(--amber-gradient); transition: width 0.5s ease; }

/* Form stack */
.form-stack { display: grid; gap: 0.8rem; }
.form-stack label { display: grid; gap: 0.3rem; font-size: 0.82rem; color: var(--text-soft); }

/* Checkin button & status tags */
.btn-checkin { padding: 0.35rem 0.8rem; border-radius: var(--radius-pill); font-size: 0.78rem; font-weight: 600; background: var(--green-soft); color: var(--green-text); border: 1px solid rgba(21,128,61,0.2); cursor: pointer; transition: all var(--ease-smooth); }
.btn-checkin:hover { background: #22c55e; color: #fff; }
.tag-success { background: var(--green-soft); color: var(--green-text); border: 1px solid rgba(21,128,61,0.15); }
.tag-danger { background: var(--red-soft); color: var(--red-text); border: 1px solid rgba(153,27,27,0.15); }

/* ── Responsive ── */
@media (max-width: 900px) {
  .topbar { flex-direction: column; align-items: stretch; }
  .tab-bar { overflow-x: auto; -webkit-overflow-scrolling: touch; scrollbar-width: none; }
  .tab-bar::-webkit-scrollbar { display: none; }
  .tab { flex: none; white-space: nowrap; }
  .user-area { justify-content: center; }
  .seat-shell { grid-template-columns: 1fr; }
  .filter-bar { gap: 0.6rem; }
  .filter-field select, .filter-field input { min-height: 44px; }
  .seat { max-width: 52px; }
}

@media (max-width: 600px) {
  .booking-page { padding: 1rem 0.75rem 2rem; }
  .topbar { padding: 1rem; }
  .tab { padding: 0.45rem 0.8rem; font-size: 0.8rem; }
  .card { padding: 1.25rem; }
  .filter-toggle { display: flex; }
  .filter-bar { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; padding: 1rem; }
  .filter-sep { display: none; }
  .time-row { flex-direction: column; align-items: stretch; }
  .time-row .filter-field select { width: 100%; }
  .row { display: grid; grid-template-columns: 22px repeat(5, 1fr); gap: 0.3rem; }
  .seat { width: 100%; height: auto; aspect-ratio: 1; max-width: 52px; justify-self: center; }
  .seat-num { font-size: 0.8rem; }
  .booking-row, .detail-btns { flex-direction: column; align-items: stretch; }
  .profile-grid { grid-template-columns: 1fr; }
  .stats-row { grid-template-columns: repeat(3, 1fr); gap: 0.6rem; }
  .stat-item { padding: 0.5rem 0.3rem; }
  .stat-num { font-size: 1.2rem; }
  .timeline-track { grid-template-columns: repeat(14, 1fr); }
  .timeline-cell { font-size: 0.6rem; }
}

@media (max-width: 420px) {
  .booking-page { padding: 0.75rem 0.5rem 1.5rem; }
  .filter-bar { grid-template-columns: 1fr; }
  .tab { padding: 0.4rem 0.6rem; font-size: 0.75rem; }
  .seat-map { gap: 0.35rem; }
  .row { gap: 0.2rem; grid-template-columns: 18px repeat(5, 1fr); }
  .row-label { font-size: 0.65rem; }
  .seat { max-width: none; border-radius: 8px; }
  .seat-num { font-size: 0.7rem; }
  .seat-txt { font-size: 0.55rem; }
  .stats-row { grid-template-columns: repeat(3, 1fr); gap: 0.4rem; }
  .stat-num { font-size: 1rem; }
  .stat-lbl { font-size: 0.65rem; }
  .timeline-cell { font-size: 0.55rem; }
}
</style>
