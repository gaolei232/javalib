# 项目功能说明

## 项目概述
本项目是一个基于 Spring Boot + Vue 3 的前后端分离系统，提供用户认证、座位预约、管理员控制台、学生统计与用户管理等功能，并通过 WebSocket 实现座位状态的实时广播。

## 后端功能（Spring Boot）

### 认证与授权
- 登录：通过邮箱和密码进行登录，返回用户信息与令牌。
- 注册：用户注册并自动生成令牌。
- 登出：前端清理认证信息。
- 获取当前用户：支持通过 Bearer Token 获取当前登录用户信息。
- 刷新令牌：根据旧令牌生成新令牌。
- 忘记/重置密码：提供找回密码与重置密码接口（当前为简化实现）。

### 座位管理与预约
- 座位列表：查询全部座位；支持按日期、时段、用户、起始时间过滤，并动态标记座位状态（可用/已预约/未来预约）。
- 座位详情：支持按 ID 或 seatId 查询座位。
- 状态筛选：按状态获取座位，或获取可用座位列表。
- 座位统计：统计可用/已预约/已使用/总座位数。
- 座位初始化：生成 5 行（A-E）* 8 列（1-8）的默认座位。
- 创建/更新/删除座位：支持后台管理操作。
- 预约座位：支持按日期与时长预约（时长范围 1-6 小时）。
- 取消预约：按日期与时段取消预约。
- 标记已使用：将已预约座位标记为“已使用”。
- 重置座位：恢复为可用状态。
- 预约记录：查询全部预约、仅已预约记录、以及用户个人预约记录。

### 学生统计
- 学生统计 CRUD：支持按 ID、学生 ID、姓名进行查询，支持新增、更新、删除。
- 预约统计：记录预约次数、完成次数、取消次数、总时长和最后预约日期。
- 系统统计：统计学生总数、总预约数、完成/取消数、平均时长。
- 数据重建：当统计数据为空时，从预约记录重建。

### 用户管理
- 用户 CRUD：支持创建、更新、删除与查询用户。
- 登录校验：校验密码并支持加密存储。
- 注册：邮箱唯一校验，默认角色为普通用户。

### WebSocket
- 座位状态广播：座位状态变更时向所有连接广播最新座位状态。
- 预约通知：提供广播预约消息的入口。

### 基础配置
- 数据库：MySQL 连接配置，JPA 自动更新表结构。
- 安全配置：启用 CORS，关闭 CSRF，默认放行全部请求。
- 服务端口：默认 8080。

## 前端功能（Vue 3）

### 认证与路由
- 登录：支持普通用户与管理员登录；记住我模式；输入校验。
- 注册：用户注册、表单校验、注册成功跳转。
- 路由权限：未登录强制跳转登录；管理员与普通用户分别进入不同页面。

### 首页
- 项目介绍展示、登录/注册入口与基础功能说明。

### 座位预约
- 座位布局展示：按行列展示座位，支持选择座位并查看状态。
- 座位状态区分：可用、已预约（他人/本人）、未来预约、已使用。
- 预约操作：选择日期、时长后发起预约。
- 取消预约：支持取消自己的预约。
- 预约记录：展示当前用户的历史预约。
- WebSocket 实时更新：接收座位状态更新并刷新页面。

### 管理员控制台
- 仪表盘：统计总用户数、今日预约、总座位数、使用率、最近活动。
- 预约管理：按日期与状态筛选预约记录，支持查看与取消。
- 用户管理：查询、添加、编辑、删除用户。
- 座位管理：查看座位布局、初始化座位、刷新座位、重置座位状态。
- 学生统计：系统统计概览、学生统计列表、排行榜。
- 系统设置：系统名称、最大预约数、预约时间段、通知设置等（前端展示）。

### 用户管理页面
- 用户列表：展示用户列表，支持添加与删除。
- 用户详情：查看单个用户的基础信息。

## API 概览

### 认证接口
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/auth/refresh`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

### 座位接口
- `GET /api/seats`
- `GET /api/seats/bookings`
- `GET /api/seats/{id}`
- `GET /api/seats/seat-id/{seatId}`
- `GET /api/seats/status/{status}`
- `GET /api/seats/available`
- `GET /api/seats/row/{row}/col/{col}`
- `GET /api/seats/statistics`
- `POST /api/seats`
- `POST /api/seats/initialize`
- `PUT /api/seats/{id}`
- `POST /api/seats/{seatId}/book`
- `POST /api/seats/{seatId}/cancel`
- `POST /api/seats/{seatId}/mark-used`
- `POST /api/seats/{seatId}/reset`
- `GET /api/seats/user/{userId}/bookings`
- `DELETE /api/seats/{id}`

### 学生统计接口
- `GET /api/student-stats`
- `GET /api/student-stats/{id}`
- `GET /api/student-stats/student/{studentId}`
- `GET /api/student-stats/name/{name}`
- `GET /api/student-stats/top`
- `GET /api/student-stats/system`
- `GET /api/student-stats/total-students`
- `GET /api/student-stats/total-bookings`
- `POST /api/student-stats`
- `PUT /api/student-stats/{id}`
- `POST /api/student-stats/booking`
- `POST /api/student-stats/cancellation`
- `DELETE /api/student-stats/{id}`

### 用户接口
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

## 前端页面概览
- `/`：主页
- `/login`：登录
- `/register`：注册
- `/seat-booking`：座位预约
- `/admin`：管理员控制台
- `/users`：用户列表
- `/users/:id`：用户详情

## WebSocket 通道
- `ws://localhost:8080/ws/seat-status`：座位状态更新推送通道
