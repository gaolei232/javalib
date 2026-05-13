<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-badge">LS</div>
        <div>
          <h1>管理员控制台</h1>
          <p>座位预约管理系统</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <button
          v-for="item in navItems"
          :key="item.key"
          class="sidebar-item"
          :class="{ active: activeTab === item.key }"
          @click="activeTab = item.key"
        >
          <span class="sidebar-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-pill">
          <span class="admin-avatar">A</span>
          <div>
            <strong>{{ adminName }}</strong>
            <p>系统管理员</p>
          </div>
        </div>
        <button class="logout-button" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <div class="main-shell">
      <header class="topbar">
        <div>
          <h2>{{ currentTabTitle }}</h2>
          <p>{{ currentTabDesc }}</p>
        </div>
        <div class="topbar-actions">
          <button class="secondary-btn" @click="reloadActiveTab">刷新当前模块</button>
        </div>
      </header>

      <main class="page-content">
        <!-- Dashboard -->
        <section v-if="activeTab === 'dashboard'" class="page-section">
          <div class="hero-grid">
            <div class="hero-card hero-primary">
              <div class="hero-card-header">
                <span>系统总览</span>
                <span class="hero-tag">实时</span>
              </div>
              <h3>欢迎回来，{{ adminName }}</h3>
              <p>这里展示预约系统当前概况，你可以查看核心指标、最近预约和异常状态。</p>
              <div class="hero-metrics">
                <div>
                  <span>总用户</span>
                  <strong>{{ totalUsers }}</strong>
                </div>
                <div>
                  <span>今日预约</span>
                  <strong>{{ todayBookings }}</strong>
                </div>
                <div>
                  <span>座位总数</span>
                  <strong>{{ totalSeats }}</strong>
                </div>
              </div>
            </div>

            <div class="quick-stats">
              <div class="stat-card">
                <div class="stat-icon">U</div>
                <div>
                  <p>总用户数</p>
                  <strong>{{ totalUsers }}</strong>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">B</div>
                <div>
                  <p>今日预约</p>
                  <strong>{{ todayBookings }}</strong>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">S</div>
                <div>
                  <p>总座位数</p>
                  <strong>{{ totalSeats }}</strong>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">R</div>
                <div>
                  <p>使用率</p>
                  <strong>{{ usageRate }}%</strong>
                </div>
              </div>
            </div>
          </div>

          <div class="content-grid two">
            <section class="panel">
              <div class="panel-header">
                <h3>最近活动</h3>
                <span class="muted-text">{{ recentActivities.length }} 条</span>
              </div>
              <div v-if="recentActivities.length === 0" class="empty-box">暂无最近活动</div>
              <div v-else class="activity-list">
                <div
                  v-for="activity in recentActivities"
                  :key="activity.id"
                  class="activity-item"
                >
                  <div class="activity-time">{{ activity.time }}</div>
                  <div class="activity-body">
                    <strong>{{ activity.content }}</strong>
                    <span>{{ activity.user }}</span>
                  </div>
                </div>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <h3>系统统计</h3>
              </div>
              <div class="kv-grid">
                <div class="kv-card">
                  <span>学生总数</span>
                  <strong>{{ systemStats.totalStudents }}</strong>
                </div>
                <div class="kv-card">
                  <span>总预约</span>
                  <strong>{{ systemStats.totalBookings }}</strong>
                </div>
                <div class="kv-card">
                  <span>已完成</span>
                  <strong>{{ systemStats.completedBookings }}</strong>
                </div>
                <div class="kv-card">
                  <span>已取消</span>
                  <strong>{{ systemStats.cancelledBookings }}</strong>
                </div>
                <div class="kv-card">
                  <span>平均时长</span>
                  <strong>{{ systemStats.avgHours }}h</strong>
                </div>
              </div>
            </section>
          </div>
        </section>

        <!-- Bookings -->
        <section v-if="activeTab === 'bookings'" class="page-section">
          <div class="toolbar">
            <div class="toolbar-group">
              <label>
                日期
                <input v-model="bookingFilterDate" type="date" />
              </label>
              <label>
                状态
                <select v-model="bookingFilterStatus">
                  <option value="all">全部</option>
                  <option value="BOOKED">已预约</option>
                  <option value="USED">已使用</option>
                  <option value="CANCELLED">已取消</option>
                  <option value="EXPIRED">已过期</option>
                </select>
              </label>
            </div>
            <div class="toolbar-group">
              <input
                v-model="bookingSearch"
                class="search-input"
                type="text"
                placeholder="搜索用户、座位、日期..."
              />
            </div>
          </div>

          <section class="panel">
            <div class="panel-header">
              <h3>预约列表</h3>
              <span class="muted-text">{{ filteredBookings.length }} 条</span>
            </div>

            <div v-if="filteredBookings.length === 0" class="empty-box">暂无符合条件的预约记录</div>

            <div v-else class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>用户</th>
                    <th>座位</th>
                    <th>日期</th>
                    <th>时间段</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="booking in filteredBookings" :key="booking.id">
                    <td>#{{ booking.id }}</td>
                    <td>{{ booking.userName }}</td>
                    <td>{{ booking.seatId }}</td>
                    <td>{{ formatDate(booking.date) }}</td>
                    <td>{{ getBookingTimeLabel(booking) }}</td>
                    <td>
                      <span :class="['status-badge', normalizeStatusClass(booking.status)]">
                        {{ getStatusLabel(booking.status) }}
                      </span>
                    </td>
                    <td>{{ formatDateTime(booking.createdAt) }}</td>
                    <td>
                      <div class="action-row">
                        <button class="action-button view" @click="viewBookingDetail(booking)">
                          查看
                        </button>
                        <button
                          v-if="booking.status === 'BOOKED'"
                          class="action-button cancel"
                          @click="cancelBooking(booking)"
                        >
                          取消
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </section>

        <!-- Users -->
        <section v-if="activeTab === 'users'" class="page-section">
          <div class="toolbar">
            <div class="toolbar-group grow">
              <input
                v-model="userSearch"
                class="search-input"
                type="text"
                placeholder="搜索姓名或邮箱..."
              />
            </div>
            <div class="toolbar-group">
              <button class="primary-btn" @click="openAddUserModal">添加用户</button>
            </div>
          </div>

          <section class="panel">
            <div class="panel-header">
              <h3>用户列表</h3>
              <span class="muted-text">{{ filteredUsers.length }} 人</span>
            </div>

            <div v-if="filteredUsers.length === 0" class="empty-box">暂无用户数据</div>

            <div v-else class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>姓名</th>
                    <th>邮箱</th>
                    <th>角色</th>
                    <th>注册时间</th>
                    <th>预约次数</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="user in filteredUsers" :key="user.id">
                    <td>#{{ user.id }}</td>
                    <td>{{ user.name }}</td>
                    <td>{{ user.email }}</td>
                    <td>
                      <span :class="['role-badge', user.role.toLowerCase()]">
                        {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                      </span>
                    </td>
                    <td>{{ formatDateTime(user.createdAt) }}</td>
                    <td>{{ getUserBookingCount(user.id) }}</td>
                    <td>
                      <div class="action-row">
                        <button class="action-button view" @click="viewUserDetail(user)">查看</button>
                        <button class="action-button edit" @click="openEditUserModal(user)">编辑</button>
                        <button class="action-button delete" @click="deleteUser(user.id)">删除</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </section>

        <!-- Seats -->
        <section v-if="activeTab === 'seats'" class="page-section">
          <div class="toolbar">
            <div class="toolbar-group">
              <button class="primary-btn" @click="initializeSeats">初始化座位</button>
              <button class="secondary-btn" @click="refreshSeats">刷新座位</button>
            </div>
            <div class="toolbar-group">
              <label>
                楼栋
                <select v-model="seatBuildingFilter">
                  <option v-for="building in seatBuildingOptions" :key="building.value" :value="building.value">
                    {{ building.label }}
                  </option>
                </select>
              </label>
              <label>
                楼层
                <select v-model.number="seatFloorFilter">
                  <option v-for="floor in seatFloorOptions" :key="floor" :value="floor">
                    {{ floor }} 层
                  </option>
                </select>
              </label>
            </div>
          </div>
          <div class="floor-switch">
            <span class="floor-switch-label">楼层快捷切换</span>
            <div class="floor-switch-list">
              <button
                v-for="floor in seatFloorOptions"
                :key="`floor-tab-${floor}`"
                class="floor-switch-item"
                :class="{ active: seatFloorFilter === floor }"
                @click="seatFloorFilter = floor"
              >
                {{ floor }} 层
              </button>
            </div>
          </div>

          <div class="content-grid two">
            <section class="panel">
              <div class="panel-header">
                <h3>座位布局</h3>
                <span class="muted-text">{{ sortedSeats.length }} 个</span>
              </div>

              <div class="seat-legend">
                <div class="legend-chip"><span class="dot available"></span>可用</div>
                <div class="legend-chip"><span class="dot booked"></span>已预约</div>
                <div class="legend-chip"><span class="dot used"></span>已使用</div>
                <div class="legend-chip"><span class="dot future-booked"></span>未来预约</div>
              </div>

              <div v-if="sortedSeats.length === 0" class="empty-box">暂无座位数据</div>

              <div v-else class="seat-grid">
                <button
                  v-for="seat in sortedSeats"
                  :key="seat.id"
                  :class="[
                    'admin-seat',
                    normalizeStatusClass(seat.status),
                    { selected: selectedSeat === seat.id }
                  ]"
                  @click="selectSeat(seat)"
                >
                  {{ seat.row }}-{{ seat.col }}
                </button>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <h3>座位详情</h3>
              </div>

              <div v-if="selectedSeatDetail" class="detail-card">
                <div class="detail-row"><span>座位编号</span><strong>{{ selectedSeatDetail.seatId }}</strong></div>
                <div class="detail-row"><span>状态</span><strong>{{ getStatusLabel(selectedSeatDetail.status) }}</strong></div>
                <div class="detail-row">
                  <span>最近预约人</span>
                  <strong>{{ selectedSeatDetail.lastBookedBy || '-' }}</strong>
                </div>
                <div class="detail-row">
                  <span>最近预约时间</span>
                  <strong>{{ formatDateTime(selectedSeatDetail.lastBookedAt) }}</strong>
                </div>
                <div class="detail-row">
                  <span>靠窗</span>
                  <strong>{{ selectedSeatDetail.nearWindow ? '是' : '否' }}</strong>
                </div>
                <div class="detail-row">
                  <span>插座</span>
                  <strong>{{ selectedSeatDetail.hasOutlet ? '有' : '无' }}</strong>
                </div>
                <div class="detail-row">
                  <span>区域</span>
                  <strong>{{ selectedSeatDetail.quietZone ? '安静区' : '普通区' }}</strong>
                </div>

                <div class="detail-actions">
                  <button class="secondary-btn" @click="resetSeatStatus">重置状态</button>
                  <button class="ghost-btn" @click="selectedSeatDetail = null">关闭</button>
                </div>
              </div>

              <div v-else class="empty-box">请选择左侧座位查看详情</div>
            </section>
          </div>
        </section>

        <!-- Student stats -->
        <section v-if="activeTab === 'student-stats'" class="page-section">
          <div class="toolbar">
            <div class="toolbar-group grow">
              <input
                v-model="studentSearch"
                class="search-input"
                type="text"
                placeholder="搜索学生..."
              />
            </div>
            <div class="toolbar-group">
              <button class="secondary-btn" @click="loadStudentStats">刷新数据</button>
            </div>
          </div>

          <div class="content-grid two">
            <section class="panel">
              <div class="panel-header">
                <h3>学生统计</h3>
                <span class="muted-text">{{ filteredStudentStats.length }} 条</span>
              </div>

              <div v-if="filteredStudentStats.length === 0" class="empty-box">暂无学生统计数据</div>

              <div v-else class="table-wrap">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>姓名</th>
                      <th>预约数</th>
                      <th>已完成</th>
                      <th>取消</th>
                      <th>时长</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="stat in filteredStudentStats" :key="stat.id">
                      <td>{{ stat.studentName }}</td>
                      <td>{{ stat.totalBookings }}</td>
                      <td>{{ stat.completedBookings }}</td>
                      <td>{{ stat.cancelledBookings }}</td>
                      <td>{{ stat.totalHours }}h</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <h3>预约排行榜</h3>
              </div>

              <div v-if="topStudents.length === 0" class="empty-box">暂无排行榜数据</div>

              <div v-else class="ranking-list">
                <div
                  v-for="(student, index) in topStudents"
                  :key="student.id"
                  class="ranking-item"
                >
                  <div class="rank-badge">{{ index + 1 }}</div>
                  <div class="ranking-info">
                    <strong>{{ student.studentName }}</strong>
                    <span>{{ student.totalBookings }} 次预约</span>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </section>

        <!-- Settings -->
        <section v-if="activeTab === 'settings'" class="page-section">
          <div class="content-grid three">
            <section class="panel">
              <div class="panel-header">
                <h3>基本设置</h3>
              </div>
              <div class="form-grid">
                <label>
                  系统名称
                  <input v-model="systemSettings.name" type="text" />
                </label>
                <label>
                  用户最大预约数
                  <input v-model.number="systemSettings.maxBookingsPerUser" type="number" min="1" max="10" />
                </label>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <h3>时间设置</h3>
              </div>
              <div class="form-grid">
                <label>
                  预约开始时间
                  <input v-model="systemSettings.bookingStartTime" type="time" />
                </label>
                <label>
                  预约结束时间
                  <input v-model="systemSettings.bookingEndTime" type="time" />
                </label>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <h3>通知设置</h3>
              </div>
              <div class="toggle-list">
                <label class="toggle-row">
                  <input v-model="systemSettings.enableEmailNotifications" type="checkbox" />
                  <span>启用邮件通知</span>
                </label>
                <label class="toggle-row">
                  <input v-model="systemSettings.enableSmsNotifications" type="checkbox" />
                  <span>启用短信通知</span>
                </label>
              </div>
            </section>
          </div>

          <div class="settings-actions">
            <button class="primary-btn" @click="saveSettings">保存设置</button>
            <button class="secondary-btn" @click="resetSettings">恢复默认</button>
          </div>
        </section>
      </main>
    </div>

    <!-- User modal -->
    <div v-if="showUserModal" class="modal-mask" @click.self="showUserModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ userModalMode === 'add' ? '添加用户' : '编辑用户' }}</h3>
          <button class="icon-close" @click="showUserModal = false">&times;</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label>
              姓名
              <input v-model="userFormData.name" type="text" placeholder="请输入姓名" />
            </label>
            <label>
              邮箱
              <input v-model="userFormData.email" type="email" placeholder="请输入邮箱" />
            </label>
            <label>
              密码
              <input
                v-model="userFormData.password"
                type="password"
                :placeholder="userModalMode === 'add' ? '请输入密码' : '留空则不修改密码'"
              />
            </label>
            <label>
              角色
              <select v-model="userFormData.role">
                <option value="USER">普通用户</option>
                <option value="ADMIN">管理员</option>
              </select>
            </label>
          </div>
        </div>

        <div class="modal-footer">
          <button class="ghost-btn" @click="showUserModal = false">取消</button>
          <button class="primary-btn" @click="saveUser">保存</button>
        </div>
      </div>
    </div>

    <!-- Detail modal -->
    <div v-if="detailModal.visible" class="modal-mask" @click.self="closeDetailModal">
      <div class="modal-card large">
        <div class="modal-header">
          <h3>{{ detailModal.title }}</h3>
          <button class="icon-close" @click="closeDetailModal">&times;</button>
        </div>

        <div class="modal-body">
          <div class="detail-grid">
            <div v-for="item in detailModal.items" :key="item.label" class="detail-grid-item">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="primary-btn" @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <div v-if="showNotification" class="notification" :class="notificationType">
      <span>{{ notificationMessage }}</span>
      <button class="close-notification" @click="showNotification = false">&times;</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as seatApi from '../api/seatApi'
