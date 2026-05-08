# 诚信打卡系统 + 密码修改 + 预约统计 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现预约时段打卡签到、诚信指数计算、密码修改、个人预约统计，管理员可设置诚信阈值并查看用户诚信指数。

**Architecture:** 后端新增 SystemConfig 实体管理配置，SeatBooking/User 实体新增诚信相关字段。SeatService 新增打卡、诚信查询、预约拦截逻辑。前端 SeatBookingView 账号信息区新增统计与密码修改，我的预约区新增打卡按钮，AdminView 新增诚信指数展示与阈值设置。

**Tech Stack:** Spring Boot 3 + JPA + Vue 3 + Axios

---

## File Structure

```
Backend (Java):
  Create:  entity/SystemConfig.java
  Create:  repository/SystemConfigRepository.java
  Create:  controller/SystemConfigController.java
  Modify:  entity/User.java — 新增 totalBookings, validBookings 字段
  Modify:  entity/SeatBooking.java — 新增 checkedIn 字段
  Modify:  service/AuthService.java — 新增 changePassword 签名
  Modify:  service/impl/AuthServiceImpl.java — 实现 changePassword
  Modify:  controller/AuthController.java — 新增 /change-password
  Modify:  service/SeatService.java — 新增 checkin, integrity, booking gate, expired handling
  Modify:  controller/SeatController.java — 新增 /checkin
  Modify:  repository/SeatBookingRepository.java — 新增 count 查询
  Modify:  controller/UserController.java — 新增 integrity 端点

Frontend (Vue/JS):
  Create:  src/api/systemApi.js
  Modify:  src/api/authApi.js — 新增 changePassword
  Modify:  src/api/seatApi.js — 新增 checkin
  Modify:  src/api/userApi.js — 新增 getIntegrity
  Modify:  src/views/SeatBookingView.vue — 账号信息 + 我的预约改造
  Modify:  src/views/AdminView.vue — 用户详情诚信 + 系统设置阈值
```

---

### Task 1: 创建 SystemConfig 实体与 Repository

**Files:**
- Create: `src/main/java/com/example/entity/SystemConfig.java`
- Create: `src/main/java/com/example/repository/SystemConfigRepository.java`

- [ ] **Step 1: 创建 SystemConfig 实体**

```java
package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 255)
    private String configValue;

    public SystemConfig() {}

    public SystemConfig(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
}
```

- [ ] **Step 2: 创建 SystemConfigRepository**

```java
package com.example.repository;

import com.example.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    SystemConfig findByConfigKey(String configKey);
}
```

- [ ] **Step 3: 创建初始化配置的 AdminUserInitializer 增强**

Read `src/main/java/com/example/config/AdminUserInitializer.java` first, then modify to also init system config:

```java
// 在 AdminUserInitializer 类中新增:
@Autowired
private SystemConfigRepository systemConfigRepository;

// 在 initAdminUser() 方法末尾添加:
if (systemConfigRepository.findByConfigKey("integrity_threshold") == null) {
    SystemConfig config = new SystemConfig("integrity_threshold", "0.6");
    systemConfigRepository.save(config);
}
```

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/entity/SystemConfig.java src/main/java/com/example/repository/SystemConfigRepository.java
git commit -m "feat: add SystemConfig entity and repository"
```

---

### Task 2: 更新 User 实体

**Files:**
- Modify: `src/main/java/com/example/entity/User.java`

- [ ] **Step 1: 新增 totalBookings 和 validBookings 字段**

在 User.java 中 existing fields 之后添加：

```java
@Column(name = "total_bookings", nullable = false)
private int totalBookings = 0;

@Column(name = "valid_bookings", nullable = false)
private int validBookings = 0;

public int getTotalBookings() { return totalBookings; }
public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }
public int getValidBookings() { return validBookings; }
public void setValidBookings(int validBookings) { this.validBookings = validBookings; }
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/entity/User.java
git commit -m "feat: add totalBookings and validBookings to User entity"
```

---

### Task 3: 更新 SeatBooking 实体

**Files:**
- Modify: `src/main/java/com/example/entity/SeatBooking.java`

- [ ] **Step 1: 新增 checkedIn 字段**

在 SeatBooking.java 中 existing fields 之后添加：

```java
@Column(name = "checked_in", nullable = false)
private Boolean checkedIn = false;

