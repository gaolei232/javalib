<template>
  <div class="auth-page">
    <header class="auth-header">
      <router-link to="/" class="brand">
        <span class="brand-mark"></span>
        <span class="brand-name">图书馆座位预约系统</span>
      </router-link>
      <div class="header-actions">
        <router-link to="/register" class="header-link">创建账号</router-link>
        <router-link to="/" class="header-link ghost">返回首页</router-link>
      </div>
    </header>

    <main class="auth-main">
      <section class="auth-card">
        <div class="auth-title">
          <p class="auth-eyebrow">欢迎回来</p>
          <h1>{{ isAdminMode ? '管理员登录' : '用户登录' }}</h1>
          <p>使用邮箱与密码访问座位预约系统。</p>
        </div>

        <form @submit.prevent="handleLogin" class="auth-form">
          <label class="form-group">
            <span>邮箱</span>
            <input
              id="email"
              v-model="formData.email"
              type="email"
              placeholder="name@example.com"
              required
              :class="{ error: errors.email }"
            />
            <small v-if="errors.email" class="error-message">{{ errors.email }}</small>
          </label>

          <label class="form-group">
            <span>密码</span>
            <input
              id="password"
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              required
              :class="{ error: errors.password }"
            />
            <small v-if="errors.password" class="error-message">{{ errors.password }}</small>
          </label>

            <div class="form-options">
              <label class="remember-me">
                <input v-model="formData.rememberMe" type="checkbox" />
                记住我
              </label>
              <button class="link-button" type="button">忘记密码？</button>
            </div>

          <button class="submit-button" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="auth-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
          <button class="admin-switch" type="button" @click="goAdminLogin">管理员入口</button>
        </div>

        <div v-if="errorMessage" class="alert error">
          {{ errorMessage }}
        </div>
      </section>

      <aside class="auth-side">
        <div class="side-card">
          <h2>今日座位动态</h2>
          <p>实时掌握座位占用情况，支持多时段预约与快速管理。</p>
          <ul>
            <li>高效预约流程</li>
            <li>实时 WebSocket 更新</li>
            <li>管理员控制台</li>
          </ul>
        </div>
      </aside>
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
  padding: 2.5rem 6vw 3rem;
  background: radial-gradient(circle at top right, rgba(99, 102, 241, 0.12), transparent 55%),
    var(--surface-base);
}

.auth-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  color: var(--text-primary);
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: var(--brand-gradient);
}

.brand-name {
  font-weight: 700;
  font-size: 1.1rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.header-link {
  padding: 0.5rem 1.3rem;
  border-radius: 999px;
  background: var(--brand-gradient);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 10px 20px rgba(99, 102, 241, 0.2);
}

.header-link.ghost {
  background: #fff;
  color: var(--brand-accent);
  box-shadow: var(--shadow-border);
}

.auth-main {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2.5rem;
  align-items: stretch;
}

.auth-card {
  background: var(--surface-card);
  border-radius: var(--radius-lg);
  padding: 2.5rem;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.auth-title p {
  margin-top: 0.4rem;
  color: var(--text-muted);
}

.auth-eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--brand-accent);
}

.auth-form {
  display: grid;
  gap: 1.4rem;
}

.form-group {
  display: grid;
  gap: 0.6rem;
  color: var(--text-muted);
  font-weight: 600;
}

.form-group input:not([type='checkbox']) {
  width: 100%;
  min-height: 48px;
  padding: 0.85rem 1rem;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: #fff;
  box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.04);
}

.form-group input:not([type='checkbox'])::placeholder {
  color: #94a3b8;
}

.form-group input:not([type='checkbox']):focus {
  border-color: rgba(99, 102, 241, 0.65);
  box-shadow:
    0 0 0 4px rgba(99, 102, 241, 0.12),
    inset 0 1px 2px rgba(15, 23, 42, 0.04);
}

.form-group input.error:not([type='checkbox']) {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15);
}

.error-message {
  color: #ef4444;
  font-size: 0.75rem;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: black;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-muted);
  font-weight: 500;
  cursor: pointer;
}

.remember-me input[type='checkbox'] {
  appearance: none;
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  min-height: 18px;
  margin: 0;
  padding: 0;
  flex: 0 0 18px;
  border-radius: 6px;
  border: 1.5px solid rgba(99, 102, 241, 0.45);
  background: #fff;
  box-shadow: none;
  display: inline-grid;
  place-items: center;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.remember-me input[type='checkbox']::after {
  content: '';
  width: 8px;
  height: 4px;
  border-left: 2px solid #fff;
  border-bottom: 2px solid #fff;
  transform: rotate(-45deg) scale(0);
  transition: transform 0.15s ease;
  margin-top: -1px;
}

.remember-me input[type='checkbox']:checked {
  background: var(--brand-accent);
  border-color: var(--brand-accent);
}

.remember-me input[type='checkbox']:checked::after {
  transform: rotate(-45deg) scale(1);
}

.remember-me input[type='checkbox']:focus {
  outline: none;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.14);
}

.link-button {
  background: transparent;
  box-shadow: none;
  color: var(--brand-accent);
  padding: 0;
  min-height: auto;
  border-radius: 0;
}

.submit-button {
  width: 100%;
}

.auth-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.admin-switch {
  background: #fff;
  color: var(--brand-accent);
  box-shadow: var(--shadow-border);
}

.alert {
  padding: 0.9rem 1rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

.alert.error {
  background: rgba(239, 68, 68, 0.1);
  color: #b91c1c;
}

.auth-side {
  display: flex;
  align-items: center;
}

.side-card {
  background: var(--surface-card);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow-card);
  display: grid;
  gap: 1rem;
}

.side-card ul {
  padding-left: 1.2rem;
  margin: 0;
  color: var(--text-muted);
  display: grid;
  gap: 0.6rem;
}

@media (max-width: 768px) {
  .auth-header {
    flex-direction: column;
    gap: 1.5rem;
    align-items: stretch;
  }

  .auth-footer {
    flex-direction: column;
    gap: 0.8rem;
  }

  .header-actions {
    width: 100%;
  }

  .header-link {
    flex: 1;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .auth-page {
    padding: 1.25rem 1rem 2rem;
  }

  .auth-header {
    margin-bottom: 1.5rem;
  }

  .header-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }

  .auth-card,
  .side-card {
    padding: 1.25rem;
  }

  .form-options {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }

  .remember-me {
    justify-content: space-between;
  }

  .link-button,
  .admin-switch {
    width: 100%;
    text-align: center;
  }
}
</style>