import * as studentStatsApi from '../api/studentStatsApi'
import * as userApi from '../api/userApi'

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
const seatBuildingOptions = [
  { label: '1号楼', value: 'B1' },
  { label: '2号楼', value: 'B2' },
  { label: '3号楼', value: 'B3' }
]
const seatFloorOptions = [1, 2, 3]
const showNotification = ref(false)
const notificationMessage = ref('')
const notificationType = ref('success')

const showUserModal = ref(false)
const userModalMode = ref('add')
const selectedUser = ref(null)
const userFormData = ref({
  name: '',
  email: '',
  password: '',
  role: 'USER'
})

const detailModal = ref({
  visible: false,
  title: '',
  items: []
})

const defaultSettings = {
  name: '座位预约系统',
  maxBookingsPerUser: 3,
  bookingStartTime: '08:00',
  bookingEndTime: '22:00',
  enableEmailNotifications: true,
  enableSmsNotifications: false
}

const systemSettings = ref(loadLocalSettings())

const totalUsers = ref(0)
const todayBookings = ref(0)
const totalSeats = ref(0)
const usageRate = ref(0)
const recentActivities = ref([])
const bookings = ref([])
const users = ref([])
const seats = ref([])
const systemStats = ref({
  totalStudents: 0,
  totalBookings: 0,
  completedBookings: 0,
  cancelledBookings: 0,
  avgHours: 0
})
const studentStats = ref([])
const topStudents = ref([])

