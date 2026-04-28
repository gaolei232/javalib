# 变更记录

## 2026-04-03 多楼栋多楼层座位图扩展

### 前端改动
- 重构 `src/views/SeatBookingView.vue`，修复原有模板标签损坏问题并确保可编译。
- 新增楼栋/楼层下拉切换：支持 `B1/B2/B3` 三栋楼与 `1/2/3` 三层。
- 座位加载改为按 `buildingCode + floorNo + date + userId` 查询。
- 座位点击、详情、预约、取消等逻辑改为使用后端真实 `seatId`（如 `B1-F2-A-3`），避免跨楼层冲突。
- 增强 WebSocket 更新过滤，仅更新当前所选楼栋/楼层的数据。
- 更新样式适配：`date-selector` 下新增 `select` 的统一输入样式。

### 前端 API 改动
- 更新 `src/api/seatApi.js` 的 `getAllSeats` 参数：新增 `buildingCode`、`floorNo`，并在请求参数中透传。

### 后端改动
- `src/main/java/com/example/entity/Seat.java`
  - 新增字段：`buildingCode`、`floorNo`。
- `src/main/java/com/example/repository/SeatRepository.java`
  - 新增方法：`existsBySeatId`、`findByBuildingCodeAndFloorNoOrderByRowAscColAsc`。
- `src/main/java/com/example/service/SeatViewCacheService.java`
  - 缓存键增加楼栋/楼层维度，避免跨楼层缓存串数据。
- `src/main/java/com/example/service/SeatService.java`
  - 新增楼栋/楼层规范化逻辑。
  - `getAllSeats` 支持按楼栋/楼层过滤（不传时保留全量查询）。
  - `getSeatsByDateAndTimeSlot`/`getSeatsByDate` 支持楼栋/楼层过滤。
  - 初始化座位改为 3 栋楼 × 3 层 × 5 行 × 8 列，统一 seatId 规则：`{building}-F{floor}-{row}-{col}`。
  - 创建/更新座位时补齐楼栋楼层默认值。
- `src/main/java/com/example/controller/SeatController.java`
  - `/api/seats` 新增可选参数：`buildingCode`、`floorNo`，并传递到服务层。
  - 初始化接口返回 `totalSeats` 改为动态统计。

### 数据库改动（MySQL test）
- 表 `seats` 新增字段：
  - `building_code VARCHAR(20) NOT NULL DEFAULT 'B1'`
  - `floor_no INT NOT NULL DEFAULT 1`
- 新增索引：`idx_building_floor (building_code, floor_no)`。
- 旧数据升级：
  - `seats.seat_id` 从 `A-1` 迁移为 `B1-F1-A-1` 形式。
  - `seat_bookings.seat_id` 同步迁移为新格式。
- 补齐全量座位数据后总计：`360` 个座位（3 栋 × 3 层 × 每层 40）。

### 验证
- 前端构建通过：`npm run build`。
- 后端编译通过：`mvn -DskipTests compile`。
- 数据库校验通过：每栋每层 40 个座位，总数 360。

## 2026-04-03 楼层选择区 UI 重设计（ui-ux-pro-max）

### 使用技能
- 按用户要求使用 `$ui-ux-pro-max` 设计规范，对楼栋/楼层选择区进行视觉重构。

### 具体改动
- 更新 `src/views/SeatBookingView.vue` 的筛选区域模板：
  - 将原先纵向的普通表单改为 `selector-card` 卡片式结构。
  - 新增“空间筛选”标题与“当前楼栋/楼层”状态胶囊（`selector-current`）。
  - 楼栋、楼层、日期三项改为统一栅格布局（`selector-grid`），增强对齐与层次。
- 新增并优化样式：
  - 新增 `selector-card / selector-head / selector-title / selector-current / selector-grid / selector-field` 等样式类。
  - 增加轻拟物卡片质感（渐变背景、柔和阴影、内高光），与现有整体风格保持一致。
  - 下拉框补齐箭头样式与焦点反馈，统一交互体验。
- 响应式增强：
  - `<=900px` 下筛选区转为两列；`<=600px` 下自动降为单列。
  - 移动端标题与状态胶囊纵向排列，保证可读性。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-03 座位规模与特征筛选扩展