public Boolean getCheckedIn() { return checkedIn; }
public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/entity/SeatBooking.java
git commit -m "feat: add checkedIn field to SeatBooking entity"
```

---

### Task 4: 后端 — SeatBookingRepository 新增查询方法

**Files:**
- Modify: `src/main/java/com/example/repository/SeatBookingRepository.java`

- [ ] **Step 1: 添加统计查询方法**

在 SeatBookingRepository 接口中添加：

```java
@Query("SELECT COUNT(b) FROM SeatBooking b WHERE b.userId = :userId AND b.status IN ('BOOKED', 'EXPIRED')")
Long countTotalFinishedByUserId(@Param("userId") String userId);

@Query("SELECT COUNT(b) FROM SeatBooking b WHERE b.userId = :userId AND b.checkedIn = true")
Long countCheckedInByUserId(@Param("userId") String userId);

List<SeatBooking> findByUserIdOrderByCreatedAtDesc(String userId);
```

Also add the needed import at top:
```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/repository/SeatBookingRepository.java
git commit -m "feat: add booking count query methods"
```

---

### Task 5: 后端 — AuthService 新增 changePassword

**Files:**
- Modify: `src/main/java/com/example/service/AuthService.java`
- Modify: `src/main/java/com/example/service/impl/AuthServiceImpl.java`

- [ ] **Step 1: AuthService 接口新增方法签名**

```java
Map<String, Object> changePassword(String authorizationHeader, String oldPassword, String newPassword);
```

- [ ] **Step 2: AuthServiceImpl 实现 changePassword**

```java
@Override
public Map<String, Object> changePassword(String authorizationHeader, String oldPassword, String newPassword) {
    Map<String, Object> response = new HashMap<>();
    Long userId = extractUserIdFromAuthorizationHeader(authorizationHeader);

    if (userId == null) {
        response.put("success", false);
        response.put("message", "未登录或token无效");
        return response;
    }

    User user = userService.getUserById(userId);
    if (user == null) {
        response.put("success", false);
        response.put("message", "用户不存在");
        return response;
    }

    // AuthServiceImpl uses passwordEncoder indirectly via UserService.login
    // We need to inject PasswordEncoder here
    if (oldPassword == null || newPassword == null || newPassword.isBlank()) {
        response.put("success", false);
        response.put("message", "密码不能为空");
        return response;
    }

    // Verify old password
    User verified = userService.login(user.getEmail(), oldPassword);
    if (verified == null) {
        response.put("success", false);
        response.put("message", "原密码错误");
        return response;
    }

    // Update password via userService.updateUser
    User updateUser = new User();
    updateUser.setName(user.getName());
    updateUser.setEmail(user.getEmail());
    updateUser.setPassword(newPassword); // will be encoded in updateUser
    updateUser.setRole(user.getRole());
    userService.updateUser(userId, updateUser);

    response.put("success", true);
    response.put("message", "密码修改成功");
    return response;
}
```

Note: AuthServiceImpl needs `@Autowired private PasswordEncoder passwordEncoder;` but it already has access through userService. The UserServiceImpl.updateUser already encodes passwords, so just using it works. But for password verification, we use `userService.login()`.

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/service/AuthService.java src/main/java/com/example/service/impl/AuthServiceImpl.java
git commit -m "feat: implement changePassword in AuthService"
```

---

### Task 6: 后端 — AuthController 新增 change-password 端点

**Files:**
- Modify: `src/main/java/com/example/controller/AuthController.java`

- [ ] **Step 1: 添加端点**

```java
@PostMapping("/change-password")
public ResponseEntity<Map<String, Object>> changePassword(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestBody Map<String, String> request) {
    String oldPassword = request.get("oldPassword");
    String newPassword = request.get("newPassword");
    Map<String, Object> response = authService.changePassword(authorizationHeader, oldPassword, newPassword);
    boolean success = Boolean.TRUE.equals(response.get("success"));
    return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/controller/AuthController.java
git commit -m "feat: add /change-password endpoint"
```

