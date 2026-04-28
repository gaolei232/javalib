<template>
  <div class="auth-page">
    <header class="auth-header">
      <router-link to="/" class="brand">
        <span class="brand-mark"></span>
        <span class="brand-name">图书馆座位预约系统</span>
      </router-link>
      <div class="header-actions">
        <router-link to="/login" class="header-link">登录</router-link>
        <router-link to="/" class="header-link ghost">返回首页</router-link>
      </div>
    </header>

    <main class="auth-main">
      <section class="auth-card">
        <div class="auth-title">
          <p class="auth-eyebrow">开始使用</p>
          <h1>创建新账户</h1>
          <p>填写资料后即可进入座位预约系统。</p>
        </div>

        <form @submit.prevent="handleRegister" class="auth-form">
          <label class="form-group">
            <span>用户名</span>
            <input
              id="name"
              v-model="formData.name"
              type="text"
              placeholder="请输入用户名"
              required
              :class="{ error: errors.name }"
            />
            <small v-if="errors.name" class="error-message">{{ errors.name }}</small>
          </label>

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
              placeholder="至少 6 位"
              required
              :class="{ error: errors.password }"
            />
            <small v-if="errors.password" class="error-message">{{ errors.password }}</small>
          </label>

          <label class="form-group">
            <span>确认密码</span>
            <input
              id="confirmPassword"
              v-model="formData.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              required
              :class="{ error: errors.confirmPassword }"
            />
            <small v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</small>
          </label>

          <div class="form-options">
            <label class="remember-me">
              <input v-model="formData.agreeTerms" type="checkbox" required />
              <span>我已阅读并同意服务条款与隐私政策</span>
            </label>
            <button class="link-button" type="button" @click="openPolicy">查看条款</button>
          </div>

          <button class="submit-button" type="submit" :disabled="loading">
            {{ loading ? '注册中...' : '创建账户' }}
          </button>
        </form>

        <div class="auth-footer">
          <p>已有账号？<router-link to="/login">立即登录</router-link></p>
          <button class="admin-switch" type="button" @click="prefillAdmin">管理员入口</button>
        </div>

        <div v-if="errorMessage" class="alert error">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="alert success">
          {{ successMessage }}
        </div>
      </section>

      <aside class="auth-side">
        <div class="side-card">
          <h2>注册后你将获得</h2>
          <p>清晰的预约流程与个人预约记录，快速找到满意座位。</p>
          <ul>
            <li>多时段预约策略</li>
            <li>个人预约历史</li>
            <li>管理员与学生统计</li>
          </ul>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formData = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const errors = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const validateForm = () => {
  errors.name = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''
  let isValid = true

  if (!formData.name) {
    errors.name = '请输入用户名'
    isValid = false
  } else if (formData.name.length < 2) {
    errors.name = '用户名至少为 2 个字符'
    isValid = false
  }

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
    errors.password = '密码长度至少 6 位'
    isValid = false
  }

  if (!formData.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  if (!formData.agreeTerms) {
    errorMessage.value = '请先阅读并同意服务条款与隐私政策'
    isValid = false
  }

  return isValid
}

const handleRegister = async () => {
  errorMessage.value = ''
  if (!validateForm()) return

  loading.value = true
  successMessage.value = ''

  try {
    await authStore.register({
      name: formData.name,
      email: formData.email,
      password: formData.password
    })
    successMessage.value = '注册成功，正在跳转到登录页...'
    setTimeout(() => router.push('/login'), 1200)
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const prefillAdmin = () => {
  localStorage.setItem('login_prefill_email', 'admin@example.com')
  localStorage.setItem('login_admin_mode', '1')
  router.push('/login')
}

const openPolicy = () => {
  errorMessage.value = '条款页面待接入，目前可先勾选完成测试。'
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
  gap: 1rem;
  font-size: 0.9rem;
  color: black;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  color: var(--text-muted);
  font-weight: 500;
  cursor: pointer;
}

.remember-me span {
  line-height: 1.5;
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
  white-space: nowrap;
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

.alert.success {
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
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
