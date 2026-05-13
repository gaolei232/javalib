# 图书馆座位预约系统 — 重设计方案

## 设计方向

**极简编辑 × 温暖学术（A+D 融合）**

暖纸白底色、琥珀棕强调色、精致字体层级、大面积留白。既有杂志排版的高级感，又贴合图书馆的人文氛围。

## 设计系统

| 设计元素 | 规格 |
|----------|------|
| 背景色 | `#faf8f5` 暖纸白 → `#f5f0e8` 渐变 |
| 卡片色 | `#ffffff` 纯白 |
| 强调色 | `#b45309` 琥珀棕，渐变 `#b45309 → #d97706` |
| 主文字 | `#4a3728` 深棕 |
| 辅助文字 | `#8b7355` 暖灰 |
| 标题字体 | Manrope（800/700 weight） |
| 正文字体 | Inter（400/500/600 weight） |
| 圆角 | 999px（按钮）、20px（卡片）、12px（小元素） |
| 分隔 | 细边框 `#e8e2d8` 替代大阴影 |

## 文件说明

| 文件 | 页面名称 | 路由 | 说明 |
|------|---------|------|------|
| `design-system.css` | 全局样式 | - | CSS 变量和基础样式，替代原 `style.css` |
| `HomeView.vue` | 首页 | `/` | 极简 Hero + 三功能卡片 + 数据指标 |
| `LoginView.vue` | 登录 | `/login` | 单栏居中 + 温暖表单 + 管理员入口 |
| `RegisterView.vue` | 注册 | `/register` | 单栏居中 + 四字段表单 + 条款确认 |
| `SeatBookingView.vue` | 座位预约 | `/seat-booking` | 筛选面板 + 暖色座位地图 + 右侧详情 |
| `AdminView.vue` | 管理控制台 | `/admin` | 侧栏导航 + 数据卡片 + 表格管理 |

## 功能保留说明

所有页面的以下功能完整保留：

- **API 接口**：authApi / seatApi / studentStatsApi / userApi 调用与返回值完全不变
- **路由守卫**：router.beforeEach 中的认证与角色判定逻辑不变
- **WebSocket**：SeatBookingView 中的 wsManager 连接与事件处理不变
- **Pinia Store**：auth.js 中的状态管理完全不变
- **localStorage/sessionStorage**：token 与用户信息存取逻辑不变
- **表单验证**：邮箱格式、密码长度、条款确认等验证逻辑不变
- **交互逻辑**：座位点击、时间选择、预约提交、取消预约等流程不变

## 使用方式

将所有 `.vue` 文件复制到 `src/views/` 目录，将 `design-system.css` 复制到 `src/` 并修改 `main.js` 引用即可切换为新设计方案。
