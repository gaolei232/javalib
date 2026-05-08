# 设计规格：诚信打卡系统 + 密码修改 + 预约统计

## 概述

在座位预约系统中新增三大功能模块：
1. **密码修改** — 用户可在账号信息页修改密码
2. **预约统计** — 账号信息页展示个人预约次数统计
3. **诚信打卡系统** — 预约时段内打卡签到，计算诚信指数，低于阈值禁止预约

---

## 一、数据模型变更

### 1.1 SeatBooking 新增字段

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `checked_in` | Boolean | false | 用户是否在预约时段内打卡 |

### 1.2 User 新增字段

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `total_bookings` | int | 0 | 累计已结束的预约次数 |
| `valid_bookings` | int | 0 | 累计有效预约（已打卡）次数 |

### 1.3 新增 SystemConfig 实体

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 |
| `config_key` | varchar(100) | 配置键，唯一 |
| `config_value` | varchar(255) | 配置值 |

初始数据：`integrity_threshold` = `0.6`

---

## 二、诚信指数计算规则

- **总预约次数** = 已打卡数 + 已过期且未打卡数（仅统计已结束的预约）
- **有效预约** = checkedIn = true 的数量
- **无效预约** = 状态为 EXPIRED 且 checkedIn = false 的数量
- **诚信指数** = 有效预约 / 总预约次数 × 100%
- 总预约次数为 0 时，诚信指数 = 100%

---

## 三、后端 API

### 3.1 密码修改（AuthController）

`POST /api/auth/change-password`

请求体：`{ oldPassword, newPassword }`
认证：从 Authorization header 提取 token 识别用户
逻辑：验证旧密码 → BCrypt 加密新密码 → 更新用户
返回：`{ success, message }`

### 3.2 打卡签到（SeatController）

`POST /api/seats/checkin/{bookingId}`

逻辑：
- 验证 booking 存在且状态为 BOOKED
- 验证当前时间在 [startTime, endTime] 区间内
- 设置 checkedIn = true
- User.totalBookings +1, User.validBookings +1
- 返回 `{ success, message }`

### 3.3 用户诚信指数

`GET /api/users/{id}/integrity`

返回：`{ totalBookings, validBookings, invalidBookings, integrityScore }`

### 3.4 系统配置（新增 SystemConfigController）

`GET /api/system/config?key=integrity_threshold`
返回：`{ key, value }`

`PUT /api/system/config`
请求体：`{ key, value }`
返回：`{ success, message }`

### 3.5 预约拦截逻辑

在 `SeatService.bookSeatWithDuration()` 中新增：
1. 计算用户诚信指数
2. 读取 integrity_threshold 配置
3. 若诚信指数 < 阈值，返回 false（Controller 层返回 `{ success: false, message: "诚信指数过低（XX%），无法预约" }`）

### 3.6 过期预约处理增强

在 `resetExpiredBookings` 中，当预约被标记为 EXPIRED 时：
- 若 checkedIn = false，User.totalBookings +1（无效预约+1）

---

## 四、前端变更

### 4.1 SeatBookingView.vue — 账号信息区（profile section）

新增统计子卡片（位于基本信息下方）：

```
┌─ 预约统计 ──────────────────────┐
│ 总预约    有效预约    无效预约    │
│   12        7          5        │
│                                 │
│ 诚信指数                        │
│ ████████████░░░░░ 58%          │
└─────────────────────────────────┘
```

新增密码修改子卡片：

```
┌─ 修改密码 ──────────────────────┐
│ 旧密码    [________]            │
│ 新密码    [________]            │
│ 确认密码  [________]            │
│ [修改密码]                      │
└─────────────────────────────────┘
```

### 4.2 SeatBookingView.vue — 我的预约区（bookings section）

每条 BOOKED 状态的预约记录，根据当前时间判断：
- 当前时间 < startTime：显示「待开始」
- 当前时间在 [startTime, endTime] 内且未打卡：显示 **「打卡签到」按钮**
- 已打卡：显示「已打卡」绿色标签
- 状态为 EXPIRED 且未打卡：显示「无效预约」红色标签
- 状态为 EXPIRED 且已打卡：显示「已完成」绿色标签

### 4.3 AdminView.vue — 用户详情弹窗

在 `viewUserDetail` 的 detailModal.items 中新增：
- `{ label: '诚信指数', value: 'XX%' }`
- 数据从 `GET /api/users/{id}/integrity` 获取

### 4.4 AdminView.vue — 系统设置

在设置区新增卡片：

```
┌─ 预约规则设置 ──────────────────┐
│ 诚信指数阈值                    │
│ [0.6]  (0.0 ~ 1.0)            │
│ 低于此值的用户无法预约           │
└─────────────────────────────────┘
```

设置保存到后端 `PUT /api/system/config`。

### 4.5 预约拦截提示

预约失败时，后端返回诚信指数过低的消息，前端在通知中展示。

---

## 五、涉及文件清单

### 后端（Java）
- `entity/SeatBooking.java` — 新增 checkedIn 字段
- `entity/User.java` — 新增 totalBookings、validBookings 字段
- `entity/SystemConfig.java` — 新建实体
- `repository/SystemConfigRepository.java` — 新建
- `service/AuthService.java` — 新增 changePassword 方法签名
- `service/impl/AuthServiceImpl.java` — 实现 changePassword
- `controller/AuthController.java` — 新增 /change-password 端点
- `service/SeatService.java` — 新增 checkin、integrity 查询、预约拦截逻辑
- `controller/SeatController.java` — 新增 /checkin 端点
- `controller/SystemConfigController.java` — 新建
- `controller/UserController.java` — 新增 /integrity 端点

### 前端（Vue/JS）
- `src/views/SeatBookingView.vue` — 账号信息区改造、我的预约区改造
- `src/api/authApi.js` — 新增 changePassword
- `src/api/seatApi.js` — 新增 checkin
- `src/api/userApi.js` — 新增 getIntegrity
- `src/api/systemApi.js` — 新建，getConfig/updateConfig
- `src/views/AdminView.vue` — 用户详情+诚信指数、系统设置+阈值
