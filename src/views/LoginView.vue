<template>
  <!--
    登录页 — 极简编辑 × 温暖学术
    作用：用户与管理员的登录入口，含记住我、管理员快捷入口
    接口设计：完整照搬原版 authApi.login、authStore 逻辑
  -->
  <div class="auth-page">
    <main class="auth-container">
      <div class="auth-brand">
        <router-link to="/" class="brand-mark">LS</router-link>
        <h1>欢迎回来</h1>
        <p>使用邮箱与密码登录座位预约系统</p>
      </div>

      <form class="auth-card" @submit.prevent="handleLogin">
        <label class="field">
          <span class="field-label">邮箱</span>
          <input
            v-model="formData.email"
            type="email"
            placeholder="name@example.com"
            required
            :class="{ 'input-error': errors.email }"
          />
          <small v-if="errors.email" class="error-text">{{ errors.email }}</small>
        </label>

        <label class="field">
          <span class="field-label">密码</span>
          <input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            required
            :class="{ 'input-error': errors.password }"
          />
          <small v-if="errors.password" class="error-text">{{ errors.password }}</small>
        </label>

        <div class="form-row">
          <label class="checkbox-label">
            <input v-model="formData.rememberMe" type="checkbox" />
            <span>记住我</span>
          </label>
          <button type="button" class="btn-ghost">忘记密码？</button>
        </div>

        <button type="submit" class="btn-primary btn-full" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <div class="form-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
        </div>

        <div class="admin-entry">
          <button type="button" class="btn-ghost" @click="goAdminLogin">管理员入口</button>
        </div>

        <div v-if="errorMessage" class="alert alert-error">
          {{ errorMessage }}
        </div>
      </form>

      <router-link to="/" class="back-link">返回首页</router-link>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formData = reactive({ email: '', password: '', rememberMe: false })
const errors = reactive({ email: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')
const isAdminMode = ref(false)

onMounted(() => {
  const prefill = localStorage.getItem('login_prefill_email')
  if (prefill) {
    formData.email = prefill
    localStorage.removeItem('login_prefill_email')
  }
  if (localStorage.getItem('login_admin_mode') === '1') {
    isAdminMode.value = true
    localStorage.removeItem('login_admin_mode')
  }
})

const validateForm = () => {
  errors.email = ''
  errors.password = ''
  let isValid = true

  if (!formData.email) {
    errors.email = '请输入邮箱'
    isValid = false
  } else if (!/^\S+@\S+\.\S+$/.test(formData.email)) {
    errors.email = '请输入有效的邮箱地址'
    isValid = false
  }

  if (!formData.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (formData.password.length < 6) {
    errors.password = '密码长度至少为6位'
    isValid = false
  }

  return isValid
}

const handleLogin = async () => {
  if (!validateForm()) return
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await authStore.login(formData, formData.rememberMe)

    if (isAdminMode.value && response?.user?.role !== 'ADMIN') {
      await authStore.logout()
      errorMessage.value = '该账号不是管理员，请使用管理员账号登录'
      return
    }

    router.push(response?.user?.role === 'ADMIN' ? '/admin' : '/seat-booking')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || '登录失败，请检查邮箱和密码'
  } finally {
    loading.value = false
  }
}

const goAdminLogin = () => {
  isAdminMode.value = true
  formData.email = 'admin@example.com'
  formData.password = ''
  errorMessage.value = '请输入管理员密码后登录'
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--paper-white) 0%, var(--paper-warm) 100%);
  display: grid;
  place-items: center;
  padding: 2rem;
}

.auth-container {
  width: min(420px, 100%);
}

/* ── Brand ── */
.auth-brand {
  text-align: center;
  margin-bottom: 2rem;
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--amber-gradient);
  display: grid;
  place-items: center;
  color: #fff;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1.3rem;
  margin: 0 auto 1rem;
}

.auth-brand h1 {
  font-size: 1.3rem;
  font-weight: 800;
  color: var(--text-strong);
}

.auth-brand p {
  font-size: 0.9rem;
  color: var(--text-soft);
  margin-top: 0.4rem;
}

/* ── Card ── */
.auth-card {
  background: var(--card-pure);
  border-radius: var(--radius-xl);
  padding: 2rem;
  border: 1px solid var(--border-warm);
  display: grid;
  gap: 1.2rem;
}

/* ── Fields ── */
.field {
  display: grid;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-muted);
}

.field input:not([type="checkbox"]) {
  width: 100%;
  min-height: 48px;
}

.input-error {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12) !important;
}

.error-text {
  color: #ef4444;
  font-size: 0.75rem;
}

/* ── Row ── */
.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.85rem;
  color: var(--text-soft);
  cursor: pointer;
}

.btn-full {
  width: 100%;
}

/* ── Footer ── */
.form-footer {
  text-align: center;
  font-size: 0.88rem;
  color: var(--text-soft);
}

.admin-entry {
  text-align: center;
  border-top: 1px solid var(--border-warm);
  padding-top: 0.75rem;
}

/* ── Alert ── */
.alert {
  padding: 0.8rem 1rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 0.88rem;
}

.alert-error {
  background: var(--red-soft);
  color: var(--red-text);
}

/* ── Back ── */
.back-link {
  display: block;
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.85rem;
  color: var(--text-subtle);
}

@media (max-width: 768px) {
  .auth-page { padding: 1.75rem 1.5rem; }
  .auth-brand { margin-bottom: 1.5rem; }
  .auth-brand h1 { font-size: 1.2rem; }
}

@media (max-width: 480px) {
  .auth-page {
    padding: 1.25rem 1rem;
  }

  .auth-card {
    padding: 1.5rem;
  }

  .auth-brand h1 { font-size: 1.1rem; }
  .auth-brand p { font-size: 0.82rem; }
  .brand-mark { width: 44px; height: 44px; font-size: 1.1rem; }

  .form-row { flex-direction: column; gap: 0.6rem; align-items: stretch; text-align: center; }
}
</style>
