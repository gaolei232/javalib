<template>
  <!--
    管理控制台 — 极简编辑 × 温暖学术
    作用：管理员全局管理界面，含数据总览、预约管理、用户管理、座位管理、学生统计、系统设置
    接口设计：完整照搬原版 seatApi / studentStatsApi / userApi 全部接口
  -->
  <div class="admin-shell">
    <!-- ── 侧栏 ── -->
    <aside class="sidebar">
      <div class="sidebar-head">
        <span class="brand-badge">LS</span>
        <div>
          <h1>管理控制台</h1>
          <p>座位预约管理系统</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <button
          v-for="item in navItems"
          :key="item.key"
          :class="['nav-item', { active: activeTab === item.key }]"
          @click="activeTab = item.key"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-foot">
        <div class="admin-card">
          <span class="avatar">A</span>
          <div>
            <strong>{{ adminName }}</strong>
            <p>系统管理员</p>
          </div>
        </div>
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <!-- ── 主区域 ── -->
    <main class="main-area">
      <header class="topbar">
        <div>
          <h2>{{ currentTabTitle }}</h2>
          <p>{{ currentTabDesc }}</p>
        </div>
        <button class="btn-secondary" @click="reloadActiveTab">刷新当前模块</button>
      </header>

      <div class="content">
        <!-- Dashboard -->
        <section v-if="activeTab === 'dashboard'" class="page-section">
          <div class="dash-hero">
            <div class="hero-card hero-main">
              <div class="hero-head">
                <span>系统总览</span>
                <span class="badge-live">实时</span>
              </div>
              <h3>欢迎回来，{{ adminName }}</h3>
              <p>这里展示预约系统当前概况，你可以查看核心指标、最近预约和异常状态。</p>
              <div class="hero-kpi">
                <div><span>总用户</span><strong>{{ totalUsers }}</strong></div>
                <div><span>今日预约</span><strong>{{ todayBookings }}</strong></div>
                <div><span>座位总数</span><strong>{{ totalSeats }}</strong></div>
              </div>
            </div>
            <div class="stat-grid">
              <div class="stat-card"><span class="stat-icon" style="background:var(--honey);color:var(--amber);">U</span><div><p>总用户数</p><strong>{{ totalUsers }}</strong></div></div>
              <div class="stat-card"><span class="stat-icon" style="background:var(--green-soft);color:var(--green-text);">B</span><div><p>今日预约</p><strong>{{ todayBookings }}</strong></div></div>
              <div class="stat-card"><span class="stat-icon" style="background:var(--red-soft);color:var(--red-text);">S</span><div><p>总座位数</p><strong>{{ totalSeats }}</strong></div></div>
              <div class="stat-card"><span class="stat-icon" style="background:var(--paper-warm);color:var(--text-soft);">R</span><div><p>使用率</p><strong>{{ usageRate }}%</strong></div></div>
            </div>
          </div>

          <div class="dash-bottom">
            <div class="panel">
              <div class="panel-head">
                <h3>最近活动</h3>
                <span class="muted">{{ recentActivities.length }} 条</span>
              </div>
              <div v-if="recentActivities.length === 0" class="empty">暂无最近活动</div>
              <div v-else class="activity-list">
                <div v-for="a in recentActivities" :key="a.id" class="act-item">
                  <span class="act-time">{{ a.time }}</span>
                  <div><strong>{{ a.content }}</strong><span>{{ a.user }}</span></div>
                </div>
              </div>
            </div>

            <div class="panel">
              <div class="panel-head"><h3>系统统计</h3></div>
              <div class="kv-grid">
                <div class="kv-card"><span>学生总数</span><strong>{{ systemStats.totalStudents }}</strong></div>
                <div class="kv-card"><span>总预约</span><strong>{{ systemStats.totalBookings }}</strong></div>
                <div class="kv-card"><span>已完成</span><strong>{{ systemStats.completedBookings }}</strong></div>
                <div class="kv-card"><span>已取消</span><strong>{{ systemStats.cancelledBookings }}</strong></div>
                <div class="kv-card"><span>平均时长</span><strong>{{ systemStats.avgHours }}h</strong></div>
              </div>
            </div>
          </div>
        </section>

        <!-- Bookings -->
        <section v-if="activeTab === 'bookings'" class="page-section">
          <div class="toolbar">
            <div class="tool-group">
              <label>日期 <input v-model="bookingFilterDate" type="date" /></label>
              <label>状态
                <select v-model="bookingFilterStatus">
                  <option value="all">全部</option>
                  <option value="BOOKED">已预约</option>
                  <option value="USED">已使用</option>
                  <option value="CANCELLED">已取消</option>
                  <option value="EXPIRED">已过期</option>
                </select>
              </label>
            </div>
            <input v-model="bookingSearch" class="search-input" type="text" placeholder="搜索用户、座位、日期..." />
          </div>

          <div class="panel">
            <div class="panel-head"><h3>预约列表</h3><span class="muted">{{ filteredBookings.length }} 条</span></div>
            <div v-if="filteredBookings.length === 0" class="empty">暂无符合条件的预约记录</div>
            <div v-else class="table-wrap">
              <table>
                <thead><tr><th>ID</th><th>用户</th><th>座位</th><th>日期</th><th>时间段</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
                <tbody>
                  <tr v-for="b in filteredBookings" :key="b.id">
                    <td>#{{ b.id }}</td><td>{{ b.userName }}</td><td>{{ b.seatId }}</td>
                    <td>{{ formatDate(b.date) }}</td><td>{{ getBookingTimeLabel(b) }}</td>
                    <td><span :class="['tag', statusTagClass(b.status)]">{{ getStatusLabel(b.status) }}</span></td>
                    <td>{{ formatDateTime(b.createdAt) }}</td>
                    <td>
                      <div class="act-row">
                        <button class="btn-ghost btn-xs" @click="viewBookingDetail(b)">查看</button>
                        <button v-if="b.status === 'BOOKED'" class="btn-danger btn-xs" @click="cancelBooking(b)">取消</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>

        <!-- Users -->
        <section v-if="activeTab === 'users'" class="page-section">
          <div class="toolbar">
            <input v-model="userSearch" class="search-input" type="text" placeholder="搜索姓名或邮箱..." />
            <button class="btn-primary" @click="openAddUserModal">添加用户</button>
          </div>
          <div class="panel">
            <div class="panel-head"><h3>用户列表</h3><span class="muted">{{ filteredUsers.length }} 人</span></div>
            <div v-if="filteredUsers.length === 0" class="empty">暂无用户数据</div>
            <div v-else class="table-wrap">
              <table>
                <thead><tr><th>ID</th><th>姓名</th><th>邮箱</th><th>角色</th><th>注册时间</th><th>预约次数</th><th>操作</th></tr></thead>
                <tbody>
                  <tr v-for="u in filteredUsers" :key="u.id">
                    <td>#{{ u.id }}</td><td>{{ u.name }}</td><td>{{ u.email }}</td>
                    <td><span :class="['tag', u.role === 'ADMIN' ? 'tag-danger' : 'tag-default']">{{ u.role === 'ADMIN' ? '管理员' : '普通用户' }}</span></td>
                    <td>{{ formatDateTime(u.createdAt) }}</td><td>{{ getUserBookingCount(u.id) }}</td>
                    <td>
                      <div class="act-row">
                        <button class="btn-ghost btn-xs" @click="viewUserDetail(u)">查看</button>
                        <button class="btn-ghost btn-xs" @click="openEditUserModal(u)">编辑</button>
                        <button class="btn-danger btn-xs" @click="deleteUser(u.id)">删除</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>

        <!-- Seats -->
        <section v-if="activeTab === 'seats'" class="page-section">
          <div class="toolbar">
            <div class="tool-group">
              <button class="btn-primary" @click="initializeSeats">初始化座位</button>
              <button class="btn-secondary" @click="refreshSeats">刷新座位</button>
            </div>
            <div class="tool-group">
              <label>楼栋 <select v-model="seatBuildingFilter"><option v-for="b in seatBuildingOptions" :key="b.value" :value="b.value">{{ b.label }}</option></select></label>
              <label>楼层 <select v-model.number="seatFloorFilter"><option v-for="f in seatFloorOptions" :key="f" :value="f">{{ f }} 层</option></select></label>
            </div>
          </div>
          <div class="floor-tabs">
            <span class="floor-label">楼层快捷切换</span>
            <button v-for="f in seatFloorOptions" :key="f" :class="['floor-tab', { active: seatFloorFilter === f }]" @click="seatFloorFilter = f">{{ f }} 层</button>
          </div>
          <div class="seat-layout">
            <div class="panel">
              <div class="panel-head"><h3>座位布局</h3><span class="muted">{{ sortedSeats.length }} 个</span></div>
              <div class="seat-legend">
                <span class="legend-chip"><i class="d avail"></i>可用</span>
                <span class="legend-chip"><i class="d booked"></i>已预约</span>
                <span class="legend-chip"><i class="d used"></i>已使用</span>
                <span class="legend-chip"><i class="d future"></i>未来预约</span>
              </div>
              <div v-if="sortedSeats.length === 0" class="empty">暂无座位数据</div>
              <div v-else class="seat-grid">
                <button
                  v-for="s in sortedSeats" :key="s.id"
                  :class="['a-seat', statusSeatClass(s.status), { sel: selectedSeat === s.id }]"
                  @click="selectSeat(s)"
                >{{ s.row }}-{{ s.col }}</button>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head"><h3>座位详情</h3></div>
              <div v-if="selectedSeatDetail" class="detail-card">
                <div class="d-row"><span>座位编号</span><strong>{{ selectedSeatDetail.seatId }}</strong></div>
                <div class="d-row"><span>状态</span><strong>{{ getStatusLabel(selectedSeatDetail.status) }}</strong></div>
                <div class="d-row"><span>最近预约人</span><strong>{{ selectedSeatDetail.lastBookedBy || '-' }}</strong></div>
                <div class="d-row"><span>最近预约时间</span><strong>{{ formatDateTime(selectedSeatDetail.lastBookedAt) }}</strong></div>
                <div class="d-row"><span>靠窗</span><strong>{{ selectedSeatDetail.nearWindow ? '是' : '否' }}</strong></div>
                <div class="d-row"><span>插座</span><strong>{{ selectedSeatDetail.hasOutlet ? '有' : '无' }}</strong></div>
                <div class="d-row"><span>区域</span><strong>{{ selectedSeatDetail.quietZone ? '安静区' : '普通区' }}</strong></div>
                <div class="d-act"><button class="btn-secondary" @click="resetSeatStatus">重置状态</button><button class="btn-ghost" @click="selectedSeatDetail = null">关闭</button></div>
              </div>
              <div v-else class="empty">请选择左侧座位查看详情</div>
            </div>
          </div>
        </section>

        <!-- Student stats -->
        <section v-if="activeTab === 'student-stats'" class="page-section">
          <div class="toolbar">
            <input v-model="studentSearch" class="search-input" type="text" placeholder="搜索学生..." />
            <button class="btn-secondary" @click="loadStudentStats">刷新数据</button>
          </div>
          <div class="stats-layout">
            <div class="panel">
              <div class="panel-head"><h3>学生统计</h3><span class="muted">{{ filteredStudentStats.length }} 条</span></div>
              <div v-if="filteredStudentStats.length === 0" class="empty">暂无学生统计数据</div>
              <div v-else class="table-wrap">
                <table>
                  <thead><tr><th>姓名</th><th>预约数</th><th>已完成</th><th>取消</th><th>时长</th></tr></thead>
                  <tbody>
                    <tr v-for="s in filteredStudentStats" :key="s.id"><td>{{ s.studentName }}</td><td>{{ s.totalBookings }}</td><td>{{ s.completedBookings }}</td><td>{{ s.cancelledBookings }}</td><td>{{ s.totalHours }}h</td></tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head"><h3>预约排行榜</h3></div>
              <div v-if="topStudents.length === 0" class="empty">暂无排行榜数据</div>
              <div v-else class="rank-list">
                <div v-for="(s, i) in topStudents" :key="s.id" class="rank-item">
                  <span class="rank-num">{{ i + 1 }}</span>
                  <div><strong>{{ s.studentName }}</strong><span>{{ s.totalBookings }} 次预约</span></div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Settings -->
        <section v-if="activeTab === 'settings'" class="page-section">
          <div class="settings-grid">
            <div class="panel">
              <div class="panel-head"><h3>基本设置</h3></div>
              <div class="form-grid">
                <label>系统名称 <input v-model="systemSettings.name" type="text" /></label>
                <label>用户最大预约数 <input v-model.number="systemSettings.maxBookingsPerUser" type="number" min="1" max="10" /></label>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head"><h3>时间设置</h3></div>
              <div class="form-grid">
                <label>预约开始时间 <input v-model="systemSettings.bookingStartTime" type="time" /></label>
                <label>预约结束时间 <input v-model="systemSettings.bookingEndTime" type="time" /></label>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head"><h3>通知设置</h3></div>
              <div class="toggle-list">
                <label class="toggle-row"><input v-model="systemSettings.enableEmailNotifications" type="checkbox" /><span>启用邮件通知</span></label>
                <label class="toggle-row"><input v-model="systemSettings.enableSmsNotifications" type="checkbox" /><span>启用短信通知</span></label>
              </div>
            </div>
            <div class="panel">
              <div class="panel-head"><h3>预约规则设置</h3></div>
              <div class="form-grid">
                <label>诚信指数阈值 <input v-model.number="integrityThreshold" type="number" min="0" max="1" step="0.05" /></label>
                <p class="muted">低于此值的用户将无法预约座位（0.0 ~ 1.0，默认 0.6）</p>
              </div>
            </div>
          </div>
          <div class="settings-btns">
            <button class="btn-primary" @click="saveSettings">保存设置</button>
            <button class="btn-secondary" @click="resetSettings">恢复默认</button>
          </div>
        </section>
      </div>
    </main>

    <!-- User modal -->
    <div v-if="showUserModal" class="modal-mask" @click.self="showUserModal = false">
      <div class="modal-card">
        <div class="modal-hd"><h3>{{ userModalMode === 'add' ? '添加用户' : '编辑用户' }}</h3><button class="modal-close" @click="showUserModal = false">&times;</button></div>
        <div class="modal-bd">
          <div class="form-grid">
            <label>姓名 <input v-model="userFormData.name" type="text" placeholder="请输入姓名" /></label>
            <label>邮箱 <input v-model="userFormData.email" type="email" placeholder="请输入邮箱" /></label>
            <label>密码 <input v-model="userFormData.password" type="password" :placeholder="userModalMode === 'add' ? '请输入密码' : '留空则不修改密码'" /></label>
            <label>角色 <select v-model="userFormData.role"><option value="USER">普通用户</option><option value="ADMIN">管理员</option></select></label>
          </div>
        </div>
        <div class="modal-ft"><button class="btn-ghost" @click="showUserModal = false">取消</button><button class="btn-primary" @click="saveUser">保存</button></div>
      </div>
    </div>

    <!-- Detail modal -->
    <div v-if="detailModal.visible" class="modal-mask" @click.self="closeDetailModal">
      <div class="modal-card modal-lg">
        <div class="modal-hd"><h3>{{ detailModal.title }}</h3><button class="modal-close" @click="closeDetailModal">&times;</button></div>
        <div class="modal-bd">
          <div class="detail-grid">
            <div v-for="item in detailModal.items" :key="item.label" class="detail-item"><span>{{ item.label }}</span><strong>{{ item.value }}</strong></div>
          </div>
        </div>
        <div class="modal-ft"><button class="btn-primary" @click="closeDetailModal">关闭</button></div>
      </div>
    </div>

    <!-- Notification -->
    <transition name="notif">
      <div v-if="showNotification" :class="['notification', notificationType]">
        <span>{{ notificationMessage }}</span>
        <button class="notif-close" @click="showNotification = false">&times;</button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as seatApi from '../api/seatApi'