---

### Task 7: 后端 — SystemConfigController

**Files:**
- Create: `src/main/java/com/example/controller/SystemConfigController.java`

- [ ] **Step 1: 创建 SystemConfigController**

```java
package com.example.controller;

import com.example.entity.SystemConfig;
import com.example.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemConfigController {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(@RequestParam String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key);
        Map<String, Object> response = new HashMap<>();
        if (config != null) {
            response.put("key", config.getConfigKey());
            response.put("value", config.getConfigValue());
        } else {
            response.put("key", key);
            response.put("value", null);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");
        Map<String, Object> response = new HashMap<>();

        if (key == null || key.isBlank()) {
            response.put("success", false);
            response.put("message", "配置键不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        SystemConfig config = systemConfigRepository.findByConfigKey(key);
        if (config == null) {
            config = new SystemConfig(key, value);
        } else {
            config.setConfigValue(value);
        }
        systemConfigRepository.save(config);

        response.put("success", true);
        response.put("message", "配置已更新");
        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/controller/SystemConfigController.java
git commit -m "feat: add SystemConfigController"
```

---

### Task 8: 后端 — SeatService 新增打卡、诚信查询、预约拦截、过期增强

**Files:**
- Modify: `src/main/java/com/example/service/SeatService.java`

- [ ] **Step 1: 在 SeatService 中注入 UserRepository 和 SystemConfigRepository**

```java
@Autowired
private com.example.repository.UserRepository userRepository;

@Autowired
private com.example.repository.SystemConfigRepository systemConfigRepository;
```

- [ ] **Step 2: 新增 checkin 方法**

```java
public Map<String, Object> checkin(Long bookingId, String userId) {
    Map<String, Object> response = new HashMap<>();

    SeatBooking booking = seatBookingRepository.findById(bookingId).orElse(null);
    if (booking == null) {
        response.put("success", false);
        response.put("message", "预约记录不存在");
        return response;
    }

    if (!booking.getUserId().equals(userId)) {
        response.put("success", false);
        response.put("message", "该预约不属于当前用户");
        return response;
    }

    if (!"BOOKED".equals(booking.getStatus())) {
        response.put("success", false);
        response.put("message", "该预约状态不允许打卡");
        return response;
    }

    LocalTime now = LocalTime.now();
    if (now.isBefore(booking.getStartTime()) || now.isAfter(booking.getEndTime())) {
        response.put("success", false);
        response.put("message", "不在打卡时间范围内（" + booking.getStartTime() + " - " + booking.getEndTime() + "）");
        return response;
    }

    booking.setCheckedIn(true);
    seatBookingRepository.save(booking);

    com.example.entity.User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
    if (user != null) {
        user.setTotalBookings(user.getTotalBookings() + 1);
        user.setValidBookings(user.getValidBookings() + 1);
        userRepository.save(user);
    }

    response.put("success", true);
    response.put("message", "打卡成功");
    return response;
}
```

- [ ] **Step 3: 新增用户诚信指数计算方法**

```java
public Map<String, Object> getUserIntegrity(Long userId) {
    Map<String, Object> result = new HashMap<>();
    String userIdStr = String.valueOf(userId);

    // Count from User entity (total/valid are updated at checkin and expiry)
    com.example.entity.User user = userRepository.findById(userId).orElse(null);
    if (user != null) {
        result.put("totalBookings", user.getTotalBookings());
        result.put("validBookings", user.getValidBookings());
    } else {
        result.put("totalBookings", 0);
        result.put("validBookings", 0);
        return result;
    }

    int total = (int) result.get("totalBookings");
    int valid = (int) result.get("validBookings");
    int invalid = total - valid;
    double score = total > 0 ? (double) valid / total : 1.0;

    result.put("invalidBookings", invalid);
    result.put("integrityScore", Math.round(score * 10000.0) / 10000.0);
    return result;
}
```