const currentTabTitle = computed(() => tabMeta[activeTab.value]?.title || '管理员控制台')
const currentTabDesc = computed(() => tabMeta[activeTab.value]?.desc || '')

const sortedSeats = computed(() => {
  return seats.value
    .filter((seat) => Number(seat.col) <= 5)
    .sort((a, b) => {
    const rowCompare = String(a.row).localeCompare(String(b.row))
    if (rowCompare !== 0) return rowCompare
    return Number(a.col) - Number(b.col)
  })
})

const filteredBookings = computed(() => {
  return bookings.value.filter((booking) => {
    const dateMatch = !bookingFilterDate.value || booking.date === bookingFilterDate.value
    const statusMatch =
      bookingFilterStatus.value === 'all' || booking.status === bookingFilterStatus.value

    const search = bookingSearch.value.trim().toLowerCase()
    const searchMatch =
      !search ||
      String(booking.userName).toLowerCase().includes(search) ||
      String(booking.seatId).toLowerCase().includes(search) ||
      String(booking.date).toLowerCase().includes(search)

    return dateMatch && statusMatch && searchMatch
  })
})

const filteredUsers = computed(() => {
  const search = userSearch.value.trim().toLowerCase()
  if (!search) return users.value

  return users.value.filter((user) => {
    return (
      String(user.name || '').toLowerCase().includes(search) ||
      String(user.email || '').toLowerCase().includes(search)
    )
  })
})