import * as studentStatsApi from '../api/studentStatsApi'
import * as userApi from '../api/userApi'
import * as systemApi from '../api/systemApi'

const router = useRouter()

const navItems = [
  { key: 'dashboard', label: '数据总览', icon: 'D' },
  { key: 'bookings', label: '预约管理', icon: 'B' },
  { key: 'users', label: '用户管理', icon: 'U' },
  { key: 'seats', label: '座位管理', icon: 'S' },
  { key: 'student-stats', label: '学生统计', icon: 'T' },
  { key: 'settings', label: '系统设置', icon: 'C' }
]

const tabMeta = {
  dashboard: { title: '系统概览', desc: '查看核心指标、最近活动和系统整体运行情况。' },
  bookings: { title: '预约管理', desc: '筛选、查看和取消预约记录。' },
  users: { title: '用户管理', desc: '管理普通用户与管理员账号。' },
  seats: { title: '座位管理', desc: '按楼栋楼层查看座位状态并执行重置。' },
  'student-stats': { title: '学生统计', desc: '查看学生预约行为与排行榜。' },
  settings: { title: '系统设置', desc: '调整本地管理端显示设置。' }
}

const activeTab = ref('dashboard')
const adminName = ref('管理员')
const bookingFilterDate = ref('')
const bookingFilterStatus = ref('all')
const bookingSearch = ref('')
const userSearch = ref('')
const studentSearch = ref('')
const selectedSeat = ref(null)
const selectedSeatDetail = ref(null)
let latestSeatListRequestToken = 0
const seatBuildingFilter = ref('B1')
const seatFloorFilter = ref(1)
const seatBuildingOptions = [{ label: '1号楼', value: 'B1' }, { label: '2号楼', value: 'B2' }, { label: '3号楼', value: 'B3' }]
const seatFloorOptions = [1, 2, 3]
const showNotification = ref(false)
const notificationMessage = ref('')
const notificationType = ref('success')
const showUserModal = ref(false)
const userModalMode = ref('add')
const selectedUser = ref(null)
const userFormData = ref({ name: '', email: '', password: '', role: 'USER' })
const detailModal = ref({ visible: false, title: '', items: [] })
const defaultSettings = { name: '座位预约系统', maxBookingsPerUser: 3, bookingStartTime: '08:00', bookingEndTime: '22:00', enableEmailNotifications: true, enableSmsNotifications: false }
const systemSettings = ref(loadLocalSettings())
const integrityThreshold = ref(0.6)

