<template>
  <div class="landing">
    <header class="landing-header">
      <div class="brand">
        <div class="brand-mark"></div>
        <div>
          <p class="brand-name">Library Seat</p>
          <p class="brand-tagline">智慧座位预约平台</p>
        </div>
      </div>
      <nav class="landing-nav">
        <router-link to="/login" class="nav-link">登录</router-link>
        <router-link to="/register" class="nav-link ghost">注册</router-link>
      </nav>
    </header>

    <main class="landing-main">
      <section class="hero">
        <div class="hero-content">
          <p class="hero-eyebrow">Spring Boot + Vue 3</p>
          <h2 class="hero-title">考研图书馆座位预约系统</h2>
          <p class="hero-subtitle">
            通过智能预约系统，实现座位管理的高效、安全与便捷。
          </p>
          <div class="hero-actions">
            <router-link to="/login" class="primary-action">立即开始</router-link>
            <router-link to="/register" class="secondary-action">创建账号</router-link>
            <router-link to="/aaa" class="secondary-action preview-action">AAA 页面</router-link>
          </div>
          <div class="hero-metrics">
            <div>
              <h3>40+</h3>
              <p>座位实时可视</p>
            </div>
            <div>
              <h3>WebSocket</h3>
              <p>秒级状态同步</p>
            </div>
            <div>
              <h3>Admin</h3>
              <p>可视化管理</p>
            </div>
          </div>
        </div>
        <div class="hero-card">
          <div class="card-panel">
            <div class="card-header">
              <span class="card-dot"></span>
              <span class="card-dot"></span>
              <span class="card-dot"></span>
            </div>
            <div class="card-content">
              <h2>系统亮点</h2>
              <ul>
                <li>多角色权限与安全认证</li>
                <li>座位统计与预约分析</li>
                <li>管理端全局可视化</li>
                <li>预约时长灵活配置</li>
              </ul>
              <div class="card-footer">
                <p>快速部署，稳定运行</p>
                <router-link to="/login">进入系统</router-link>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.landing {
  min-height: 100vh;
  background: radial-gradient(circle at top, rgba(99, 102, 241, 0.12), transparent 55%),
    var(--surface-base);
  padding: 2.5rem 6vw 4rem;
}

.landing-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 3.5rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--brand-gradient);
  box-shadow: var(--shadow-card);
}

.brand-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.brand-tagline {
  font-size: 0.9rem;
  color: var(--text-subtle);
}

.landing-nav {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  padding: 0.55rem 1.4rem;
  border-radius: 999px;
  background: var(--brand-gradient);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 10px 20px rgba(99, 102, 241, 0.2);
  transition: transform 0.2s ease;
}

.nav-link:hover {
  transform: translateY(-2px);
}

.nav-link.ghost {
  background: #fff;
  color: var(--brand-accent);
  box-shadow: var(--shadow-border);
}

.landing-main {
  display: flex;
  justify-content: center;
}

.hero {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 3rem;
  align-items: center;
  width: min(1100px, 100%);
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.hero-eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--brand-accent);
}

.hero-title {
  font-size: clamp(2.2rem, 3vw, 3rem);
  line-height: 1.1;
}

.hero-subtitle {
  font-size: 1.05rem;
  color: var(--text-muted);
  max-width: 520px;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.primary-action,
.secondary-action {
  padding: 0.75rem 1.6rem;
  border-radius: 999px;
  font-weight: 600;
  box-shadow: var(--shadow-card);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.primary-action {
  background: var(--brand-gradient);
  color: #fff;
}

.secondary-action {
  background: #fff;
  color: var(--brand-accent);
  box-shadow: var(--shadow-border);
}

.primary-action:hover,
.secondary-action:hover {
  transform: translateY(-2px);
}

.preview-action {
  background: rgba(255, 255, 255, 0.75);
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1.5rem;
}

.hero-metrics h3 {
  font-size: 1.3rem;
}

.hero-card {
  display: flex;
  justify-content: center;
}

.card-panel {
  background: var(--surface-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  overflow: hidden;
  width: min(420px, 100%);
}

.card-header {
  display: flex;
  gap: 0.4rem;
  padding: 1rem 1.5rem;
  background: var(--surface-elevated);
}

.card-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.6);
}

.card-content {
  padding: 2rem 2.5rem 2.5rem;
}

.card-content h2 {
  margin-bottom: 1rem;
  font-size: 1.4rem;
}

.card-content ul {
  margin: 0;
  padding-left: 1.2rem;
  color: var(--text-muted);
  display: grid;
  gap: 0.7rem;
}

.card-footer {
  margin-top: 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-footer a {
  font-weight: 600;
}

@media (max-width: 768px) {
  .landing {
    padding: 2rem 1.5rem 3rem;
  }

  .landing-header {
    flex-direction: column;
    gap: 1.5rem;
    align-items: stretch;
  }

  .landing-nav {
    width: 100%;
    justify-content: stretch;
  }

  .nav-link {
    flex: 1;
    text-align: center;
  }

  .card-content {
    padding: 1.5rem;
  }

  .card-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
}

@media (max-width: 480px) {
  .landing {
    padding: 1.25rem 1rem 2rem;
  }

  .hero {
    gap: 1.5rem;
  }

  .hero-title {
    font-size: 1.95rem;
  }

  .hero-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .primary-action,
  .secondary-action {
    width: 100%;
    text-align: center;
  }
}
</style>