- [ ] **Step 4: 增强 resetExpiredBookings — 过期未打卡更新 User 统计**

在 `resetExpiredBookings` 方法中，将 setting `booking.setStatus(STATUS_EXPIRED)` 之后的循环体改为：

```java
for (SeatBooking booking : expiredBookings) {
    booking.setStatus(STATUS_EXPIRED);
    seatBookingRepository.save(booking);

    // 未打卡的过期预约 → 用户 totalBookings+1 (无效预约)
    if (!Boolean.TRUE.equals(booking.getCheckedIn())) {
        String uid = booking.getUserId();
        if (uid != null) {
            try {
                com.example.entity.User user = userRepository.findById(Long.parseLong(uid)).orElse(null);
                if (user != null) {
                    user.setTotalBookings(user.getTotalBookings() + 1);
                    userRepository.save(user);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    affectedSeatIds.add(booking.getSeatId());
}
```

- [ ] **Step 5: 在 bookSeatWithDuration 中新增诚信指数拦截**

在 `bookSeatWithDuration` 方法开头，after parameter validation, add:

```java
// Integrity check
SystemConfig thresholdConfig = systemConfigRepository.findByConfigKey("integrity_threshold");
double threshold = thresholdConfig != null ? Double.parseDouble(thresholdConfig.getConfigValue()) : 0.6;

com.example.entity.User bookingUser = userRepository.findById(Long.parseLong(userId)).orElse(null);
if (bookingUser != null && bookingUser.getTotalBookings() > 0) {
    double userScore = (double) bookingUser.getValidBookings() / bookingUser.getTotalBookings();
    if (userScore < threshold) {
        return false; // Controller will return the error message
    }
}
```

- [ ] **Step 6: 修改 bookSeat 方法签名以返回详细信息**

We need the controller to know WHY booking failed. The current `bookSeatWithDuration` returns boolean. Change approach: Have the controller call `getUserIntegrity` first and do the check, or change the service method signature.

Simplest approach: The SeatController.bookSeat method will do the integrity check BEFORE calling the service:

```java
// In SeatController.bookSeat method, before calling service:
Map<String, Object> integrity = seatService.getUserIntegrity(Long.parseLong(userId));
SystemConfig thresholdConfig = systemConfigRepository.findByConfigKey("integrity_threshold");
double threshold = thresholdConfig != null ? Double.parseDouble(thresholdConfig.getConfigValue()) : 0.6;
double userScore = ((Number) integrity.get("integrityScore")).doubleValue();

if (userScore < threshold) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", "您的诚信指数过低（" + Math.round(userScore * 100) + "%），低于阈值" + Math.round(threshold * 100) + "%，无法预约");
    return ResponseEntity.ok(response);
}
```

And we need to add `@Autowired private SystemConfigRepository systemConfigRepository;` to SeatController.

- [ ] **Step 7: 提交**

```bash
git add src/main/java/com/example/service/SeatService.java
git commit -m "feat: add checkin, integrity, booking gate in SeatService"
```

---

### Task 9: 后端 — SeatController 新增 /checkin 端点

**Files:**
- Modify: `src/main/java/com/example/controller/SeatController.java`

- [ ] **Step 1: 添加 checkin 端点 + 预约拦截 + 注入 SystemConfigRepository**

Add the import:
```java
import com.example.repository.SystemConfigRepository;
```

Add the autowired field:
```java
@Autowired
private SystemConfigRepository systemConfigRepository;
```

Add checkin endpoint:
```java
@PostMapping("/checkin/{bookingId}")
public ResponseEntity<Map<String, Object>> checkin(
        @PathVariable Long bookingId,
        @RequestParam String userId) {
    Map<String, Object> response = seatService.checkin(bookingId, userId);
    boolean success = Boolean.TRUE.equals(response.get("success"));
    return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
}
```

Modify the `bookSeat` method — add integrity check at the beginning, right after the try block starts and before calling seatService:

