# Backend 模块文档

[根目录](../CLAUDE.md) > **backend**

---

## 变更记录 (Changelog)

### 2026-02-01 17:23:13
- 初始化后端模块文档
- 扫描完成：12个控制器、11个服务、11个仓储、13个实体
- 识别核心业务流程：认证、充电桩管理、预约、充电记录、费用配置

---

## 模块职责

Backend 模块是 Smart Charger 系统的核心后端服务，基于 Spring Boot 3.2.2 构建，提供完整的 RESTful API 接口。主要职责包括：

- **用户认证与授权**：JWT Token 生成与验证、角色权限管理
- **充电桩管理**：充电桩 CRUD、状态更新、附近充电桩查询
- **预约系统**：预约创建、取消、过期检查（定时任务）
- **充电记录**：充电开始/结束、费用计算、统计分析
- **费用配置**：价格策略管理、费用估算
- **公告管理**：系统公告发布、查询
- **故障报修**：故障上报、处理、统计
- **预警通知**：超时预警、阈值配置
- **用户管理**：用户信息维护、车辆管理

---

## 入口与启动

### 主类
- **文件路径**：`src/main/java/com/smartcharger/SmartChargerApplication.java`
- **注解**：`@SpringBootApplication` + `@EnableScheduling`
- **启动方式**：
  ```bash
  mvn spring-boot:run
  # 或在 IDE 中直接运行 main 方法
  ```

### 服务配置
- **端口**：8080
- **上下文路径**：`/api`
- **完整访问地址**：`http://localhost:8080/api`

### 配置文件
- **主配置**：`src/main/resources/application.properties`
- **关键配置项**：
  - 数据库：MySQL (localhost:3306/smart_charger)
  - Redis：localhost:6379
  - JWT：密钥、过期时间（7天）
  - JPA：自动建表（ddl-auto=update）、SQL 日志

---

## 对外接口

### API 路由总览

| 控制器 | 路由前缀 | 主要功能 | 认证要求 |
|--------|---------|---------|---------|
| AuthController | `/auth` | 登录、注册、个人信息 | 部分需要 |
| ChargingPileController | `/charging-piles` | 充电桩查询、附近充电桩 | 需要 |
| ReservationController | `/reservations` | 预约管理 | 需要 |
| VehicleController | `/vehicles` | 车辆管理 | 需要 |
| ChargingRecordController | `/charging-records` | 充电记录、统计 | 需要 |
| PriceConfigController | `/price-configs` | 价格配置（管理端） | 需要（管理员） |
| AnnouncementController | `/announcements` | 公告管理 | 部分需要 |
| FaultReportController | `/fault-reports` | 故障报修 | 需要 |
| WarningNoticeController | `/warning-notices` | 预警通知 | 需要 |
| UserManagementController | `/admin/users` | 用户管理（管理端） | 需要（管理员） |
| HealthController | `/health` | 健康检查 | 不需要 |

### 核心接口示例

#### 1. 用户认证
```java
POST /api/auth/login
Body: { "username": "user", "password": "pass" }
Response: { "token": "jwt_token", "userInfo": {...} }

POST /api/auth/register
Body: { "username": "user", "password": "pass", "email": "..." }

GET /api/auth/profile
Header: Authorization: Bearer {token}
Response: { "id": 1, "username": "user", ... }
```

#### 2. 充电桩查询
```java
GET /api/charging-piles?status=AVAILABLE&type=FAST
Response: { "content": [...], "totalElements": 10 }

GET /api/charging-piles/nearby?latitude=39.9&longitude=116.4&radius=5
Response: [ { "id": 1, "name": "充电桩A", "distance": 1.2 } ]
```

#### 3. 预约管理
```java
POST /api/reservations
Body: { "chargingPileId": 1, "vehicleId": 1, "startTime": "2026-02-01T10:00:00" }

GET /api/reservations/check-availability?chargingPileId=1&startTime=...
Response: { "available": true }

DELETE /api/reservations/{id}
```

#### 4. 充电记录
```java
POST /api/charging-records/start
Body: { "chargingPileId": 1, "vehicleId": 1 }

POST /api/charging-records/end
Body: { "recordId": 1, "endTime": "...", "energyConsumed": 30.5 }

GET /api/charging-records/statistics/monthly?year=2026&month=2
Response: { "totalEnergy": 150.5, "totalCost": 120.0, ... }
```

---

## 关键依赖与配置