### 需求实现
- 用户端每层座位展示从 `5x8` 调整为 `5x5`（仅展示 `A-E` 行与 `1-5` 列）。
- 用户端座位详情新增三项信息展示：
  - 是否靠窗（`nearWindow`）
  - 是否有插座（`hasOutlet`）
  - 是否安静区（`quietZone`）
- 用户端新增按上述三项条件筛选（全部/是/否）能力。
- 管理员端座位管理新增楼栋、楼层切换，并按楼层展示座位。
- 管理员端座位详情同步展示靠窗、插座、安静区信息。

### 后端与数据模型
- `Seat` 实体新增字段：`nearWindow`、`hasOutlet`、`quietZone`。
- `SeatService`：
  - 初始化列数 `MAX_COL` 从 `8` 调整为 `5`（新初始化数据按 5 列生成）。
  - 初始化座位时自动生成座位特征：
    - `nearWindow`: `col == 5`
    - `hasOutlet`: `col` 为奇数
    - `quietZone`: 行 `A/B`
  - 创建/更新座位时对三项布尔特征做默认值与持久化处理。
- `SeatStatusWebSocketHandler` 的推送负载补充字段：
  - `buildingCode`、`floorNo`
  - `nearWindow`、`hasOutlet`、`quietZone`

### 数据库更新（test）
- `seats` 表新增列：
  - `near_window TINYINT(1) NOT NULL DEFAULT 0`
  - `has_outlet TINYINT(1) NOT NULL DEFAULT 0`
  - `quiet_zone TINYINT(1) NOT NULL DEFAULT 0`
- 既有数据回填规则：
  - `near_window = (col = 5)`
  - `has_outlet = (col % 2 = 1)`
  - `quiet_zone = (seat_row in ('A','B'))`

### 前端实现
- `SeatBookingView.vue`：
  - 座位列改为 `1..5`。
  - 新增特征筛选面板（靠窗/插座/安静区）。
  - 筛选结果实时作用于座位状态展示与点击交互。
  - 座位详情新增特征标签显示。
- `AdminView.vue`：
  - 座位管理工具栏新增楼栋/楼层下拉。
  - `loadSeats` 改为携带 `buildingCode + floorNo` 请求。
  - 座位网格改为 5 列展示，并仅展示 `col <= 5`。
  - 座位详情新增三项特征显示。
  - 同步修复了该文件中若干历史乱码导致的模板/脚本语法错误，确保可编译。

### 验证
- 前端构建通过：`npm run build`。
- 后端编译通过：`mvn -DskipTests compile`。
- 数据库校验通过：每栋每层 `col<=5` 的座位数量均为 `25`。

## 2026-04-03 前端切换刷新与管理员页修复

### 本次目标
- 修复用户端座位页在切换楼栋/楼层时的实时刷新一致性。
- 修复用户端筛选区样式不统一问题。
- 管理员座位管理页增强楼层切换交互。
- 修复管理员界面文字乱码。

### 具体改动
- `src/views/SeatBookingView.vue`
  - 为 `loadSeats` 增加请求令牌（`latestSeatsRequestToken`），避免快速切换楼栋/楼层时旧请求覆盖新结果。
  - 重构座位特征筛选区为卡片化结构（新增 `filter-card`、`filter-grid` 等），并统一输入框/下拉框视觉风格与焦点态。
  - 移动端适配：筛选卡头部与筛选栅格在小屏下自动纵向排列。

- `src/views/AdminView.vue`
  - 全面清理模板与脚本中的乱码文本，统一为可读中文。
  - 座位管理区新增“楼层快捷切换”按钮组（与楼层下拉联动）。
  - 为管理员 `loadSeats` 增加请求令牌（`latestSeatListRequestToken`），避免切换楼栋/楼层时数据串页。
  - 修复部分提示文案与错误日志文字异常（取消预约、重置座位、保存用户、删除用户等）。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-03 楼层切换后状态与时间条不同步修复

### 问题
- 用户端座位预约页切换楼层后，部分“我已约”颜色与下方时间进度条会出现未及时刷新、显示旧上下文数据的问题。

### 修复
- `src/views/SeatBookingView.vue`
  - 在 `resetSeatSelection()` 中增加 `latestSeatRequestToken` 失效处理，切换楼层/楼栋时主动作废旧的座位当日预约异步请求，避免旧请求回写导致时间进度条显示旧座位数据。
  - 在 `getTimelineClass()` 中增加未选座位兜底：`!selectedSeatId` 时统一返回 `available`，防止时间条误显示历史占用。
  - 在 `getSeatBookingsForDay()` 中将“我已约”判定限定为 `status === BOOKED`，避免历史状态（如已取消/已过期）干扰座位颜色。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-03 跨楼层座位状态串层修复（再次修正）

