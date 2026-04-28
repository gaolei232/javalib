<template>
  <div class="aaa-page">
    <div class="sidebar">
      <div class="sidebar-header">图书馆座位管理</div>
      <ul class="nav-menu">
        <li
          v-for="tab in tabs"
          :key="tab.id"
          class="nav-item"
          :class="{ active: currentTab === tab.id }"
          @click="currentTab = tab.id"
        >
          {{ tab.icon }} {{ tab.name }}
        </li>
      </ul>
    </div>

    <div class="main-content">
      <div class="top-header">
        <h2>控制台</h2>
        <div>欢迎, 管理员 Admin</div>
      </div>

      <div class="content-area">
        <transition name="fade" mode="out-in">
          <section v-if="currentTab === 'overview'" key="overview">
            <h2 class="section-title">实时数据总览</h2>
            <div class="dashboard-cards">
              <div class="card" v-for="(value, key) in overviewData" :key="key">
                <h3>{{ value.label }}</h3>
                <div class="number" :style="{ color: value.color }">{{ value.count }}</div>
              </div>
            </div>
            <div class="table-container">
              <h3>近期系统公告</h3>
              <p class="announcement">
                期末考试周将至，开放时间延长至晚上23:00。请同学们自觉维护阅览室纪律。
              </p>
            </div>
          </section>

          <section v-else-if="currentTab === 'reservations'" key="reservations">
            <h2 class="section-title">预约记录管理</h2>
            <div class="table-container">
              <table>
                <thead>
                  <tr>
                    <th>预约单号</th>
                    <th>学生学号</th>
                    <th>座位号</th>
                    <th>预约时间段</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="res in reservations" :key="res.id">
                    <td>{{ res.id }}</td>
                    <td>{{ res.studentId }}</td>
                    <td>{{ res.seatNo }}</td>
                    <td>{{ res.time }}</td>
                    <td>
                      <span class="status" :class="res.statusClass">{{ res.statusText }}</span>
                    </td>
                    <td>
                      <button
                        v-if="res.status === 'using'"
                        class="btn btn-danger"
                        @click="endReservation(res)"
                      >
                        强制结束
                      </button>
                      <button
                        v-if="res.status === 'pending'"
                        class="btn btn-danger"
                        @click="endReservation(res)"
                      >
                        取消预约
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section v-else-if="currentTab === 'users'" key="users">
            <h2 class="section-title">学生用户管理</h2>
            <div class="table-container">
              <div class="search-row">
                <input
                  type="text"
                  v-model="searchQuery"
                  class="search-input"
                  placeholder="搜索学号或姓名..."
                />
              </div>
              <table>
                <thead>
                  <tr>
                    <th>学号</th>
                    <th>姓名</th>
                    <th>学院</th>
                    <th>信誉积分</th>
                    <th>账号状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="user in filteredUsers" :key="user.id">
                    <td>{{ user.id }}</td>
                    <td>{{ user.name }}</td>
                    <td>{{ user.department }}</td>
                    <td>{{ user.credit }}</td>
                    <td>
                      <span class="status" :class="user.status === 'normal' ? 'active' : 'error'">
                        {{ user.status === 'normal' ? '正常' : '已限制' }}
                      </span>
                    </td>
                    <td>
                      <button
                        v-if="user.status === 'normal'"
                        class="btn btn-danger"
                        @click="toggleUserStatus(user)"
                      >
                        限制权限
                      </button>
                      <button
                        v-else
                        class="btn btn-success"
                        @click="toggleUserStatus(user)"
                      >
                        恢复权限
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-if="filteredUsers.length === 0" class="empty-tip">没有找到匹配的用户</div>
            </div>
          </section>

          <section v-else-if="currentTab === 'seats'" key="seats">
            <h2 class="section-title">阅览室座位平面图 (A区)</h2>
            <div class="legend-row">
              <span class="status active">空闲 (点击设为维修)</span>
              <span class="status error">使用中</span>
              <span class="status maintenance">维修中 (点击恢复)</span>
            </div>
            <div class="table-container">
              <div class="seat-grid">
                <div
                  v-for="seat in seats"
                  :key="seat.id"
                  class="seat"
                  :class="seat.status"
                  @click="toggleSeatStatus(seat)"
                >
                  {{ seat.label }}
                </div>
              </div>
            </div>
          </section>

          <section v-else-if="currentTab === 'statistics'" key="statistics">
            <h2 class="section-title">使用数据统计分析</h2>
            <div class="dashboard-cards">
              <div class="table-container chart-panel">
                <h3>各学院使用频次排行</h3>
                <ul class="stat-list">
                  <li v-for="(stat, index) in departmentStats" :key="index">
                    <strong>{{ index + 1 }}.</strong> {{ stat.name }} ({{ stat.percentage }}%)
                  </li>
                </ul>
              </div>
              <div class="table-container chart-panel">
                <h3>高峰时段分布</h3>
                <div class="chart-placeholder">[此处建议引入 ECharts/Chart.js 渲染图表]</div>
              </div>
            </div>
          </section>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'

const currentTab = ref('overview')

const tabs = [
  { id: 'overview', name: '数据总览', icon: '📊' },
  { id: 'reservations', name: '预约管理', icon: '📅' },
  { id: 'users', name: '用户管理', icon: '👥' },
  { id: 'seats', name: '座位管理', icon: '🪑' },
  { id: 'statistics', name: '学生统计', icon: '📈' }
]

const overviewData = reactive({
  total: { label: '总座位数', count: 500, color: '#2c3e50' },
  free: { label: '空闲座位', count: 128, color: '#2ecc71' },
  today: { label: '今日预约人次', count: 842, color: '#2c3e50' },
  violations: { label: '违规记录', count: 12, color: '#e74c3c' }
})