const filteredStudentStats = computed(() => {
  const search = studentSearch.value.trim().toLowerCase()
  if (!search) return studentStats.value

  return studentStats.value.filter((stat) =>
    String(stat.studentName || '').toLowerCase().includes(search)
  )
})

onMounted(async () => {
  bookingFilterDate.value = new Date().toISOString().split('T')[0]
  await initializePage()
})

watch(activeTab, async (tab) => {
  if (tab === 'dashboard') {
    await loadDashboardData()
  }
  if (tab === 'bookings') {
    await loadBookings()
  }
  if (tab === 'users') {
    await loadUsers()
  }
  if (tab === 'seats') {
    await loadSeats()
    await loadSeatStatistics()
  }
  if (tab === 'student-stats') {
    await loadSystemStatistics()
    await loadStudentStats()
  }
})

watch([seatBuildingFilter, seatFloorFilter], async () => {
  if (activeTab.value !== 'seats') return
  selectedSeat.value = null
  selectedSeatDetail.value = null
  await loadSeats()
})

async function initializePage() {
  await Promise.allSettled([
    loadUsers(),
    loadBookings(),
    loadSeatStatistics(),
    loadSystemStatistics(),
    loadStudentStats(),
    loadSeats()
  ])
  await loadDashboardData()
}

async function reloadActiveTab() {
  if (activeTab.value === 'dashboard') {
    await Promise.allSettled([loadDashboardData(), loadSeatStatistics(), loadSystemStatistics()])
  } else if (activeTab.value === 'bookings') {
    await loadBookings()
  } else if (activeTab.value === 'users') {
    await loadUsers()
  } else if (activeTab.value === 'seats') {
    await Promise.allSettled([loadSeats(), loadSeatStatistics()])
  } else if (activeTab.value === 'student-stats') {
    await Promise.allSettled([loadSystemStatistics(), loadStudentStats()])
  } else {
    showNotificationMessage('当前模块无需刷新', 'info')
    return
  }

  showNotificationMessage('模块已刷新', 'success')
}

async function loadDashboardData() {
  try {
    await Promise.allSettled([loadUsers(), loadBookings(), loadSeatStatistics()])

    const today = new Date().toISOString().split('T')[0]
    const bookedOnly = bookings.value.filter((item) => item.status === 'BOOKED')
    todayBookings.value = bookedOnly.filter((item) => item.date === today).length

    recentActivities.value = bookedOnly.slice(0, 8).map((b) => ({
      id: b.id,
      time: formatDateTime(b.createdAt),
      content: `预约座位 ${b.seatId}（${getBookingTimeLabel(b)}）`,
      user: b.userName
    }))
  } catch (error) {
    console.error('加载仪表盘数据失败', error)
  }
}

async function loadSeatStatistics() {
  try {
    const stats = await seatApi.getSeatStatistics()
    totalSeats.value = Number(stats.total || 0)

    const total = Number(stats.total || 0)
    const booked = Number(stats.booked || 0)
    usageRate.value = total > 0 ? Math.round((booked / total) * 100) : 0
  } catch (error) {
    console.error('加载座位统计失败:', error)
  }
}

async function loadSystemStatistics() {
  try {
    const stats = await studentStatsApi.getSystemStatistics()
    systemStats.value = {
      totalStudents: stats.totalStudents || 0,
      totalBookings: stats.totalBookings || 0,
      completedBookings: stats.completedBookings || 0,
      cancelledBookings: stats.cancelledBookings || 0,
      avgHours: stats.avgHours || 0
    }
  } catch (error) {
    console.error('加载系统统计失败:', error)
  }
}

async function loadStudentStats() {
  try {
    const stats = await studentStatsApi.getAllStudentStats()
    studentStats.value = Array.isArray(stats) ? stats : []

    const top = await studentStatsApi.getTopStudents()
    topStudents.value = Array.isArray(top) ? top.slice(0, 10) : []
  } catch (error) {
    console.error('加载学生统计失败:', error)
    studentStats.value = []
    topStudents.value = []
  }
}

async function loadBookings() {
  try {
    if (users.value.length === 0) {
      await loadUsers()
    }

    const allBookings = await seatApi.getAllBookings()
    bookings.value = (Array.isArray(allBookings) ? allBookings : []).map((b) => {
      const user = users.value.find((u) => String(u.id) === String(b.userId))
      return {
        id: b.id,
        userId: b.userId,
        userName: user?.name || `用户ID ${b.userId}`,
        seatId: b.seatId,
        date: b.bookingDate || b.date || '',
        startTime: normalizeTime(b.startTime),
        endTime: normalizeTime(b.endTime),
        timeSlotId: b.timeSlotId || '',
        status: b.status,
        createdAt: b.createdAt
      }
    })
  } catch (error) {
    console.error('加载预约管理数据失败:', error)
    bookings.value = []
  }
}

async function initializeSeats() {
  try {
    await seatApi.initializeSeats()
    showNotificationMessage('座位初始化成功', 'success')
    await Promise.allSettled([loadSeats(), loadSeatStatistics()])
  } catch (error) {
    console.error('初始化座位失败:', error)
    showNotificationMessage('座位初始化失败', 'error')
  }
}