### 用户复现问题
- 预约 `B1-F1-A1` 后，切换到 `B2-F1` 时，`A1` 仍错误显示“我已约”，时间进度条也被旧楼层状态污染。

### 根因
- 实际接口返回中混入了非当前楼栋/楼层的座位数据，前端按 `row/col` 取座位时会误命中其他楼层同位置座位。

### 修复
- `src/views/SeatBookingView.vue`
  - 新增 `parseSeatLocationFromSeatId()`：从 `seatId` 解析楼栋楼层（如 `B2-F1-*`）。
  - 新增 `isSeatInSelectedLocation()`：优先用 `buildingCode/floorNo`，否则回退解析 `seatId`，并排除无法判定楼层的旧格式 `seatId`（如 `A-1`）。
  - `loadSeats()` 中对接口返回结果做前端二次过滤，仅保留当前 `selectedBuildingCode + selectedFloorNo` 的座位，再渲染。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-08 座位属性随机化（测试数据）

### 需求
- 为各座位随机添加属性，用于数据测试。

### 代码改动
- `src/main/java/com/example/service/SeatService.java`
  - 新增 `randomizeSeatAttributes()`：对全部座位随机赋值
    - `nearWindow`
    - `hasOutlet`
    - `quietZone`
  - 随机化后会刷新座位视图缓存区间（今天起 30 天）。

- `src/main/java/com/example/controller/SeatController.java`
  - 新增接口：`POST /api/seats/randomize-attributes`
  - 返回字段：`success`、`message`、`updatedSeats`。
  - 同时修复了该文件中几处历史乱码导致的未闭合字符串问题，恢复可编译。

### 已执行的数据操作
- 已直接在 `test.seats` 执行随机赋值：
  - `near_window = IF(RAND()<0.5,1,0)`
  - `has_outlet = IF(RAND()<0.5,1,0)`
  - `quiet_zone = IF(RAND()<0.5,1,0)`
- 执行结果统计：
  - `total = 400`
  - `near_window_yes = 183`
  - `has_outlet_yes = 208`
  - `quiet_zone_yes = 219`

### 验证
- 后端编译通过：`mvn -DskipTests compile`。

## 2026-04-08 注册页与管理员页问题修复

### 修复项
1. 注册界面点击“管理员入口”无响应
- 重写 `src/views/RegisterView.vue`，将 `prefillAdmin()` 改为：
  - 写入 `login_prefill_email=admin@example.com`
  - 写入 `login_admin_mode=1`
  - 立即跳转 `router.push('/login')`

2. 注册界面隐私政策勾选框样式不统一
- 在 `RegisterView.vue` 新增自定义勾选样式 `agreement-check`，统一尺寸、边框、选中态与焦点态视觉。

3. 管理员界面学生统计页中文乱码
- 在 `src/views/AdminView.vue` 修复学生统计表头乱码：`瀹屾垚` -> `已完成`。

4. 座位管理页一栋一层重复座位显示
- 在 `AdminView.vue` 新增：
  - `parseSeatLocationFromSeatId()`
  - `isSeatInSelectedLocation()`
  - `dedupeSeatsByPosition()`
- `loadSeats()` 增加二次处理：
  - 先按当前楼栋/楼层过滤
  - 再按 `row-col` 去重（优先保留标准 seatId：`B*-F*-*`）

### 其他
- `RegisterView.vue` 同步清理了整页历史乱码文案，恢复正常中文显示。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-08 注册页勾选区视觉统一优化

### 问题
- 注册页“我已阅读并同意服务条款与隐私政策”勾选区与页面整体卡片/输入框风格不一致。

### 修复
- `src/views/RegisterView.vue`
  - 重做 `.agreement`：增加卡片化背景、圆角、边框、阴影、hover 与 focus-within 态。
  - 优化 `.agreement-check`：与主题一致的渐变底、选中阴影、过渡动效。
  - 增加 `.agreement span` 文本行高，提升可读性与版面协调度。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-08 注册页勾选区改为 form-options 风格