```java
// After: LocalTime parsedEndTime = LocalTime.parse(endTime);
// Before: boolean success = seatService.bookSeatWithDuration(...)
Map<String, Object> integrity = seatService.getUserIntegrity(Long.parseLong(userId));
SystemConfig thresholdConfig = systemConfigRepository.findByConfigKey("integrity_threshold");
double threshold = thresholdConfig != null ? Double.parseDouble(thresholdConfig.getConfigValue()) : 0.6;
double userScore = ((Number) integrity.get("integrityScore")).doubleValue();

if (userScore < threshold) {
    response.put("success", false);
    response.put("message", "您的诚信指数过低（" + Math.round(userScore * 100) + "%），无法预约，诚信阈值：" + Math.round(threshold * 100) + "%");
    return ResponseEntity.ok(response);
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/controller/SeatController.java
git commit -m "feat: add checkin endpoint and integrity booking gate"
```

---

### Task 10: 后端 — UserController 新增 integrity 端点

**Files:**
- Modify: `src/main/java/com/example/controller/UserController.java`

- [ ] **Step 1: 添加 /integrity 端点**

```java
@GetMapping("/{id}/integrity")
public Map<String, Object> getUserIntegrity(@PathVariable Long id) {
    return seatService.getUserIntegrity(id);
}
```

Note: This requires injecting SeatService in UserController:
```java
@Autowired
private com.example.service.SeatService seatService;
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/controller/UserController.java
git commit -m "feat: add user integrity endpoint"
```

---

### Task 11: 前端 — API 层更新

**Files:**
- Modify: `src/api/authApi.js`
- Modify: `src/api/seatApi.js`
- Modify: `src/api/userApi.js`
- Create: `src/api/systemApi.js`

- [ ] **Step 1: authApi.js 新增 changePassword**

```javascript
changePassword: async (oldPassword, newPassword) => {
    const response = await apiClient.post('/auth/change-password', { oldPassword, newPassword })
    return response.data
}
```

- [ ] **Step 2: seatApi.js 新增 checkin**

```javascript
export const checkin = async (bookingId, userId) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/seats/checkin/${bookingId}`, null, {
            params: { userId }
        })
        return response.data
    } catch (error) {
        console.error('打卡失败:', error)
        throw error
    }
}
```

Also add `checkin` to the default export.

- [ ] **Step 3: userApi.js 新增 getIntegrity**

```javascript
export const getUserIntegrity = async (id) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/users/${id}/integrity`)
        return response.data
    } catch (error) {
        console.error('获取用户诚信指数失败:', error)
        throw error
    }
}
```

Also add to default export.

- [ ] **Step 4: 创建 systemApi.js**

```javascript
import axios from 'axios'

const API_BASE_URL = '/api'

export const getConfig = async (key) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/system/config`, { params: { key } })
        return response.data
    } catch (error) {
        console.error('获取系统配置失败:', error)
        throw error
    }
}

export const updateConfig = async (key, value) => {
    try {
        const response = await axios.put(`${API_BASE_URL}/system/config`, { key, value })
        return response.data
    } catch (error) {
        console.error('更新系统配置失败:', error)
        throw error
    }
}

export default { getConfig, updateConfig }
```

- [ ] **Step 5: 提交**

```bash
git add src/api/authApi.js src/api/seatApi.js src/api/userApi.js src/api/systemApi.js
git commit -m "feat: add frontend API for checkin, integrity, password change, system config"
```

---

### Task 12: 前端 — SeatBookingView 账号信息区改造

**Files:**
- Modify: `src/views/SeatBookingView.vue`

- [ ] **Step 1: 导入新增 API**

```javascript
import { authApi } from '../api/authApi'
import * as userApi from '../api/userApi'
```

- [ ] **Step 2: 新增响应式变量**

```javascript
// Password change
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