const totalUsers = ref(0)
const todayBookings = ref(0)
const totalSeats = ref(0)
const usageRate = ref(0)
const recentActivities = ref([])
const bookings = ref([])
const users = ref([])
const seats = ref([])
const systemStats = ref({ totalStudents: 0, totalBookings: 0, completedBookings: 0, cancelledBookings: 0, avgHours: 0 })
const studentStats = ref([])
const topStudents = ref([])

const currentTabTitle = computed(() => tabMeta[activeTab.value]?.title || '管理员控制台')
const currentTabDesc = computed(() => tabMeta[activeTab.value]?.desc || '')

const sortedSeats = computed(() => {
  return seats.value.filter(s => Number(s.col) <= 5).sort((a, b) => {
    const rc = String(a.row).localeCompare(String(b.row))
    return rc !== 0 ? rc : Number(a.col) - Number(b.col)
  })
})

const filteredBookings = computed(() => {
  return bookings.value.filter(b => {
    const dm = !bookingFilterDate.value || b.date === bookingFilterDate.value
    const sm = bookingFilterStatus.value === 'all' || b.status === bookingFilterStatus.value
    const q = bookingSearch.value.trim().toLowerCase()
    const qm = !q || String(b.userName).toLowerCase().includes(q) || String(b.seatId).toLowerCase().includes(q) || String(b.date).toLowerCase().includes(q)
    return dm && sm && qm
  })
})

