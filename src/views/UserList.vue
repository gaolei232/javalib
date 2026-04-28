<template>
  <div class="user-list">
    <h1>用户管理</h1>
    <div class="add-user">
      <button @click="showAddForm = true">添加用户</button>
    </div>
    <div v-if="showAddForm" class="user-form">
      <h2>添加用户</h2>
      <input v-model="newUser.name" placeholder="姓名" />
      <input v-model="newUser.email" placeholder="邮箱" />
      <input v-model="newUser.password" placeholder="密码" type="password" />
      <div class="form-actions">
        <button @click="addUser">保存</button>
        <button @click="showAddForm = false">取消</button>
      </div>
    </div>
    <table class="users-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>姓名</th>
          <th>邮箱</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.name }}</td>
          <td>{{ user.email }}</td>
          <td>
            <router-link :to="`/users/${user.id}`">
              <button>查看</button>
            </router-link>
            <button @click="editUser(user)">编辑</button>
            <button @click="deleteUser(user.id)" class="delete">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as userApi from '../api/userApi'

const router = useRouter()
const users = ref([])
const showAddForm = ref(false)
const newUser = ref({ name: '', email: '', password: '' })

onMounted(() => {
  fetchUsers()
})

const fetchUsers = async () => {
  try {
    const response = await userApi.getAllUsers()
    users.value = response
  } catch (error) {
    console.error('Failed to fetch users:', error)
  }
}

const addUser = async () => {
  try {
    await userApi.createUser(newUser.value)
    newUser.value = { name: '', email: '', password: '' }
    showAddForm.value = false
    fetchUsers()
  } catch (error) {
    console.error('Failed to add user:', error)
  }
}

const editUser = (user) => {
  // 这里可以实现编辑功能
  console.log('Edit user:', user)
}

const deleteUser = async (id) => {
  try {
    await userApi.deleteUser(id)
    fetchUsers()
  } catch (error) {
    console.error('Failed to delete user:', error)
  }
}
</script>

<style scoped>
.user-list {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.add-user {
  margin-bottom: 20px;
  text-align: right;
}

.user-form {
  background: #f5f5f5;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
}

.user-form input {
  display: block;
  width: 100%;
  padding: 10px;
  margin-bottom: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
  display: block;
  overflow-x: auto;
  white-space: nowrap;
}

.users-table th,
.users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.users-table th {
  background-color: #f2f2f2;
}

.users-table td button {
  margin-right: 5px;
}

.delete {
  background-color: #ff4757;
  color: white;
}

.delete:hover {
  border-color: #ff4757;
  background-color: #ff3742;
}

@media (max-width: 640px) {
  .user-list {
    padding: 1rem;
  }

  .add-user {
    text-align: left;
  }

  .add-user button,
  .form-actions button {
    width: 100%;
  }

  .form-actions {
    flex-direction: column;
  }

  .users-table td button {
    display: block;
    width: 100%;
    margin: 0 0 8px;
  }
}
</style>
