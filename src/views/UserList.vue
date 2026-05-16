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
  max-width: 960px;
  margin: 0 auto;
  padding: 2rem;
}

.user-list h1 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-strong);
  margin-bottom: 1.5rem;
}

.add-user {
  margin-bottom: 1.25rem;
  text-align: right;
}

.add-user button {
  padding: 0.65rem 1.6rem;
  border-radius: var(--radius-pill);
  background: var(--amber-gradient);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform var(--ease-smooth);
}
.add-user button:hover { transform: translateY(-2px); }

.user-form {
  background: var(--card-pure);
  padding: 1.5rem;
  margin-bottom: 1.25rem;
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-warm);
}
.user-form h2 { font-size: 1rem; color: var(--text-body); margin-bottom: 1rem; }

.user-form input {
  display: block;
  width: 100%;
  padding: 0.7rem 0.9rem;
  margin-bottom: 0.75rem;
  border: 1.5px solid var(--border-warm);
  border-radius: var(--radius-md);
  font-family: var(--font-body);
  font-size: 0.94rem;
  background: var(--paper-white);
  color: var(--text-body);
  transition: border-color var(--ease-smooth);
}
.user-form input:focus { outline: none; border-color: var(--amber); box-shadow: 0 0 0 3px var(--amber-soft); }

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 0.75rem;
}
.form-actions button {
  padding: 0.55rem 1.4rem;
  border-radius: var(--radius-pill);
  font-weight: 600;
  font-size: 0.88rem;
  cursor: pointer;
  transition: all var(--ease-smooth);
}
.form-actions button:first-child {
  background: var(--amber-gradient);
  color: #fff;
}
.form-actions button:first-child:hover { transform: translateY(-1px); }
.form-actions button:last-child {
  background: var(--card-pure);
  color: var(--text-soft);
  border: 1px solid var(--border-warm);
}
.form-actions button:last-child:hover { border-color: var(--amber); color: var(--amber); }

.users-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1.25rem;
  background: var(--card-pure);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-warm);
  overflow: hidden;
}
.users-table thead { display: table-header-group; }
.users-table tbody { display: table-row-group; }
.users-table tr { display: table-row; }
.users-table th,
.users-table td {
  display: table-cell;
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border-warm);
}
.users-table th {
  background: var(--paper-warm);
  color: var(--text-subtle);
  font-size: 0.8rem;
  font-weight: 600;
}
.users-table td { color: var(--text-body); font-size: 0.9rem; }
.users-table tr:last-child td { border-bottom: none; }
.users-table td button {
  margin-right: 5px;
  padding: 0.35rem 0.8rem;
  border-radius: var(--radius-pill);
  font-weight: 600;
  font-size: 0.82rem;
  cursor: pointer;
  border: 1px solid var(--border-warm);
  background: var(--card-pure);
  color: var(--text-soft);
  transition: all var(--ease-smooth);
}
.users-table td button:hover { border-color: var(--amber); color: var(--amber); }
.users-table td .delete {
  background: var(--red-soft);
  color: var(--red-text);
  border-color: rgba(153,27,27,0.15);
}
.users-table td .delete:hover { background: #ef4444; color: #fff; }

/* ── Responsive ── */
@media (max-width: 768px) {
  .user-list { padding: 1.5rem 1rem; }
  .user-list h1 { font-size: 1.3rem; }
}

@media (max-width: 640px) {
  .user-list { padding: 1rem 0.75rem; }

  .add-user { text-align: left; }
  .add-user button,
  .form-actions button { width: 100%; }
  .form-actions { flex-direction: column; }

  .user-form { padding: 1rem; }

  .users-table { display: block; overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .users-table table { min-width: 500px; }
  .users-table td button { display: block; width: 100%; margin: 0 0 6px; }
  .users-table td button:last-child { margin-bottom: 0; }
}

@media (max-width: 420px) {
  .user-list { padding: 0.75rem 0.5rem; }
  .user-list h1 { font-size: 1.15rem; }
  .user-form { padding: 0.85rem; border-radius: var(--radius-lg); }
}
</style>