const filteredUsers = computed(() => {
  const q = userSearch.value.trim().toLowerCase()
  if (!q) return users.value
  return users.value.filter(u => String(u.name || '').toLowerCase().includes(q) || String(u.email || '').toLowerCase().includes(q))
})

const filteredStudentStats = computed(() => {
  const q = studentSearch.value.trim().toLowerCase()
  if (!q) return studentStats.value
  return studentStats.value.filter(s => String(s.studentName || '').toLowerCase().includes(q))
})

onMounted(async () => { bookingFilterDate.value = new Date().toISOString().split('T')[0]; await initializePage() })

watch(activeTab, async (tab) => {
  if (tab === 'dashboard') await loadDashboardData()
  if (tab === 'bookings') await loadBookings()
  if (tab === 'users') await loadUsers()
  if (tab === 'seats') { await loadSeats(); await loadSeatStatistics() }
  if (tab === 'student-stats') { await loadSystemStatistics(); await loadStudentStats() }
})

watch([seatBuildingFilter, seatFloorFilter], async () => {
  if (activeTab.value !== 'seats') return
  selectedSeat.value = null; selectedSeatDetail.value = null; await loadSeats()
})

async function initializePage() {
  await Promise.allSettled([loadUsers(), loadBookings(), loadSeatStatistics(), loadSystemStatistics(), loadStudentStats(), loadSeats()])
  await loadDashboardData()
  await loadIntegrityThreshold()
}

async function reloadActiveTab() {
  if (activeTab.value === 'dashboard') await Promise.allSettled([loadDashboardData(), loadSeatStatistics(), loadSystemStatistics()])
  else if (activeTab.value === 'bookings') await loadBookings()
  else if (activeTab.value === 'users') await loadUsers()
  else if (activeTab.value === 'seats') await Promise.allSettled([loadSeats(), loadSeatStatistics()])
  else if (activeTab.value === 'student-stats') await Promise.allSettled([loadSystemStatistics(), loadStudentStats()])
  else { showNotificationMessage('当前模块无需刷新', 'info'); return }
  showNotificationMessage('模块已刷新', 'success')
}

async function loadDashboardData() {
  try {
    await Promise.allSettled([loadUsers(), loadBookings(), loadSeatStatistics()])
    const today = new Date().toISOString().split('T')[0]
    const bookedOnly = bookings.value.filter(item => item.status === 'BOOKED')
    todayBookings.value = bookedOnly.filter(item => item.date === today).length
    recentActivities.value = bookedOnly.slice(0, 8).map(b => ({ id: b.id, time: formatDateTime(b.createdAt), content: `预约座位 ${b.seatId}（${getBookingTimeLabel(b)}）`, user: b.userName }))
  } catch { /* ignore */ }
}

async function loadSeatStatistics() {
  try {
    const stats = await seatApi.getSeatStatistics()
    totalSeats.value = Number(stats.total || 0)
    usageRate.value = totalSeats.value > 0 ? Math.round((Number(stats.booked || 0) / totalSeats.value) * 100) : 0
  } catch { /* ignore */ }
}

async function loadSystemStatistics() {
  try {
    const stats = await studentStatsApi.getSystemStatistics()
    systemStats.value = { totalStudents: stats.totalStudents || 0, totalBookings: stats.totalBookings || 0, completedBookings: stats.completedBookings || 0, cancelledBookings: stats.cancelledBookings || 0, avgHours: stats.avgHours || 0 }
  } catch { /* ignore */ }
}

async function loadStudentStats() {
  try {
    const stats = await studentStatsApi.getAllStudentStats()
    studentStats.value = Array.isArray(stats) ? stats : []
    const top = await studentStatsApi.getTopStudents()
    topStudents.value = Array.isArray(top) ? top.slice(0, 10) : []
  } catch { studentStats.value = []; topStudents.value = [] }
}