### 需求
- 将“我已阅读并同意服务条款与隐私政策”区域改为与登录页 `form-options` 一致的样式结构。

### 修改
- `src/views/RegisterView.vue`
  - 模板改为：`<div class="form-options"><label class="remember-me">...</label><button class="link-button">...</button></div>`
  - 样式改为登录页同款：
    - `.form-options`
    - `.remember-me`
    - `.remember-me input[type='checkbox']` 及选中/焦点态
    - `.link-button`
  - 移动端适配同步采用 `form-options` 折叠布局。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-08 注册页协议勾选区与登录页风格再次对齐

### 需求
- 将注册页“我已阅读并同意服务条款与隐私政策”勾选区进一步调整为与登录页 `form-options` 区块一致的视觉与排版风格。

### 修改
- `src/views/RegisterView.vue`
  - 重写注册页文件，清理页面中的历史乱码文案，保留原有注册逻辑与管理员入口逻辑。
  - 协议区继续使用登录页同款结构：`form-options + remember-me + link-button`。
  - 为协议文案增加 `span` 包裹，优化长文本换行与勾选框对齐效果。
  - 为 `form-options` 增加 `gap`，为 `remember-me` 增加 `flex: 1`，让左侧协议区和右侧“查看条款”按钮在桌面端与移动端都更协调。
  - `link-button` 增加 `white-space: nowrap`，避免按钮文案换行导致布局松散。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-15 新增 AAA 页面并接入首页跳转

### 需求
- 将桌面文件 `AAA.vue` 加入项目，并在主页增加按钮跳转到该页面。

### 修改
- 新增文件：`src/views/AAA.vue`
  - 将 `C:\Users\16929\OneDrive\Desktop\AAA.vue` 复制到项目视图目录。
- 修改路由：`src/router/index.js`
  - 新增 `AAAView` 引入。
  - 新增路由：`/aaa`（name: `aaa`，component: `AAAView`）。
- 修改主页：`src/views/HomeView.vue`
  - 在 hero 按钮区新增入口：`<router-link to="/aaa" class="secondary-action preview-action">AAA 页面</router-link>`。
  - 新增 `.preview-action` 样式，保持与现有按钮体系一致。
  - 修复主页中历史遗留的损坏闭合标签（如 `?/router-link`、`?/h2`、`?/p`、`?/li`），恢复 Vue 模板可编译状态。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-15 主页中文乱码修复

### 需求
- 修复首页(HomeView)可见文案乱码问题。

### 修改
- src/views/HomeView.vue
  - 将首页导航、主标题、副标题、指标文案、卡片文案全部替换为正常 UTF-8 中文文本。
  - 保留原有页面布局与样式结构，仅修复文案显示。

### 验证
- 前端构建通过：
pm run build。

## 2026-04-15 管理员样式污染其他页面修复

### 问题
- `AdminView.vue` 使用未加作用域的 `<style>`，且包含 `input`、`select`、`label`、`*` 和 `:root` 等通用选择器，导致管理员页样式覆盖到其他页面，新页面输入框被干扰。

### 修改
- `src/views/AdminView.vue`
  - 将 `<style>` 改为 `<style scoped>`，限制管理员样式仅在当前页面生效。
  - 将样式变量挂载点从 `:root` 改为 `.admin-shell`，避免全局 CSS 变量被管理员页重写。

### 验证
- 前端构建通过：`npm run build`。

## 2026-04-15 AAA 页面按提供代码重写并完成可运行验证

### 需求
- 将用户提供的完整页面代码写入 `AAA.vue`，并验证页面可在当前 Vue3 项目中跑通。

### 修改
- `src/views/AAA.vue`
  - 将原先错误的整页 HTML（含 `<!DOCTYPE html>`、`<script src=...>`、`createApp(...).mount('#app')`）重写为 Vue 单文件组件结构：`<template> + <script setup> + <style scoped>`。
  - 保留用户提供页面的核心布局与交互逻辑：侧边导航、总览、预约管理、用户管理、座位管理、统计分析。
  - 将数据与方法改写为组合式 API：`ref`、`reactive`、`computed`。
  - 样式改为 `scoped`，并将颜色变量收敛到 `.aaa-page` 作用域，避免污染其他页面样式。
  - 增加基础移动端适配，确保小屏可正常显示。

### 验证
- 前端构建通过：`npm run build`。
