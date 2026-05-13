# Test Evidence

## 构建验证结果

| 项目 | 结果 |
| --- | --- |
| 前端构建工具 | Vite |
| 构建耗时 | 1.12秒 |
| 构建产物 JS | 213.01 KB (gzip 77.39 KB) |
| 构建产物 CSS | 44.62 KB (gzip 7.86 KB) |
| 入口 HTML | 0.47 KB |
| 后端打包工具 | Maven (JDK 17) |
| JAR 文件 | springboot-vue3-system-1.0.0.jar |
| JAR 体积 | 62 MB |
| node_modules | 39 MB |

## 接口测试结果

### 认证接口
- POST /api/auth/register — 需要完整字段（name/email/password），返回token
- POST /api/auth/login — 返回token + user对象
- GET /api/auth/me — 需Authorization header，返回当前用户信息

### 座位接口
- GET /api/seats — 支持date/buildingCode/floorNo等参数筛选
- GET /api/seats/statistics — 返回 total/booked/available/used
- POST /api/seats/initialize — 初始化400个座位（已有11个历史座位=411总）

### 预约接口
- POST /api/seats/{seatId}/book?userId=X&date=YYYY-MM-DD&startTime=HH:MM&endTime=HH:MM — 预约成功
- 冲突检测：同一座位同一日期重叠时段（09:00-11:00和09:30-10:30）→ 失败
- 不同座位同一日期不同时段 → 成功

### 取消接口
- POST /api/seats/{seatId}/cancel?bookingId=X — 取消成功，状态回滚

### 诚信指数
- GET /api/users/{id}/integrity — 返回 totalBookings/validBookings/integrityScore

## 测试数据示例

- 座位编码：B1-F1-A-1（建筑B1-楼层F1-排A-列1）
- 座位属性：nearWindow(列5), hasOutlet(奇数列), quietZone(行A/B)
- 用户ID: 3 (admin@example.com)
- 令牌格式：user-3-02798712-b827-4302-918b-114b106c5b80