async function loadBookings() {
  try {
    if (users.value.length === 0) await loadUsers()
    const all = await seatApi.getAllBookings()
    bookings.value = (Array.isArray(all) ? all : []).map(b => {
      const u = users.value.find(uu => String(uu.id) === String(b.userId))
      return { id: b.id, userId: b.userId, userName: u?.name || `用户ID ${b.userId}`, seatId: b.seatId, date: b.bookingDate || b.date || '', startTime: normalizeTime(b.startTime), endTime: normalizeTime(b.endTime), timeSlotId: b.timeSlotId || '', status: b.status, createdAt: b.createdAt }
    })
  } catch { bookings.value = [] }
}

async function initializeSeats() {
  try { await seatApi.initializeSeats(); showNotificationMessage('座位初始化成功', 'success'); await Promise.allSettled([loadSeats(), loadSeatStatistics()]) }
  catch { showNotificationMessage('座位初始化失败', 'error') }
}

async function loadSeats() {
  const rt = ++latestSeatListRequestToken
  try {
    const all = await seatApi.getAllSeats(new Date().toISOString().split('T')[0], null, seatBuildingFilter.value, seatFloorFilter.value)
    if (rt !== latestSeatListRequestToken) return
    const norm = Array.isArray(all) ? all : []
    seats.value = dedupeSeatsByPosition(norm.filter(isSeatInSelectedLocation))
  } catch { if (rt !== latestSeatListRequestToken) return; seats.value = [] }
}

async function loadUsers() {
  try { const all = await userApi.getAllUsers(); users.value = Array.isArray(all) ? all : []; totalUsers.value = users.value.length }
  catch { users.value = []; totalUsers.value = 0 }
}

async function refreshSeats() { await Promise.allSettled([loadSeats(), loadSeatStatistics()]); showNotificationMessage('座位已刷新', 'success') }

function normalizeTime(v) { if (!v) return ''; return String(v).slice(0, 5) }
function formatDate(ds) { if (!ds) return '-'; const d = new Date(ds); if (Number.isNaN(d.getTime())) return '-'; return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }) }
function formatDateTime(ds) { if (!ds) return '-'; const d = new Date(ds); if (Number.isNaN(d.getTime())) return '-'; return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) }

function getBookingTimeLabel(b) {
  if (b?.startTime && b?.endTime) return `${b.startTime} - ${b.endTime}`
  const m = { morning: '上午 (09:00-12:00)', afternoon: '下午 (14:00-17:00)', evening: '晚上 (18:00-21:00)' }
  return m[b?.timeSlotId] || b?.timeSlotId || '-'
}

function getStatusLabel(s) { const m = { AVAILABLE: '可用', BOOKED: '已预约', USED: '已使用', CANCELLED: '已取消', EXPIRED: '已过期', FUTURE_BOOKED: '未来预约' }; return m[s] || s || '-' }
function statusTagClass(s) { const m = { BOOKED: 'tag-warning', USED: 'tag-success', CANCELLED: 'tag-danger', EXPIRED: 'tag-danger', AVAILABLE: 'tag-success', FUTURE_BOOKED: 'tag-default' }; return m[s] || '' }
function statusSeatClass(s) { return String(s || '').toLowerCase().replace(/_/g, '-') }

function parseSeatLocationFromSeatId(sid) { const m = String(sid || '').match(/^(B\d+)-F(\d+)-/i); if (!m) return null; return { buildingCode: String(m[1]).toUpperCase(), floorNo: Number(m[2]) } }
function isSeatInSelectedLocation(seat) {
  if (!seat) return false
  const sbc = String(seat.buildingCode || '').toUpperCase(); const sfn = Number(seat.floorNo)
  if (sbc && Number.isFinite(sfn) && sfn > 0) return sbc === seatBuildingFilter.value && sfn === Number(seatFloorFilter.value)
  const p = parseSeatLocationFromSeatId(seat.seatId); if (!p) return false
  return p.buildingCode === seatBuildingFilter.value && p.floorNo === Number(seatFloorFilter.value)
}

function dedupeSeatsByPosition(list) {
  const map = new Map()
  for (const s of list) {
    const key = `${String(s.row || '').toUpperCase()}-${Number(s.col)}`
    const ex = map.get(key)
    if (!ex) { map.set(key, s); continue }
    const sid = String(s.seatId || ''); const esid = String(ex.seatId || '')
    const ss = /^[Bb]\d+-F\d+-/.test(sid); const es = /^[Bb]\d+-F\d+-/.test(esid)
    if (ss && !es) map.set(key, s)
  }
  return Array.from(map.values())
}

function getUserBookingCount(uid) { return bookings.value.filter(b => String(b.userId) === String(uid)).length }

function viewBookingDetail(b) {
  detailModal.value = {
    visible: true, title: `预约详情 #${b.id}`,
    items: [{ label: '预约ID', value: `#${b.id}` }, { label: '用户', value: b.userName }, { label: '用户ID', value: b.userId }, { label: '座位', value: b.seatId }, { label: '日期', value: formatDate(b.date) }, { label: '时间段', value: getBookingTimeLabel(b) }, { label: '状态', value: getStatusLabel(b.status) }, { label: '创建时间', value: formatDateTime(b.createdAt) }]
  }
}

async function cancelBooking(b) {
  try { await seatApi.cancelBookingById(b.seatId, b.id); showNotificationMessage('预约已取消', 'success'); await Promise.allSettled([loadBookings(), loadSeatStatistics(), loadSystemStatistics(), loadStudentStats(), loadDashboardData()]) }
  catch { showNotificationMessage('取消预约失败', 'error') }
}

function selectSeat(s) { selectedSeat.value = s.id; selectedSeatDetail.value = s }

async function resetSeatStatus() {
  if (!selectedSeatDetail.value) return
  try { await seatApi.resetSeat(selectedSeatDetail.value.seatId); showNotificationMessage('座位状态已重置', 'success'); await Promise.allSettled([loadSeats(), loadSeatStatistics()]); selectedSeatDetail.value = null; selectedSeat.value = null }
  catch { showNotificationMessage('重置座位失败', 'error') }
}

function openAddUserModal() { userModalMode.value = 'add'; selectedUser.value = null; userFormData.value = { name: '', email: '', password: '', role: 'USER' }; showUserModal.value = true }
function openEditUserModal(u) { userModalMode.value = 'edit'; selectedUser.value = u; userFormData.value = { name: u.name || '', email: u.email || '', password: '', role: u.role || 'USER' }; showUserModal.value = true }