const reservations = ref([
  {
    id: 'RES-001',
    studentId: '202300101',
    seatNo: 'A区-012',
    time: '08:00 - 12:00',
    status: 'using',
    statusText: '使用中',
    statusClass: 'active'
  },
  {
    id: 'RES-002',
    studentId: '202300105',
    seatNo: 'B区-045',
    time: '14:00 - 18:00',
    status: 'pending',
    statusText: '待签到',
    statusClass: 'pending'
  }
])

const endReservation = (res) => {
  const index = reservations.value.indexOf(res)
  if (index > -1) {
    reservations.value.splice(index, 1)
  }
}

const searchQuery = ref('')

const users = ref([
  { id: '202300101', name: '张三', department: '计算机学院', credit: 100, status: 'normal' },
  { id: '202300256', name: '李四', department: '经济管理学院', credit: 60, status: 'restricted' },
  { id: '202300302', name: '王五', department: '法学院', credit: 95, status: 'normal' }
])

const filteredUsers = computed(() => {
  if (!searchQuery.value) {
    return users.value
  }
  const lowerCaseQuery = searchQuery.value.toLowerCase()
  return users.value.filter(
    (user) => user.id.includes(lowerCaseQuery) || user.name.includes(lowerCaseQuery)
  )
})

const toggleUserStatus = (user) => {
  user.status = user.status === 'normal' ? 'restricted' : 'normal'
}

const seats = ref(
  Array.from({ length: 12 }, (_, i) => {
    const id = `A${String(i + 1).padStart(2, '0')}`
    let status = 'available'
    if (['A02', 'A03', 'A08', 'A09'].includes(id)) {
      status = 'occupied'
    }
    if (id === 'A05') {
      status = 'maintenance'
    }
    return { id, label: id, status }
  })
)

const toggleSeatStatus = (seat) => {
  if (seat.status === 'occupied') {
    return
  }
  seat.status = seat.status === 'available' ? 'maintenance' : 'available'
}

const departmentStats = ref([
  { name: '计算机学院', percentage: 45 },
  { name: '法学院', percentage: 25 },
  { name: '经济管理学院', percentage: 15 },
  { name: '其他', percentage: 15 }
])
</script>

<style scoped>
.aaa-page {
  --primary-color: #2c3e50;
  --secondary-color: #34495e;
  --accent-color: #3498db;
  --bg-color: #f4f7f6;
  --text-color: #333;
  --card-bg: #fff;
  --success: #2ecc71;
  --danger: #e74c3c;
  --warning: #f1c40f;

  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', sans-serif;
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.aaa-page *,
.aaa-page *::before,
.aaa-page *::after {
  box-sizing: border-box;
}

.sidebar {
  width: 250px;
  background-color: var(--primary-color);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  background-color: #1a252f;
  font-size: 1.2rem;
  font-weight: 700;
}

.nav-menu {
  list-style: none;
  flex: 1;
  margin-top: 20px;
  padding: 0;
}

.nav-item {
  padding: 15px 20px;
  cursor: pointer;
  transition: background 0.3s;
  border-left: 4px solid transparent;
}

.nav-item:hover,
.nav-item.active {
  background-color: var(--secondary-color);
  border-left-color: var(--accent-color);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.top-header {
  background-color: var(--card-bg);
  padding: 15px 30px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-area {
  padding: 30px;
  overflow-y: auto;
  flex: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.dashboard-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background: var(--card-bg);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.card h3 {
  color: #7f8c8d;
  font-size: 1rem;
  margin-bottom: 10px;
}

.card .number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--primary-color);
}

.table-container {
  background: var(--card-bg);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

th,
td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #f8f9fa;
  color: #333;
}

tr:hover {
  background-color: #f1f1f1;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #fff;
  font-size: 0.9rem;
  transition: opacity 0.2s;
}

.btn:hover {
  opacity: 0.8;
}

.btn-danger {
  background-color: var(--danger);
}

.btn-success {
  background-color: var(--success);
}

.status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.85rem;
  color: #fff;
}

.status.active {
  background-color: var(--success);
}

.status.pending {
  background-color: var(--warning);
  color: #333;
}

.status.error {
  background-color: var(--danger);
}

.status.maintenance {
  background-color: #95a5a6;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.seat {
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.1s;
}

.seat:active {
  transform: scale(0.95);
}

.seat.available {
  background-color: var(--success);
}

.seat.occupied {
  background-color: var(--danger);
  cursor: not-allowed;
}

.seat.maintenance {
  background-color: #95a5a6;
}

.section-title {
  margin-bottom: 20px;
  color: var(--primary-color);
}

.search-input {
  padding: 8px 12px;
  width: 250px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-right: 10px;
  outline: none;
}

.search-input:focus {
  border-color: var(--accent-color);
}

.search-row {
  margin-bottom: 15px;
  display: flex;
}

.legend-row {
  margin-bottom: 15px;
  gap: 10px;
  display: flex;
  flex-wrap: wrap;
}

.chart-panel {
  flex: 1;
}

.chart-placeholder {
  height: 150px;
  background: #ecf0f1;
  margin-top: 15px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7f8c8d;
}

.stat-list {
  margin-top: 15px;
  line-height: 2;
  list-style-type: none;
  padding: 0;
}

.announcement {
  margin-top: 10px;
  color: #7f8c8d;
}

.empty-tip {
  text-align: center;
  color: #999;
  margin-top: 20px;
}

@media (max-width: 900px) {
  .aaa-page {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .main-content {
    height: auto;
    min-height: calc(100vh - 220px);
  }

  .top-header {
    padding: 12px 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .content-area {
    padding: 16px;
  }

  .search-input {
    width: 100%;
    margin-right: 0;
  }
}
</style>