// Integrity stats
const integrityStats = ref({ totalBookings: 0, validBookings: 0, invalidBookings: 0, integrityScore: 1.0 })
```

- [ ] **Step 3: 在 loadUserBookings 后加载诚信数据**

```javascript
async function loadIntegrityStats() {
    if (!currentUserId.value) return
    try {
        const stats = await userApi.getUserIntegrity(currentUserId.value)
        integrityStats.value = stats
    } catch { /* ignore */ }
}
```

Update `onMounted` and `loadUserBookings` to also call `loadIntegrityStats()`.

- [ ] **Step 4: 账号信息区 HTML（替换现有 profile section）**

```html
<section v-if="activeSection === 'profile'" class="section">
  <div class="card">
    <div class="card-head">
      <h2>账号信息</h2>
      <p>查看当前登录账号信息。</p>
    </div>
    <div class="profile-grid">
      <div class="profile-item"><span class="profile-label">姓名</span><p>{{ authStore.user?.name || '未命名' }}</p></div>
      <div class="profile-item"><span class="profile-label">邮箱</span><p>{{ authStore.user?.email || '-' }}</p></div>
      <div class="profile-item"><span class="profile-label">角色</span><p>{{ authStore.user?.role || 'USER' }}</p></div>
    </div>
  </div>

  <!-- 预约统计 -->
  <div class="card">
    <div class="card-head"><h2>预约统计</h2></div>
    <div class="stats-row">
      <div class="stat-item"><span class="stat-num">{{ integrityStats.totalBookings }}</span><span class="stat-lbl">总预约</span></div>
      <div class="stat-item"><span class="stat-num">{{ integrityStats.validBookings }}</span><span class="stat-lbl">有效预约</span></div>
      <div class="stat-item"><span class="stat-num">{{ integrityStats.invalidBookings }}</span><span class="stat-lbl">无效预约</span></div>
    </div>
    <div class="integrity-bar-wrap">
      <div class="integrity-bar-head"><span>诚信指数</span><strong>{{ Math.round(integrityStats.integrityScore * 100) }}%</strong></div>
      <div class="integrity-bar"><div class="integrity-fill" :style="{ width: Math.round(integrityStats.integrityScore * 100) + '%' }"></div></div>
    </div>
  </div>

  <!-- 修改密码 -->
  <div class="card">
    <div class="card-head"><h2>修改密码</h2></div>
    <div class="form-stack">
      <label>旧密码 <input v-model="oldPassword" type="password" placeholder="请输入旧密码" /></label>
      <label>新密码 <input v-model="newPassword" type="password" placeholder="请输入新密码" /></label>
      <label>确认密码 <input v-model="confirmPassword" type="password" placeholder="请再次输入新密码" /></label>
      <button class="btn-primary" @click="handleChangePassword">修改密码</button>
    </div>
  </div>
</section>
```

- [ ] **Step 5: 新增 handleChangePassword 方法**

```javascript
async function handleChangePassword() {
    if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
        showNotificationMessage('请填写所有密码字段', 'error'); return
    }
    if (newPassword.value !== confirmPassword.value) {
        showNotificationMessage('两次输入的新密码不一致', 'error'); return
    }
    if (newPassword.value.length < 6) {
        showNotificationMessage('新密码长度至少6位', 'error'); return
    }
    try {
        const result = await authApi.changePassword(oldPassword.value, newPassword.value)
        if (result?.success) {
            showNotificationMessage('密码修改成功', 'success')
            oldPassword.value = ''; newPassword.value = ''; confirmPassword.value = ''
        } else {
            showNotificationMessage(result?.message || '密码修改失败', 'error')
        }
    } catch {
        showNotificationMessage('密码修改失败，请重试', 'error')
    }
}
```

- [ ] **Step 6: 新增样式（scoped style 内）**

```css
/* Stats row */
.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-bottom: 1.25rem; }
.stat-item { text-align: center; padding: 0.8rem; border-radius: var(--radius-md); background: var(--paper-white); border: 1px solid var(--border-warm); }
.stat-num { display: block; font-size: 1.5rem; font-weight: 800; color: var(--text-body); }
.stat-lbl { display: block; font-size: 0.75rem; color: var(--text-subtle); margin-top: 0.2rem; }

/* Integrity bar */
.integrity-bar-wrap { margin-top: 0.5rem; }
.integrity-bar-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; font-size: 0.85rem; }
.integrity-bar-head strong { color: var(--amber); }
.integrity-bar { height: 8px; border-radius: 999px; background: var(--paper-warm); overflow: hidden; }
.integrity-fill { height: 100%; border-radius: 999px; background: var(--amber-gradient); transition: width 0.5s ease; }

