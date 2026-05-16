<template>
  <div class="user-detail">
    <h1>用户详情</h1>
    <div v-if="user" class="user-info">
      <p><strong>ID:</strong> {{ user.id }}</p>
      <p><strong>姓名:</strong> {{ user.name }}</p>
      <p><strong>邮箱:</strong> {{ user.email }}</p>
      <div class="actions">
        <router-link to="/users">
          <button>返回列表</button>
        </router-link>
      </div>
    </div>
    <div v-else class="loading">
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import * as userApi from '../api/userApi'

const route = useRoute()
const user = ref(null)

onMounted(() => {
  fetchUserDetail()
})

const fetchUserDetail = async () => {
  try {
    const userId = route.params.id
    const response = await userApi.getUserById(userId)
    user.value = response
  } catch (error) {
    console.error('Failed to fetch user details:', error)
  }
}
</script>

<style scoped>
.user-detail {
  width: 100%;
  max-width: 720px;
  margin: 0 auto;
  padding: 2rem;
}

.user-detail h1 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-strong);
  margin-bottom: 1.5rem;
}

.user-info {
  background: var(--card-pure);
  padding: 1.5rem;
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-warm);
}

.user-info p {
  margin: 0.75rem 0;
  padding: 0.65rem 0.85rem;
  border-radius: var(--radius-md);
  background: var(--paper-warm);
  color: var(--text-body);
  font-size: 0.94rem;
}

.user-info p strong {
  display: inline-block;
  min-width: 60px;
  color: var(--text-subtle);
}

.actions {
  margin-top: 1.25rem;
}

.actions button {
  padding: 0.65rem 1.6rem;
  border-radius: var(--radius-pill);
  background: var(--amber-gradient);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform var(--ease-smooth);
  border: none;
}
.actions button:hover { transform: translateY(-2px); }

.loading {
  text-align: center;
  padding: 3rem;
  color: var(--text-subtle);
  font-size: 0.95rem;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .user-detail { padding: 1.5rem 1rem; }
  .user-detail h1 { font-size: 1.3rem; }
}

@media (max-width: 640px) {
  .user-detail { padding: 1rem 0.75rem; }
  .user-info { padding: 1rem; }
  .user-info p { padding: 0.55rem 0.75rem; font-size: 0.9rem; }
  .actions button { width: 100%; }
}

@media (max-width: 420px) {
  .user-detail { padding: 0.75rem 0.5rem; }
  .user-detail h1 { font-size: 1.15rem; }
  .user-info { padding: 0.85rem; border-radius: var(--radius-lg); }
}
</style>