async function saveUser() {
  try {
    if (!userFormData.value.name || !userFormData.value.email) { showNotificationMessage('请填写完整的姓名和邮箱', 'error'); return }
    const payload = { name: userFormData.value.name, email: userFormData.value.email, role: userFormData.value.role }
    if (userFormData.value.password?.trim()) payload.password = userFormData.value.password
    if (userModalMode.value === 'add') { if (!payload.password) { showNotificationMessage('新建用户时必须填写密码', 'error'); return }; await userApi.createUser(payload); showNotificationMessage('用户添加成功', 'success') }
    else { await userApi.updateUser(selectedUser.value.id, payload); showNotificationMessage('用户更新成功', 'success') }
    showUserModal.value = false; await Promise.allSettled([loadUsers(), loadBookings(), loadDashboardData()])
  } catch { showNotificationMessage('保存用户失败', 'error') }
}

async function deleteUser(uid) { if (!confirm('确定要删除该用户吗？')) return; try { await userApi.deleteUser(uid); showNotificationMessage('用户删除成功', 'success'); await Promise.allSettled([loadUsers(), loadBookings(), loadDashboardData()]) } catch { showNotificationMessage('删除用户失败', 'error') } }

async function viewUserDetail(u) {
    let integrityText = '-'
    try {
        const integrity = await userApi.getUserIntegrity(u.id)
        integrityText = Math.round((integrity.integrityScore || 0) * 100) + '%'
    } catch { /* use default */ }

    detailModal.value = {
        visible: true, title: `用户详情：${u.name}`,
        items: [
            { label: '用户ID', value: `#${u.id}` },
            { label: '姓名', value: u.name || '-' },
            { label: '邮箱', value: u.email || '-' },
            { label: '角色', value: u.role === 'ADMIN' ? '管理员' : '普通用户' },
            { label: '注册时间', value: formatDateTime(u.createdAt) },
            { label: '预约次数', value: String(getUserBookingCount(u.id)) },
            { label: '诚信指数', value: integrityText }
        ]
    }
}

function closeDetailModal() { detailModal.value = { visible: false, title: '', items: [] } }

function loadLocalSettings() { try { const raw = localStorage.getItem('admin_system_settings'); if (!raw) return { ...defaultSettings }; return { ...defaultSettings, ...JSON.parse(raw) } } catch { return { ...defaultSettings } } }
async function saveSettings() {
    localStorage.setItem('admin_system_settings', JSON.stringify(systemSettings.value))
    try {
        await systemApi.updateConfig('integrity_threshold', String(integrityThreshold.value))
    } catch { /* ignore */ }
    showNotificationMessage('设置已保存', 'success')
}
function resetSettings() { systemSettings.value = { ...defaultSettings }; localStorage.setItem('admin_system_settings', JSON.stringify(systemSettings.value)); showNotificationMessage('已恢复默认设置', 'success') }

async function loadIntegrityThreshold() {
    try {
        const config = await systemApi.getConfig('integrity_threshold')
        if (config?.value) integrityThreshold.value = parseFloat(config.value)
    } catch { integrityThreshold.value = 0.6 }
}

function handleLogout() { localStorage.removeItem('user'); sessionStorage.removeItem('user'); localStorage.removeItem('token'); sessionStorage.removeItem('token'); router.push('/login') }

function showNotificationMessage(msg, type = 'success') { notificationMessage.value = msg; notificationType.value = type; showNotification.value = true; setTimeout(() => { showNotification.value = false }, 3000) }
</script>

<style scoped>
@keyframes fadeUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes notifIn { from { opacity: 0; transform: translateY(-8px) scale(0.97); } to { opacity: 1; transform: translateY(0) scale(1); } }

.admin-shell { min-height: 100vh; display: grid; grid-template-columns: 240px 1fr; background: var(--paper-white); }