async function loadSeats() {
  const requestToken = ++latestSeatListRequestToken
  try {
    const allSeats = await seatApi.getAllSeats(
      null,
      null,
      seatBuildingFilter.value,
      seatFloorFilter.value
    )
    if (requestToken !== latestSeatListRequestToken) return
    const normalizedSeats = Array.isArray(allSeats) ? allSeats : []
    const seatsInLocation = normalizedSeats.filter(isSeatInSelectedLocation)
    seats.value = dedupeSeatsByPosition(seatsInLocation)
  } catch (error) {
    if (requestToken !== latestSeatListRequestToken) return
    console.error('加载座位失败:', error)
    seats.value = []
  }
}

async function loadUsers() {
  try {
    const allUsers = await userApi.getAllUsers()
    users.value = Array.isArray(allUsers) ? allUsers : []
    totalUsers.value = users.value.length
  } catch (error) {
    console.error('加载用户失败:', error)
    users.value = []
    totalUsers.value = 0
  }
}

async function refreshSeats() {
  await Promise.allSettled([loadSeats(), loadSeatStatistics()])
  showNotificationMessage('座位已刷新', 'success')
}

function normalizeTime(value) {
  if (!value) return ''
  return String(value).slice(0, 5)
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

function formatDateTime(dateTimeString) {
  if (!dateTimeString) return '-'
  const date = new Date(dateTimeString)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getBookingTimeLabel(booking) {
  if (booking?.startTime && booking?.endTime) {
    return `${booking.startTime} - ${booking.endTime}`
  }

  const timeSlots = {
    morning: '上午 (09:00-12:00)',
    afternoon: '下午 (14:00-17:00)',
    evening: '晚上 (18:00-21:00)'
  }

  return timeSlots[booking?.timeSlotId] || booking?.timeSlotId || '-'
}

function getStatusLabel(status) {
  const statusMap = {
    AVAILABLE: '可用',
    BOOKED: '已预约',
    USED: '已使用',
    CANCELLED: '已取消',
    EXPIRED: '已过期',
    FUTURE_BOOKED: '未来预约'
  }
  return statusMap[status] || status || '-'
}

function normalizeStatusClass(status) {
  return String(status || '').toLowerCase().replace(/_/g, '-')
}

function parseSeatLocationFromSeatId(seatId) {
  const match = String(seatId || '').match(/^(B\d+)-F(\d+)-/i)
  if (!match) return null
  return {
    buildingCode: String(match[1]).toUpperCase(),
    floorNo: Number(match[2])
  }
}

function isSeatInSelectedLocation(seat) {
  if (!seat) return false
  const seatBuildingCode = String(seat.buildingCode || '').toUpperCase()
  const seatFloorNo = Number(seat.floorNo)

  if (seatBuildingCode && Number.isFinite(seatFloorNo) && seatFloorNo > 0) {
    return seatBuildingCode === seatBuildingFilter.value && seatFloorNo === Number(seatFloorFilter.value)
  }

  const parsed = parseSeatLocationFromSeatId(seat.seatId)
  if (!parsed) return false
  return parsed.buildingCode === seatBuildingFilter.value && parsed.floorNo === Number(seatFloorFilter.value)
}

function dedupeSeatsByPosition(seatList) {
  const map = new Map()
  for (const seat of seatList) {
    const key = `${String(seat.row || '').toUpperCase()}-${Number(seat.col)}`
    const existing = map.get(key)
    if (!existing) {
      map.set(key, seat)
      continue
    }

    const seatId = String(seat.seatId || '')
    const existingSeatId = String(existing.seatId || '')
    const seatStandard = /^[Bb]\d+-F\d+-/.test(seatId)
    const existingStandard = /^[Bb]\d+-F\d+-/.test(existingSeatId)
    if (seatStandard && !existingStandard) {
      map.set(key, seat)
    }
  }
  return Array.from(map.values())
}

function getUserBookingCount(userId) {
  return bookings.value.filter((booking) => String(booking.userId) === String(userId)).length
}

function viewBookingDetail(booking) {
  detailModal.value = {
    visible: true,
    title: `预约详情 #${booking.id}`,
    items: [
      { label: '预约ID', value: `#${booking.id}` },
      { label: '用户', value: booking.userName },
      { label: '用户ID', value: booking.userId },
      { label: '座位', value: booking.seatId },
      { label: '日期', value: formatDate(booking.date) },
      { label: '时间段', value: getBookingTimeLabel(booking) },
      { label: '状态', value: getStatusLabel(booking.status) },
      { label: '创建时间', value: formatDateTime(booking.createdAt) }
    ]
  }
}

async function cancelBooking(booking) {
  try {
    await seatApi.cancelBookingById(booking.seatId, booking.id)
    showNotificationMessage('预约已取消', 'success')
    await Promise.allSettled([
      loadBookings(),
      loadSeatStatistics(),
      loadSystemStatistics(),
      loadStudentStats(),
      loadDashboardData()
    ])
  } catch (error) {
    console.error('取消预约失败:', error)
    showNotificationMessage('取消预约失败', 'error')
  }
}

function selectSeat(seat) {
  selectedSeat.value = seat.id
  selectedSeatDetail.value = seat
}

async function resetSeatStatus() {
  if (!selectedSeatDetail.value) return

  try {
    await seatApi.resetSeat(selectedSeatDetail.value.seatId)
    showNotificationMessage('座位状态已重置', 'success')
    await Promise.allSettled([loadSeats(), loadSeatStatistics()])
    selectedSeatDetail.value = null
    selectedSeat.value = null
  } catch (error) {
    console.error('重置座位失败:', error)
    showNotificationMessage('重置座位失败', 'error')
  }
}

function openAddUserModal() {
  userModalMode.value = 'add'
  selectedUser.value = null
  userFormData.value = {
    name: '',
    email: '',
    password: '',
    role: 'USER'
  }
  showUserModal.value = true
}

function openEditUserModal(user) {
  userModalMode.value = 'edit'
  selectedUser.value = user
  userFormData.value = {
    name: user.name || '',
    email: user.email || '',
    password: '',
    role: user.role || 'USER'
  }
  showUserModal.value = true
}

async function saveUser() {
  try {
    if (!userFormData.value.name || !userFormData.value.email) {
      showNotificationMessage('请填写完整的姓名和邮箱', 'error')
      return
    }

    const payload = {
      name: userFormData.value.name,
      email: userFormData.value.email,
      role: userFormData.value.role
    }

    if (userFormData.value.password && userFormData.value.password.trim()) {
      payload.password = userFormData.value.password
    }

    if (userModalMode.value === 'add') {
      if (!payload.password) {
        showNotificationMessage('新建用户时必须填写密码', 'error')
        return
      }

      await userApi.createUser(payload)
      showNotificationMessage('用户添加成功', 'success')
    } else {
      await userApi.updateUser(selectedUser.value.id, payload)
      showNotificationMessage('用户更新成功', 'success')
    }

    showUserModal.value = false
    await Promise.allSettled([loadUsers(), loadBookings(), loadDashboardData()])
  } catch (error) {
    console.error('保存用户失败:', error)
    showNotificationMessage('保存用户失败', 'error')
  }
}

async function deleteUser(userId) {
  if (!confirm('确定要删除该用户吗？')) return

  try {
    await userApi.deleteUser(userId)
    showNotificationMessage('用户删除成功', 'success')
    await Promise.allSettled([loadUsers(), loadBookings(), loadDashboardData()])
  } catch (error) {
    console.error('删除用户失败:', error)
    showNotificationMessage('删除用户失败', 'error')
  }
}

function viewUserDetail(user) {
  detailModal.value = {
    visible: true,
    title: `用户详情：${user.name}`,
    items: [
      { label: '用户ID', value: `#${user.id}` },
      { label: '姓名', value: user.name || '-' },
      { label: '邮箱', value: user.email || '-' },
      { label: '角色', value: user.role === 'ADMIN' ? '管理员' : '普通用户' },
      { label: '注册时间', value: formatDateTime(user.createdAt) },
      { label: '预约次数', value: String(getUserBookingCount(user.id)) }
    ]
  }
}

function closeDetailModal() {
  detailModal.value = {
    visible: false,
    title: '',
    items: []
  }
}

function loadLocalSettings() {
  try {
    const raw = localStorage.getItem('admin_system_settings')
    if (!raw) return { ...defaultSettings }
    return { ...defaultSettings, ...JSON.parse(raw) }
  } catch {
    return { ...defaultSettings }
  }
}

function saveSettings() {
  localStorage.setItem('admin_system_settings', JSON.stringify(systemSettings.value))
  showNotificationMessage('设置已保存到本地', 'success')
}

function resetSettings() {
  systemSettings.value = { ...defaultSettings }
  localStorage.setItem('admin_system_settings', JSON.stringify(systemSettings.value))
  showNotificationMessage('已恢复默认设置', 'success')
}

function handleLogout() {
  localStorage.removeItem('user')
  sessionStorage.removeItem('user')
  localStorage.removeItem('token')
  sessionStorage.removeItem('token')
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
</script>

<style scoped>
.admin-shell {
  --bg: #e9eef5;
  --bg-deep: #dde5ef;
  --panel: #e9eef5;
  --panel-2: #edf2f8;
  --text: #334155;
  --text-strong: #1e293b;
  --muted: #64748b;
  --line: rgba(148, 163, 184, 0.18);

  --primary: #5b7cfa;
  --primary-soft: #dbe4ff;
  --success: #34b27b;
  --success-soft: #dff5ea;
  --warn: #d69b2d;
  --warn-soft: #fff2d8;
  --danger: #e16c6c;
  --danger-soft: #fde7e7;
  --purple: #8b7cf6;
  --purple-soft: #ece8ff;

  --shadow-out:
    10px 10px 24px rgba(163, 177, 198, 0.55),
    -10px -10px 24px rgba(255, 255, 255, 0.9);

  --shadow-out-sm:
    6px 6px 14px rgba(163, 177, 198, 0.42),
    -6px -6px 14px rgba(255, 255, 255, 0.92);

  --shadow-in:
    inset 6px 6px 12px rgba(163, 177, 198, 0.45),
    inset -6px -6px 12px rgba(255, 255, 255, 0.92);

  --shadow-in-soft:
    inset 3px 3px 8px rgba(163, 177, 198, 0.35),
    inset -3px -3px 8px rgba(255, 255, 255, 0.95);

  --radius-xl: 28px;
  --radius-lg: 22px;
  --radius-md: 16px;
  --radius-sm: 12px;
  --transition: all 0.22s ease;
}

* {
  box-sizing: border-box;
}

.admin-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(91, 124, 250, 0.08), transparent 24%),
    radial-gradient(circle at bottom right, rgba(139, 124, 246, 0.08), transparent 22%),
    linear-gradient(180deg, #edf2f8 0%, var(--bg) 100%);
  color: var(--text);
  display: grid;
  grid-template-columns: 280px 1fr;
}

.sidebar {
  background: var(--panel);
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: sticky;
  top: 0;
  height: 100vh;
  box-shadow: var(--shadow-out);
  border-radius: 0 28px 28px 0;
}

.sidebar-brand {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 8px 6px;
}

.brand-badge {
  width: 54px;
  height: 54px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: var(--primary);
  background: var(--panel);
  box-shadow: var(--shadow-out-sm);
}

.sidebar-brand h1 {
  margin: 0;
  font-size: 1.05rem;
  color: var(--text-strong);
}

.sidebar-brand p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 0.88rem;
}

.sidebar-nav {
  display: grid;
  gap: 10px;
}

.sidebar-item {
  border: none;
  background: var(--panel);
  color: var(--muted);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: var(--transition);
  text-align: left;
  font-size: 0.95rem;
  box-shadow: var(--shadow-out-sm);
}

.sidebar-item:hover {
  color: var(--text-strong);
  transform: translateY(-1px);
}

.sidebar-item.active {
  color: var(--primary);
  box-shadow: var(--shadow-in);
  font-weight: 700;
}

.sidebar-icon {
  font-size: 1.05rem;
}

.sidebar-footer {
  margin-top: auto;
  display: grid;
  gap: 14px;
}

.admin-pill {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-radius: 18px;
  background: var(--panel);
  box-shadow: var(--shadow-in-soft);
}

.admin-avatar {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: var(--primary);
  background: var(--panel);
  box-shadow: var(--shadow-out-sm);
}

.admin-pill strong {
  display: block;
  color: var(--text-strong);
}

.admin-pill p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 0.85rem;
}

