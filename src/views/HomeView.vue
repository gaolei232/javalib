<template>
  <!--
    首页 — 极简编辑 × 温暖学术
    作用：系统 Landing Page，呈现品牌形象并引导用户进入预约或注册
  -->
  <div class="landing">
    <header class="landing-header">
      <router-link to="/" class="brand">
        <span class="brand-mark">LS</span>
        <span class="brand-name">座位预约系统</span>
      </router-link>
      <nav class="landing-nav">
        <router-link to="/login" class="btn-primary">登录</router-link>
        <router-link to="/register" class="btn-secondary">注册</router-link>
      </nav>
    </header>

    <main class="hero">
      <p class="hero-eyebrow">Spring Boot + Vue 3 · WebSocket 实时同步</p>
      <h1 class="hero-title">安静地找到一个<br />属于你的座位</h1>
      <p class="hero-desc">
        考研图书馆座位预约系统 — 智能分配、实时状态、多楼栋覆盖。<br />像翻开一本书一样自然地预约。
      </p>
      <div class="hero-actions">
        <router-link to="/login" class="btn-primary btn-lg">开始预约</router-link>
        <router-link to="/register" class="btn-secondary btn-lg">创建账号</router-link>
      </div>

      <div class="hero-stats">
        <div class="stat">
          <span class="stat-value">40+</span>
          <span class="stat-label">实时可视座位</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat">
          <span class="stat-value">3</span>
          <span class="stat-label">楼栋覆盖</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat">
          <span class="stat-value">&lt;1s</span>
          <span class="stat-label">WebSocket 同步延迟</span>
        </div>
      </div>
    </main>

    <section class="features">
      <div class="feature-card">
        <div class="feature-icon icon-time">S</div>
        <h3>灵活时段预约</h3>
        <p>08:00 – 22:00，30 分钟粒度，自由选择开始与结束时间。</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon icon-realtime">R</div>
        <h3>实时状态同步</h3>
        <p>WebSocket 推送座位变化，无需手动刷新页面。</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon icon-admin">A</div>
        <h3>多角色权限</h3>
        <p>学生用户预约座位，管理员全局可视化管理与统计分析。</p>
      </div>
    </section>

    <footer class="landing-footer">
      <span>考研图书馆座位预约系统</span>
      <router-link to="/aaa" class="footer-link">基础页面</router-link>
    </footer>
  </div>
</template>

<script setup>
// 首页无额外逻辑 — 路由链接直接跳转
</script>

<style scoped>
.landing {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--paper-white) 0%, var(--paper-warm) 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem 2rem 3rem;
}

/* ── 导航 ── */
.landing-header {
  width: 100%;
  max-width: 1100px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-body);
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--amber-gradient);
  display: grid;
  place-items: center;
  color: #fff;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1.1rem;
}

.brand-name {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.05rem;
  color: var(--text-body);
}

.landing-nav {
  display: flex;
  gap: 0.75rem;
}

/* ── Hero ── */
.hero {
  max-width: 720px;
  text-align: center;
  margin-bottom: 4rem;
}

.hero-eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--amber);
  margin-bottom: 1.5rem;
}

.hero-title {
  font-family: var(--font-display);
  font-size: clamp(2.2rem, 3.5vw, 3.2rem);
  font-weight: 800;
  color: var(--text-strong);
  line-height: 1.15;
  margin-bottom: 1.5rem;
}

.hero-desc {
  font-size: 1.05rem;
  color: var(--text-soft);
  max-width: 520px;
  margin: 0 auto 2.5rem;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 3rem;
}

.btn-lg {
  padding: 0.85rem 2rem;
  font-size: 1rem;
}

/* ── Stats ── */
.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.stat {
  text-align: center;
}

.stat-value {
  display: block;
  font-family: var(--font-display);
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-strong);
}

.stat-label {
  display: block;
  font-size: 0.82rem;
  color: var(--text-soft);
  margin-top: 0.2rem;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--border-warm);
}

/* ── Features ── */
.features {
  max-width: 960px;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.feature-card {
  background: var(--card-pure);
  border-radius: var(--radius-xl);
  padding: 2rem;
  border: 1px solid var(--border-warm);
  transition: transform var(--ease-smooth);
}

.feature-card:hover {
  transform: translateY(-3px);
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1.1rem;
  margin-bottom: 1rem;
}

.icon-time {
  background: var(--honey);
  color: var(--amber);
}

.icon-realtime {
  background: var(--green-soft);
  color: var(--green-text);
}

.icon-admin {
  background: var(--red-soft);
  color: var(--red-text);
}

.feature-card h3 {
  font-size: 1.05rem;
  margin-bottom: 0.5rem;
  color: var(--text-body);
}

.feature-card p {
  font-size: 0.9rem;
  color: var(--text-soft);
  line-height: 1.6;
}

/* ── Footer ── */
.landing-footer {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  font-size: 0.82rem;
  color: var(--text-subtle);
}

.footer-link {
  color: var(--text-soft);
  font-weight: 500;
}

/* ── 响应式 ── */
@media (max-width: 768px) {
  .landing {
    padding: 1.5rem 1.5rem 2.5rem;
  }

  .landing-header {
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 2.5rem;
  }

  .hero-stats {
    gap: 1.5rem;
  }
}

@media (max-width: 480px) {
  .landing {
    padding: 1.25rem 1rem 2rem;
  }

  .hero-title {
    font-size: 1.8rem;
  }

  .hero-actions {
    flex-direction: column;
    width: 100%;
  }

  .btn-lg {
    width: 100%;
    text-align: center;
  }

  .hero-stats {
    flex-direction: column;
    gap: 0.8rem;
  }

  .stat-divider {
    width: 32px;
    height: 1px;
  }
}
</style>