/* ── Sidebar ── */
.sidebar { background: linear-gradient(180deg, var(--paper-white) 0%, var(--paper-warm) 100%); border-right: 1px solid var(--border-warm); padding: 1.5rem 1rem; display: flex; flex-direction: column; gap: 1.5rem; position: sticky; top: 0; height: 100vh; }
.sidebar-head { display: flex; align-items: center; gap: 0.75rem; padding: 0 0.5rem; }
.brand-badge { width: 42px; height: 42px; border-radius: 12px; background: var(--amber-gradient); display: grid; place-items: center; color: #fff; font-family: var(--font-display); font-weight: 800; font-size: 1.1rem; }
.sidebar-head h1 { font-size: 0.95rem; color: var(--text-body); }
.sidebar-head p { font-size: 0.72rem; color: var(--text-subtle); margin-top: 0.15rem; }

.sidebar-nav { display: grid; gap: 0.35rem; }
.nav-item { display: flex; align-items: center; gap: 0.6rem; padding: 0.65rem 1rem; border-radius: var(--radius-md); font-weight: 500; font-size: 0.88rem; color: var(--text-soft); background: transparent; transition: all var(--ease-smooth); }
.nav-item:hover { color: var(--text-body); background: rgba(180, 83, 9, 0.04); }
.nav-item.active { color: var(--amber); background: var(--amber-soft); font-weight: 700; }
.nav-icon { font-size: 0.9rem; }

.sidebar-foot { margin-top: auto; display: grid; gap: 0.6rem; }
.admin-card { display: flex; align-items: center; gap: 0.6rem; padding: 0.65rem 0.75rem; border-radius: var(--radius-md); background: var(--card-pure); border: 1px solid var(--border-warm); }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: #d4a574; display: grid; place-items: center; color: #fff; font-weight: 700; font-size: 0.85rem; }
.admin-card strong { font-size: 0.85rem; color: var(--text-body); }
.admin-card p { font-size: 0.7rem; color: var(--text-subtle); }
.btn-logout { padding: 0.55rem; border-radius: var(--radius-sm); text-align: center; font-size: 0.85rem; color: var(--red-text); font-weight: 600; background: var(--red-soft); border: 1px solid rgba(153,27,27,0.15); cursor: pointer; }

/* ── Main ── */
.main-area { min-width: 0; display: flex; flex-direction: column; }
.topbar { padding: 1.5rem 2rem 0; display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; }
.topbar h2 { font-size: 1.5rem; font-weight: 800; color: var(--text-strong); }
.topbar p { color: var(--text-soft); margin-top: 0.2rem; font-size: 0.88rem; }
.content { padding: 1.5rem 2rem 2rem; }
.page-section { display: grid; gap: 1.25rem; animation: fadeUp 0.3s ease; }

/* ── Dashboard ── */
.dash-hero { display: grid; grid-template-columns: 1.5fr 1fr; gap: 1.25rem; }

.hero-card { background: var(--card-pure); border-radius: var(--radius-xl); padding: 1.5rem; border: 1px solid var(--border-warm); }
.hero-main { background: linear-gradient(135deg, var(--honey) 0%, #fef9f0 100%); border-color: var(--honey-border); }
.hero-head { display: flex; justify-content: space-between; align-items: center; font-size: 0.85rem; font-weight: 600; color: var(--honey-text); }
.badge-live { padding: 0.2rem 0.55rem; border-radius: var(--radius-pill); font-size: 0.7rem; background: var(--card-pure); color: var(--amber); }
.hero-card h3 { font-size: 1.3rem; font-weight: 800; color: var(--text-body); margin: 0.75rem 0 0.5rem; }
.hero-card p { font-size: 0.88rem; color: var(--text-soft); line-height: 1.6; }
.hero-kpi { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.75rem; margin-top: 1.25rem; }
.hero-kpi div { background: var(--card-pure); border-radius: var(--radius-md); padding: 0.8rem; text-align: center; border: 1px solid var(--border-warm); }
.hero-kpi span { font-size: 0.75rem; color: var(--text-subtle); display: block; }
.hero-kpi strong { font-size: 1.2rem; font-weight: 800; color: var(--text-body); display: block; margin-top: 0.25rem; }

.stat-grid { display: grid; gap: 0.8rem; }
.stat-card { background: var(--card-pure); border-radius: var(--radius-lg); padding: 1rem; border: 1px solid var(--border-warm); display: flex; align-items: center; gap: 0.8rem; }
.stat-icon { width: 44px; height: 44px; border-radius: 12px; display: grid; place-items: center; font-weight: 800; font-size: 1.1rem; }
.stat-card p { font-size: 0.78rem; color: var(--text-subtle); }
.stat-card strong { font-size: 1.15rem; font-weight: 800; color: var(--text-body); display: block; margin-top: 0.15rem; }

.dash-bottom { display: grid; grid-template-columns: 1.5fr 1fr; gap: 1.25rem; }

/* ── Panel ── */
.panel { background: var(--card-pure); border-radius: var(--radius-xl); padding: 1.5rem; border: 1px solid var(--border-warm); }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.panel-head h3 { font-size: 1rem; color: var(--text-body); }
.muted { color: var(--text-subtle); font-size: 0.82rem; }
.empty { padding: 1.5rem; text-align: center; color: var(--text-subtle); }

/* ── Activity ── */
.activity-list { display: grid; gap: 0.5rem; }
.act-item { display: flex; gap: 0.8rem; padding: 0.7rem 0.85rem; border-radius: var(--radius-md); background: var(--paper-white); border: 1px solid var(--border-warm); }
.act-time { font-size: 0.78rem; color: var(--text-subtle); min-width: 110px; white-space: nowrap; }
.act-item strong { display: block; font-size: 0.88rem; color: var(--text-body); }
.act-item span { font-size: 0.78rem; color: var(--text-subtle); }

/* ── KV grid ── */
.kv-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.kv-card { background: var(--paper-white); border-radius: var(--radius-md); padding: 1rem; text-align: center; border: 1px solid var(--border-warm); }
.kv-card span { font-size: 0.75rem; color: var(--text-subtle); }
.kv-card strong { display: block; font-size: 1.1rem; font-weight: 800; color: var(--text-body); margin-top: 0.25rem; }

/* ── Toolbar ── */
.toolbar { display: flex; justify-content: space-between; gap: 0.8rem; align-items: center; flex-wrap: wrap; }
.tool-group { display: flex; gap: 0.8rem; align-items: center; flex-wrap: wrap; }
.toolbar label { display: grid; gap: 0.3rem; font-size: 0.8rem; color: var(--text-subtle); }
.search-input { min-width: 240px; }

/* ── Table ── */
.table-wrap { overflow-x: auto; border-radius: var(--radius-lg); border: 1px solid var(--border-warm); }
table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
th, td { padding: 0.7rem 0.8rem; text-align: left; border-bottom: 1px solid var(--border-warm); white-space: nowrap; }
th { color: var(--text-subtle); font-weight: 600; font-size: 0.8rem; background: var(--paper-white); }
td { color: var(--text-body); }

.act-row { display: flex; gap: 0.4rem; }
.btn-xs { padding: 0.3rem 0.7rem; font-size: 0.78rem; border-radius: var(--radius-xs); }

/* ── Floor tabs ── */
.floor-tabs { display: flex; align-items: center; gap: 0.5rem; }
.floor-label { font-size: 0.85rem; color: var(--text-subtle); }
.floor-tab { padding: 0.4rem 1rem; border-radius: var(--radius-sm); font-size: 0.85rem; font-weight: 500; color: var(--text-soft); background: var(--card-pure); border: 1px solid var(--border-warm); cursor: pointer; transition: all var(--ease-smooth); }
.floor-tab.active { color: var(--amber); border-color: var(--amber); background: var(--amber-soft); }

/* ── Seat layout ── */
.seat-layout { display: grid; grid-template-columns: 1.4fr 1fr; gap: 1.25rem; align-items: start; }
.seat-legend { display: flex; gap: 0.6rem; margin-bottom: 0.8rem; flex-wrap: wrap; }
.legend-chip { display: flex; align-items: center; gap: 0.3rem; font-size: 0.78rem; color: var(--text-soft); }
.d { width: 8px; height: 8px; border-radius: 50%; }
.d.avail { background: #22c55e; }
.d.booked { background: #f59e0b; }
.d.used { background: #ef4444; }
.d.future { background: #8b5cf6; }

.seat-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 0.5rem; }
.a-seat { min-height: 48px; border-radius: var(--radius-md); border: none; cursor: pointer; font-weight: 700; font-size: 0.82rem; background: var(--paper-white); border: 1px solid var(--border-warm); color: var(--text-soft); transition: all var(--ease-smooth); }
.a-seat:hover { transform: translateY(-2px); }
.a-seat.available { color: #15803d; border-color: rgba(34,197,94,0.3); }
.a-seat.booked { color: #b45309; border-color: rgba(245,158,11,0.3); }
.a-seat.used { color: #991b1b; border-color: rgba(239,68,68,0.3); }
.a-seat.future-booked { color: #6d28d9; border-color: rgba(139,92,246,0.3); }
.a-seat.sel { border-color: var(--amber); background: var(--amber-soft); }

/* ── Detail card ── */
.detail-card { display: grid; gap: 0; }
.d-row { display: flex; justify-content: space-between; padding: 0.6rem 0; border-bottom: 1px dashed var(--border-warm); }
.d-row:last-child { border: none; }
.d-row span { color: var(--text-subtle); font-size: 0.85rem; }
.d-row strong { color: var(--text-body); font-size: 0.88rem; }
.d-act { display: flex; gap: 0.6rem; margin-top: 0.8rem; }

/* ── Stats layout ── */
.stats-layout { display: grid; grid-template-columns: 1.4fr 1fr; gap: 1.25rem; }

/* ── Rank ── */
.rank-list { display: grid; gap: 0.5rem; }
.rank-item { display: flex; align-items: center; gap: 0.75rem; padding: 0.6rem 0.8rem; border-radius: var(--radius-md); background: var(--paper-white); border: 1px solid var(--border-warm); }
.rank-num { width: 36px; height: 36px; border-radius: 50%; display: grid; place-items: center; font-weight: 800; color: var(--amber); background: var(--honey); }
.rank-item strong { display: block; font-size: 0.88rem; color: var(--text-body); }
.rank-item span { font-size: 0.78rem; color: var(--text-subtle); }

/* ── Settings ── */
.settings-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.25rem; }
.form-grid { display: grid; gap: 0.8rem; }
.form-grid label { display: grid; gap: 0.3rem; font-size: 0.82rem; color: var(--text-soft); }
.toggle-list { display: grid; gap: 0.6rem; }
.toggle-row { display: flex; align-items: center; gap: 0.5rem; padding: 0.6rem 0.75rem; border-radius: var(--radius-md); background: var(--paper-white); border: 1px solid var(--border-warm); font-size: 0.88rem; color: var(--text-body); cursor: pointer; }
.settings-btns { display: flex; gap: 0.8rem; }

/* ── Modal ── */
.modal-mask { position: fixed; inset: 0; background: rgba(139,115,85,0.12); backdrop-filter: blur(8px); display: grid; place-items: center; z-index: 1000; padding: 1.5rem; }
.modal-card { width: min(520px, 100%); background: var(--card-pure); border-radius: var(--radius-xl); border: 1px solid var(--border-warm); box-shadow: var(--shadow-elevated); }
.modal-lg { width: min(680px, 100%); }
.modal-hd { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--border-warm); }
.modal-hd h3 { color: var(--text-body); }
.modal-close { width: 32px; height: 32px; border-radius: 50%; background: var(--paper-white); border: 1px solid var(--border-warm); color: var(--text-soft); font-size: 1.1rem; cursor: pointer; display: grid; place-items: center; }
.modal-bd { padding: 1.5rem; }
.modal-ft { padding: 1rem 1.5rem; border-top: 1px solid var(--border-warm); display: flex; gap: 0.75rem; justify-content: flex-end; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.detail-item { padding: 0.75rem; border-radius: var(--radius-md); background: var(--paper-white); border: 1px solid var(--border-warm); display: grid; gap: 0.25rem; }
.detail-item span { color: var(--text-subtle); font-size: 0.8rem; }
.detail-item strong { color: var(--text-body); font-size: 0.9rem; }

/* ── Notification ── */
.notification { position: fixed; top: 1.5rem; right: 1.5rem; z-index: 1100; padding: 0.9rem 1.2rem; border-radius: var(--radius-lg); display: flex; align-items: center; gap: 0.8rem; color: #fff; font-weight: 600; font-size: 0.9rem; box-shadow: var(--shadow-elevated); animation: notifIn 0.2s ease; }
.notification.success { background: var(--amber); }
.notification.error { background: #ef4444; }
.notification.info { background: #6366f1; }
.notif-close { background: rgba(255,255,255,0.2); color: #fff; padding: 0.3rem 0.6rem; border-radius: 999px; box-shadow: none; }
.notif-enter-active, .notif-leave-active { transition: all 0.25s ease; }
.notif-enter-from, .notif-leave-to { opacity: 0; transform: translateY(-8px); }

/* ── Responsive ── */
@media (max-width: 1180px) {
  .dash-hero, .dash-bottom, .stats-layout, .seat-layout, .settings-grid { grid-template-columns: 1fr; }
}
@media (max-width: 960px) {
  .admin-shell { grid-template-columns: 1fr; }
  .sidebar { position: static; height: auto; border-radius: 0 0 20px 20px; }
}
@media (max-width: 640px) {
  .topbar, .content { padding-left: 1rem; padding-right: 1rem; }
  .hero-kpi, .kv-grid, .detail-grid { grid-template-columns: 1fr; }
  .search-input { min-width: 100%; }
  .seat-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .toolbar, .settings-btns, .d-act, .act-row { flex-direction: column; align-items: stretch; }
  .notification { right: 0.75rem; left: 0.75rem; }
}
</style>