.main-shell {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  padding: 24px 28px 0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.topbar h2 {
  margin: 0;
  font-size: 1.7rem;
  color: var(--text-strong);
}

.topbar p {
  margin: 6px 0 0;
  color: var(--muted);
}

.topbar-actions {
  display: flex;
  gap: 10px;
}

.page-content {
  padding: 24px 28px 28px;
}

.page-section {
  display: grid;
  gap: 20px;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
}

.hero-card {
  background: var(--panel);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--shadow-out);
}

.hero-primary {
  background: linear-gradient(145deg, #eef3fb 0%, #dde6f3 100%);
}

.hero-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-tag {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  color: var(--primary);
  background: var(--panel);
  box-shadow: var(--shadow-in-soft);
}

.hero-card h3 {
  margin: 18px 0 10px;
  font-size: 1.8rem;
  color: var(--text-strong);
}

.hero-card p {
  margin: 0;
  max-width: 60ch;
  line-height: 1.6;
  color: var(--muted);
}

.hero-metrics {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.hero-metrics div {
  border-radius: 18px;
  padding: 14px;
  background: var(--panel);
  box-shadow: var(--shadow-in-soft);
}

.hero-metrics span {
  display: block;
  font-size: 0.85rem;
  color: var(--muted);
}

.hero-metrics strong {
  display: block;
  margin-top: 8px;
  font-size: 1.45rem;
  color: var(--text-strong);
}

.quick-stats {
  display: grid;
  gap: 14px;
}

.stat-card,
.panel,
.kv-card,
.detail-card {
  background: var(--panel);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-out);
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.stat-card {
  padding: 18px;
  display: flex;
  gap: 14px;
  align-items: center;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 1.3rem;
  background: var(--panel);
  box-shadow: var(--shadow-in-soft);
}

.stat-card p {
  margin: 0 0 6px;
  color: var(--muted);
  font-size: 0.88rem;
}

.stat-card strong {
  font-size: 1.35rem;
  color: var(--text-strong);
}

.content-grid {
  display: grid;
  gap: 20px;
}

.content-grid.two {
  grid-template-columns: 1.4fr 1fr;
}

.content-grid.three {
  grid-template-columns: repeat(3, 1fr);
}

.panel {
  padding: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.08rem;
  color: var(--text-strong);
}

.muted-text {
  color: var(--muted);
  font-size: 0.88rem;
}

.activity-list,
.ranking-list {
  display: grid;
  gap: 12px;
}

.activity-item,
.ranking-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 16px;
  background: var(--panel-2);
  box-shadow: var(--shadow-in-soft);
}

.activity-time {
  min-width: 122px;
  color: var(--muted);
  font-size: 0.84rem;
}

.activity-body {
  display: grid;
  gap: 4px;
}

.activity-body strong {
  color: var(--text-strong);
}

.activity-body span {
  color: var(--muted);
  font-size: 0.88rem;
}

.kv-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.kv-card {
  padding: 18px;
}

.kv-card span {
  color: var(--muted);
  font-size: 0.88rem;
}

.kv-card strong {
  display: block;
  margin-top: 8px;
  font-size: 1.35rem;
  color: var(--text-strong);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar-group.grow {
  flex: 1;
}

.floor-switch {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.floor-switch-label {
  color: var(--muted);
  font-size: 0.9rem;
}

.floor-switch-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.floor-switch-item {
  min-height: 36px;
  padding: 0 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  color: var(--muted);
  background: var(--panel);
  box-shadow: var(--shadow-out-sm);
  transition: var(--transition);
}

.floor-switch-item.active {
  color: var(--primary);
  box-shadow: var(--shadow-in);
}

label {
  display: grid;
  gap: 6px;
  font-size: 0.9rem;
  color: var(--muted);
}

input,
select {
  width: 100%;
  min-height: 42px;
  padding: 0 12px;
  border-radius: 14px;
  border: none;
  background: var(--panel);
  color: var(--text-strong);
  box-shadow: var(--shadow-in);
  transition: var(--transition);
}

input:focus,
select:focus {
  outline: none;
  box-shadow:
    inset 4px 4px 8px rgba(163, 177, 198, 0.42),
    inset -4px -4px 8px rgba(255, 255, 255, 0.96),
    0 0 0 2px rgba(91, 124, 250, 0.12);
}

.search-input {
  min-width: 260px;
}

.primary-btn,
.secondary-btn,
.ghost-btn,
.logout-button {
  min-height: 42px;
  padding: 0 16px;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  transition: var(--transition);
  font-weight: 600;
  background: var(--panel);
}

.primary-btn {
  color: var(--primary);
  box-shadow: var(--shadow-out-sm);
}

.primary-btn:hover,
.secondary-btn:hover,
.ghost-btn:hover,
.logout-button:hover {
  transform: translateY(-1px);
}

.primary-btn:active,
.secondary-btn:active,
.ghost-btn:active,
.logout-button:active {
  box-shadow: var(--shadow-in);
  transform: translateY(0);
}

.secondary-btn {
  color: var(--text-strong);
  box-shadow: var(--shadow-out-sm);
}

.ghost-btn {
  color: var(--muted);
  box-shadow: var(--shadow-out-sm);
}

.logout-button {
  color: var(--danger);
  box-shadow: var(--shadow-out-sm);
}

.table-wrap {
  overflow-x: auto;
  border-radius: 18px;
  background: var(--panel);
  box-shadow: var(--shadow-in-soft);
  padding: 8px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  white-space: nowrap;
}

.data-table th {
  color: var(--muted);
  font-weight: 600;
  font-size: 0.9rem;
}

.data-table td {
  color: var(--text-strong);
}

.action-row {
  display: flex;
  gap: 8px;
}

.action-button {
  min-height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: var(--transition);
  font-weight: 600;
  background: var(--panel);
  box-shadow: var(--shadow-out-sm);
}

.action-button:active {
  box-shadow: var(--shadow-in);
}

.action-button.view {
  color: var(--primary);
}

.action-button.edit {
  color: var(--purple);
}

.action-button.delete,
.action-button.cancel {
  color: var(--danger);
}

.status-badge,
.role-badge {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.83rem;
  font-weight: 700;
  box-shadow: var(--shadow-in-soft);
}

.status-badge.booked {
  background: var(--warn-soft);
  color: var(--warn);
}

.status-badge.used {
  background: var(--success-soft);
  color: var(--success);
}

.status-badge.cancelled,
.status-badge.expired {
  background: var(--danger-soft);
  color: var(--danger);
}

.status-badge.available {
  background: #e6f7ed;
  color: #2f855a;
}

.status-badge.future-booked {
  background: var(--purple-soft);
  color: var(--purple);
}

.role-badge.admin {
  background: var(--danger-soft);
  color: var(--danger);
}

.role-badge.user {
  background: var(--primary-soft);
  color: var(--primary);
}

.seat-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.legend-chip {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--panel);
  box-shadow: var(--shadow-out-sm);
  font-size: 0.88rem;
  color: var(--muted);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.dot.available { background: var(--success); }
.dot.booked { background: var(--warn); }
.dot.used { background: var(--danger); }
.dot.future-booked { background: var(--purple); }

.seat-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.admin-seat {
  min-height: 54px;
  border-radius: 16px;
  border: none;
  cursor: pointer;
  transition: var(--transition);
  font-weight: 700;
  background: var(--panel);
  box-shadow: var(--shadow-out-sm);
}

.admin-seat.available {
  color: var(--success);
}

.admin-seat.booked {
  color: var(--warn);
}

.admin-seat.used {
  color: var(--danger);
}

.admin-seat.future-booked {
  color: var(--purple);
}

.admin-seat.selected {
  box-shadow: var(--shadow-in);
  color: var(--primary);
}

.detail-card {
  padding: 18px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed rgba(148, 163, 184, 0.18);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row span {
  color: var(--muted);
}

.detail-actions,
.settings-actions,
.modal-footer {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.ranking-item {
  align-items: center;
}

.rank-badge {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: var(--warn);
  background: var(--panel);
  box-shadow: var(--shadow-in);
}

.ranking-info {
  display: grid;
  gap: 4px;
}

.ranking-info strong {
  color: var(--text-strong);
}

.ranking-info span {
  color: var(--muted);
  font-size: 0.88rem;
}

.form-grid {
  display: grid;
  gap: 14px;
}

.toggle-list {
  display: grid;
  gap: 12px;
}

.toggle-row {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--panel);
  box-shadow: var(--shadow-in-soft);
  color: var(--text-strong);
}

.settings-tip {
  color: var(--muted);
  font-size: 0.9rem;
  margin: 4px 0 0;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(148, 163, 184, 0.18);
  backdrop-filter: blur(10px);
  display: grid;
  place-items: center;
  z-index: 1000;
  padding: 20px;
}

.modal-card {
  width: min(560px, 100%);
  background: var(--panel);
  border-radius: 26px;
  box-shadow: var(--shadow-out);
}

.modal-card.large {
  width: min(720px, 100%);
}

.modal-header,
.modal-body,
.modal-footer {
  padding: 18px 20px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: var(--text-strong);
}

.icon-close,
.close-notification {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  background: var(--panel);
  color: var(--muted);
  box-shadow: var(--shadow-out-sm);
}

.icon-close:active,
.close-notification:active {
  box-shadow: var(--shadow-in);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.detail-grid-item {
  padding: 14px;
  border-radius: 16px;
  background: var(--panel);
  box-shadow: var(--shadow-in-soft);
  display: grid;
  gap: 6px;
}

.detail-grid-item span {
  color: var(--muted);
  font-size: 0.88rem;
}

.detail-grid-item strong {
  color: var(--text-strong);
}

.empty-box {
  min-height: 160px;
  display: grid;
  place-items: center;
  text-align: center;
  color: var(--muted);
  background: var(--panel);
  box-shadow: var(--shadow-in);
  border-radius: 18px;
}

.notification {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 1100;
  min-width: 280px;
  max-width: 420px;
  padding: 14px 16px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  box-shadow: var(--shadow-out);
  background: var(--panel);
}

.notification.success {
  color: var(--success);
}

.notification.error {
  color: var(--danger);
}

.notification.info {
  color: var(--primary);
}

@media (max-width: 1180px) {
  .hero-grid,
  .content-grid.two,
  .content-grid.three {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    height: auto;
    border-radius: 0 0 24px 24px;
  }

  .seat-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .topbar-actions {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .topbar,
  .page-content {
    padding-left: 16px;
    padding-right: 16px;
  }

  .hero-metrics,
  .kv-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .search-input {
    min-width: 100%;
  }

  .seat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .notification {
    right: 12px;
    left: 12px;
    max-width: none;
    min-width: 0;
  }

  .toolbar,
  .toolbar-group,
  .settings-actions,
  .detail-actions,
  .modal-footer,
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .toolbar-group label,
  .toolbar-group input,
  .toolbar-group select,
  .primary-btn,
  .secondary-btn,
  .ghost-btn,
  .logout-button,
  .action-button {
    width: 100%;
  }

  .panel,
  .hero-card,
  .modal-header,
  .modal-body,
  .modal-footer {
    padding-left: 16px;
    padding-right: 16px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .activity-item,
  .ranking-item,
  .stat-card {
    align-items: flex-start;
  }

  .activity-item {
    flex-direction: column;
  }

  .activity-time {
    min-width: 0;
  }

  .data-table th,
  .data-table td {
    padding: 12px 10px;
  }
}

@media (max-width: 420px) {
  .sidebar {
    padding: 16px 12px;
  }

  .topbar,
  .page-content {
    padding-left: 12px;
    padding-right: 12px;
  }

  .hero-card h3 {
    font-size: 1.45rem;
  }

  .seat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-seat {
    min-height: 48px;
  }
}
</style>