### Maven 依赖 (pom.xml)
- **Spring Boot Starter**：web、data-jpa、validation、aop、security
- **数据库**：MySQL Connector (mysql-connector-j)
- **缓存与锁**：spring-boot-starter-data-redis、redisson-spring-boot-starter 3.24.3
- **安全**：jjwt-api/impl/jackson 0.12.3
- **工具库**：hutool-all 5.8.37、lombok、apache poi-ooxml 5.2.3

### 安全配置
- **JWT 过滤器**：`JwtAuthenticationFilter` 拦截所有请求，验证 Token
- **白名单路径**：
  - `/api/auth/login`
  - `/api/auth/register`
  - `/api/health`
- **Token 生成**：`JwtTokenProvider` 使用 HS512 算法签名
- **密码加密**：BCryptPasswordEncoder

### Redis 配置
- **用途**：
  - 用户会话缓存
  - 分布式锁（Redisson）用于预约并发控制
- **连接池**：Lettuce（max-active=8, max-idle=8）

### 定时任务
- **ReservationExpireTask**：每分钟检查过期预约，自动取消
- **OvertimeWarningTask**：每5分钟检查充电超时，发送预警通知

---

## 数据模型

### 核心实体 (Entity)

#### 1. User (用户)
- **表名**：`users`
- **字段**：id, username, password, email, phone, realName, enabled, roles
- **关系**：
  - 多对多：Role (通过 user_roles 中间表)
  - 一对多：Vehicle, Reservation, ChargingRecord, FaultReport

#### 2. ChargingPile (充电桩)
- **表名**：`charging_piles`
- **字段**：id, name, location, latitude, longitude, type, status, power, currentPower
- **枚举**：
  - ChargingPileType: SLOW, FAST, SUPER_FAST
  - ChargingPileStatus: AVAILABLE, OCCUPIED, MAINTENANCE, FAULT
- **关系**：一对多 Reservation, ChargingRecord, FaultReport

#### 3. Reservation (预约)
- **表名**：`reservations`
- **字段**：id, userId, chargingPileId, vehicleId, startTime, endTime, status
- **枚举**：ReservationStatus: PENDING, CONFIRMED, CANCELLED, EXPIRED, COMPLETED
- **并发控制**：使用 Redisson 分布式锁防止重复预约

#### 4. ChargingRecord (充电记录)
- **表名**：`charging_records`
- **字段**：id, userId, chargingPileId, vehicleId, startTime, endTime, energyConsumed, totalCost, status
- **枚举**：ChargingRecordStatus: CHARGING, COMPLETED, CANCELLED
- **费用计算**：根据 PriceConfig 和充电时长/电量计算

#### 5. PriceConfig (价格配置)
- **表名**：`price_configs`
- **字段**：id, name, pricePerKwh, serviceFee, peakHourMultiplier, effectiveDate, expiryDate
- **用途**：管理员配置不同时段的电价策略

#### 6. Announcement (公告)
- **表名**：`announcements`
- **字段**：id, title, content, priority, status, publishTime, expiryTime
- **枚举**：AnnouncementStatus: DRAFT, PUBLISHED, EXPIRED

#### 7. FaultReport (故障报修)
- **表名**：`fault_reports`
- **字段**：id, chargingPileId, userId, description, status, reportTime, handleTime
- **枚举**：FaultReportStatus: PENDING, PROCESSING, RESOLVED, CLOSED

#### 8. WarningNotice (预警通知)
- **表名**：`warning_notices`
- **字段**：id, userId, type, content, sendStatus, readStatus, sendTime
- **枚举**：WarningNoticeType: OVERTIME, LOW_BATTERY, SYSTEM

#### 9. Vehicle (车辆)
- **表名**：`vehicles`
- **字段**：id, userId, brand, model, licensePlate, batteryCapacity

#### 10. SystemConfig (系统配置)
- **表名**：`system_configs`
- **字段**：id, configKey, configValue, description
- **用途**：存储全局配置（如超时阈值）

#### 11. Role (角色)
- **表名**：`roles`
- **字段**：id, name, description
- **预设角色**：USER, ADMIN

#### 12. Permission (权限)
- **表名**：`permissions`
- **字段**：id, name, description, resource, action

#### 13. BaseEntity (基类)
- **字段**：createdAt, updatedAt
- **审计**：使用 `@EntityListeners(AuditingEntityListener.class)` 自动填充时间

---