/* Form stack */
.form-stack { display: grid; gap: 0.8rem; }
.form-stack label { display: grid; gap: 0.3rem; font-size: 0.82rem; color: var(--text-soft); }
```

- [ ] **Step 7: 提交**

```bash
git add src/views/SeatBookingView.vue
git commit -m "feat: add profile stats, integrity bar, and password change to SeatBookingView"
```

---

### Task 13: 前端 — SeatBookingView 我的预约区打卡按钮

**Files:**
- Modify: `src/views/SeatBookingView.vue` (继续修改)

- [ ] **Step 1: 新增辅助计算属性**

```javascript
function getBookingTimeStatus(booking) {
    if (booking.checkedIn) return 'checked_in'
    if (booking.status === 'EXPIRED') return 'expired_no_checkin'
    if (booking.status === 'CANCELLED') return 'cancelled'
    if (booking.status === 'BOOKED') {
        const now = new Date()
        const todayStr = now.toISOString().split('T')[0]
        if (booking.date !== todayStr) return 'future'
        const currentTime = now.getHours() * 60 + now.getMinutes()
        const startMin = toMinutes(booking.startTime)
        const endMin = toMinutes(booking.endTime)
        if (currentTime < startMin) return 'pending'
        if (currentTime >= startMin && currentTime <= endMin) return 'can_checkin'
        // past endTime but still BOOKED — hasn't been expired yet
        return 'pending'
    }
    return 'unknown'
}
```

- [ ] **Step 2: 更新「我的预约」模板**

Replace the booking-actions div in the bookings list:

```html
<div class="booking-actions">
    <span class="tag tag-default">{{ getStatusLabel(b.status) }}</span>
    <template v-if="b.status === 'BOOKED'">
        <span v-if="getBookingTimeStatus(b) === 'pending'" class="tag tag-default">待开始</span>
        <button v-if="getBookingTimeStatus(b) === 'can_checkin'" class="btn-checkin" @click="handleCheckin(b)">打卡签到</button>
        <span v-if="b.checkedIn" class="tag tag-success">已打卡</span>
    </template>
    <span v-if="b.status === 'EXPIRED' && !b.checkedIn" class="tag tag-danger">无效预约</span>
    <span v-if="b.status === 'EXPIRED' && b.checkedIn" class="tag tag-success">已完成</span>
    <button v-if="b.status === 'BOOKED' && b.date >= today && !b.checkedIn" class="btn-danger" @click="handleCancelBookingById(b.id)">取消</button>
