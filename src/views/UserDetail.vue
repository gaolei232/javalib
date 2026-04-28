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
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.user-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  text-align: left;
}

.user-info p {
  margin: 10px 0;
}

.actions {
  margin-top: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
}

@media (max-width: 640px) {
  .user-detail {
    padding: 1rem;
  }

  .user-info {
    padding: 16px;
  }

  .actions button {
    width: 100%;
  }
}
</style>