## 测试与质量

### 测试现状
- **单元测试**：未发现 `src/test` 目录
- **集成测试**：未配置
- **API 测试**：建议使用 Postman/Insomnia

### 代码质量工具
- **日志**：SLF4J + Logback（DEBUG 级别）
- **AOP 日志**：`ApiLogAspect` 记录所有 API 请求/响应
- **异常处理**：`GlobalExceptionHandler` 统一处理异常并返回标准格式

### 建议补充
1. **单元测试**：为 Service 层编写 JUnit 5 + Mockito 测试
2. **集成测试**：使用 `@SpringBootTest` 测试完整流程
3. **API 文档**：集成 Swagger/OpenAPI 自动生成文档

---

## 常见问题 (FAQ)

### Q1: 如何添加新的实体？
1. 在 `entity/` 下创建实体类，继承 `BaseEntity`
2. 在 `repository/` 下创建 Repository 接口，继承 `JpaRepository`
3. 在 `service/` 和 `service/impl/` 下创建服务接口和实现
4. 在 `controller/` 下创建控制器，定义 API 路由
5. 在 `dto/request/` 和 `dto/response/` 下创建 DTO 类

### Q2: 如何调试 SQL 语句？
- 查看控制台日志（`spring.jpa.show-sql=true`）
- 日志格式化已开启（`spring.jpa.properties.hibernate.format_sql=true`）

### Q3: 如何修改 JWT 过期时间？
- 修改 `application.properties` 中的 `jwt.expiration`（单位：毫秒）
- 默认：604800000ms = 7天

### Q4: 如何处理并发预约冲突？
- `ReservationServiceImpl` 使用 Redisson 分布式锁
- 锁键格式：`reservation:lock:{chargingPileId}:{startTime}`

### Q5: 定时任务如何配置？
- 在类上添加 `@Component` + `@Scheduled`
- 示例：`@Scheduled(cron = "0 * * * * ?")` 每分钟执行

---

## 相关文件清单

### 核心目录结构
```
backend/
├── src/main/java/com/smartcharger/
│   ├── SmartChargerApplication.java          # 主启动类
│   ├── controller/                           # 控制器层 (12个)
│   │   ├── AuthController.java
│   │   ├── ChargingPileController.java
│   │   ├── ReservationController.java
│   │   ├── VehicleController.java
│   │   ├── ChargingRecordController.java
│   │   ├── PriceConfigController.java
│   │   ├── AnnouncementController.java
│   │   ├── FaultReportController.java
│   │   ├── WarningNoticeController.java
│   │   ├── UserManagementController.java
│   │   └── HealthController.java
│   ├── service/                              # 服务接口 (11个)
│   │   └── impl/                             # 服务实现 (11个)
│   ├── repository/                           # 数据访问层 (11个)
│   ├── entity/                               # 实体类 (13个)
│   │   └── enums/                            # 枚举类 (8个)
│   ├── dto/                                  # 数据传输对象
│   │   ├── request/                          # 请求 DTO
│   │   └── response/                         # 响应 DTO
│   ├── common/                               # 通用组件
│   │   ├── config/                           # 配置类
│   │   │   ├── SecurityConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   └── JpaAuditingConfig.java
│   │   ├── security/                         # 安全组件
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── exception/                        # 异常处理
│   │   │   ├── BusinessException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── result/                           # 统一响应
│   │   │   ├── Result.java
│   │   │   └── ResultCode.java
│   │   ├── aspect/                           # AOP 切面
│   │   │   └── ApiLogAspect.java
│   │   └── filter/                           # 过滤器
│   │       └── RequestCachingFilter.java
│   └── task/                                 # 定时任务
│       ├── ReservationExpireTask.java
│       └── OvertimeWarningTask.java
├── src/main/resources/
│   └── application.properties                # 主配置文件
└── pom.xml                                   # Maven 依赖配置
```

### 关键文件路径
- **主启动类**：`src/main/java/com/smartcharger/SmartChargerApplication.java`
- **全局异常处理**：`src/main/java/com/smartcharger/common/exception/GlobalExceptionHandler.java`
- **JWT 配置**：`src/main/java/com/smartcharger/common/security/JwtTokenProvider.java`
- **数据库配置**：`src/main/resources/application.properties`

---

**文档生成时间**：2026-02-01 17:23:13
**扫描文件数**：130+ Java 文件
**覆盖率**：核心业务逻辑已完整扫描