</div>
```

- [ ] **Step 3: 新增 handleCheckin 方法**

```javascript
async function handleCheckin(booking) {
    if (!currentUserId.value) { showNotificationMessage('请先登录', 'error'); return }
    try {
        const result = await seatApi.checkin(booking.id, currentUserId.value)
        if (result?.success) {
            showNotificationMessage('打卡成功', 'success')
            await Promise.allSettled([loadUserBookings(), loadIntegrityStats()])
        } else {
            showNotificationMessage(result?.message || '打卡失败', 'error')
        }
    } catch { showNotificationMessage('打卡失败，请重试', 'error') }
}
```

- [ ] **Step 4: 更新 normalizeBooking 以包含 checkedIn**

```javascript
function normalizeBooking(raw) {
    return {
        id: raw.id, seatId: String(raw.seatId || ''),
        date: raw.bookingDate || raw.date || '',
        startTime: normalizeTime(raw.startTime), endTime: normalizeTime(raw.endTime),
        status: raw.status || 'BOOKED', userId: String(raw.userId || ''),
        createdAt: raw.createdAt || '', checkedIn: Boolean(raw.checkedIn)
    }
}
```

- [ ] **Step 5: 新增样式**

```css
.btn-checkin { padding: 0.35rem 0.8rem; border-radius: var(--radius-pill); font-size: 0.78rem; font-weight: 600; background: var(--green-soft); color: var(--green-text); border: 1px solid rgba(21,128,61,0.2); cursor: pointer; transition: all var(--ease-smooth); }
.btn-checkin:hover { background: #22c55e; color: #fff; }
.tag-success { background: var(--green-soft); color: var(--green-text); border: 1px solid rgba(21,128,61,0.15); }
.tag-danger { background: var(--red-soft); color: var(--red-text); border: 1px solid rgba(153,27,27,0.15); }
```

- [ ] **Step 6: 提交**

```bash
git add src/views/SeatBookingView.vue
git commit -m "feat: add checkin button to booking list"
```

---

### Task 14: 前端 — AdminView 用户详情诚信指数 + 系统设置阈值

**Files:**
- Modify: `src/views/AdminView.vue`

- [ ] **Step 1: 导入 systemApi**

```javascript
import * as systemApi from '../api/systemApi'
```

- [ ] **Step 2: 新增阈值响应式变量**

```javascript
const integrityThreshold = ref(0.6)
```

- [ ] **Step 3: 修改 viewUserDetail — 异步获取诚信指数**

```javascript
async function viewUserDetail(u) {
    let integrityText = '-'
    try {
        const integrity = await userApi.getUserIntegrity(u.id)
        integrityText = Math.round((integrity.integrityScore || 0) * 100) + '%'
    } catch { /* use default */ }

    detailModal.value = {
        visible: true, title: `用户详情：${u.name}`,
        items: [
            { label: '用户ID', value: `#${u.id}` },
            { label: '姓名', value: u.name || '-' },
            { label: '邮箱', value: u.email || '-' },
            { label: '角色', value: u.role === 'ADMIN' ? '管理员' : '普通用户' },
            { label: '注册时间', value: formatDateTime(u.createdAt) },
            { label: '预约次数', value: String(getUserBookingCount(u.id)) },
            { label: '诚信指数', value: integrityText }
        ]
    }
}
```

- [ ] **Step 4: 在系统设置区新增阈值卡片（settings section）**

在 settings-grid 内添加：

```html
<div class="panel">
  <div class="panel-head"><h3>预约规则设置</h3></div>
  <div class="form-grid">
    <label>诚信指数阈值 <input v-model.number="integrityThreshold" type="number" min="0" max="1" step="0.05" /></label>
    <p class="muted">低于此值的用户将无法预约座位（0.0 ~ 1.0，默认 0.6）</p>
  </div>
</div>
```

- [ ] **Step 5: 修改 saveSettings — 同时保存阈值到后端**

```javascript
async function saveSettings() {
    localStorage.setItem('admin_system_settings', JSON.stringify(systemSettings.value))
    try {
        await systemApi.updateConfig('integrity_threshold', String(integrityThreshold.value))
    } catch { /* ignore if backend not available */ }
    showNotificationMessage('设置已保存', 'success')
}
```

- [ ] **Step 6: 在 initializePage 中加载阈值**

```javascript
async function loadIntegrityThreshold() {
    try {
        const config = await systemApi.getConfig('integrity_threshold')
        if (config?.value) integrityThreshold.value = parseFloat(config.value)
    } catch { integrityThreshold.value = 0.6 }
}
```

Add to `initializePage()` after existing calls.

- [ ] **Step 7: 提交**

```bash
git add src/views/AdminView.vue
git commit -m "feat: add integrity score to user detail and threshold to settings"
```

---

### Task 15: AdminUserInitializer 增强

**Files:**
- Modify: `src/main/java/com/example/config/AdminUserInitializer.java`

- [ ] **Step 1: 注入 SystemConfigRepository 并初始化配置**

```java
@Autowired
private com.example.repository.SystemConfigRepository systemConfigRepository;

// 在 initAdminUser() 方法末尾:
if (systemConfigRepository.findByConfigKey("integrity_threshold") == null) {
    com.example.entity.SystemConfig config = new com.example.entity.SystemConfig("integrity_threshold", "0.6");
    systemConfigRepository.save(config);
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/config/AdminUserInitializer.java
git commit -m "feat: auto-init integrity_threshold config"
```
