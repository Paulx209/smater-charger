# Proposal: 充电记录管理功能优化

## Context

### User Need
充电记录管理功能已完整实现（后端 API + 前端页面），但存在以下质量问题需要优化：

**后端性能问题**：
1. **N+1 查询问题**：每次查询充电记录列表时，对每条记录单独查询充电桩和车辆信息，导致数据库查询次数激增（100条记录 = 200+次查询）
2. **并发竞态条件**：`startCharging` 方法在检查"用户无活跃充电记录"和创建新记录之间存在时间窗口，可能导致同一用户创建多个充电记录

**前端用户体验问题**：
1. **加载状态不友好**：数据加载时显示空白，缺少加载骨架屏
2. **错误处理不完善**：API 失败时仅显示简单错误消息，缺少错误边界和重试机制
3. **响应式布局问题**：表格在移动设备上需要横向滚动，用户体验差

### Discovered Constraints

#### 后端约束
- **单用户单充电约束**：`findByUserIdAndStatus(userId, CHARGING)` 必须返回唯一结果
- **事务边界**：`startCharging` 和 `endCharging` 必须保持事务一致性（ChargingRecord + ChargingPile + Reservation）
- **JPA 查询模式**：当前使用派生查询方法，需要改为 `@Query` with `JOIN FETCH`
- **Redisson 分布式锁**：项目已集成 Redisson，可用于并发控制（参考 ReservationService）
- **Spring Data JPA 版本**：3.2.2，支持 `@EntityGraph` 和 `JOIN FETCH`

#### 前端约束
- **Element Plus 版本**：2.13.1，支持 `el-skeleton` 组件
- **Vue 3 Composition API**：必须使用 `<script setup lang="ts">` 模式
- **Pinia Store 模式**：异步操作必须返回 `Promise<T>`，loading 状态必须在 try-finally 中管理
- **响应式设计**：需要支持移动端（最小宽度 375px）
- **错误处理模式**：使用 `ElMessage.error()` 显示错误，`console.error()` 记录日志

#### 数据流约束
- **后端 DTO 结构**：`ChargingRecordResponse` 包含 31 个字段，包括关联的 pile 和 vehicle 信息
- **分页模式**：后端使用 1-indexed 分页（page=1 表示第一页），前端需要转换
- **日期格式**：后端使用 `yyyy-MM-dd HH:mm:ss`，前端使用 `toLocaleString('zh-CN')`

---

## Requirements

### Requirement 1: 修复后端 N+1 查询问题
**Priority**: HIGH
**Type**: Performance Optimization

**Description**:
优化充电记录查询方法，使用 `JOIN FETCH` 一次性加载关联的充电桩和车辆信息，减少数据库查询次数。

**Scenarios**:
1. **场景 1.1**：查询充电记录列表（带筛选）
   - **Given**: 用户请求充电记录列表，筛选条件为 status=COMPLETED，日期范围 2026-01-01 到 2026-01-31，page=1，size=10
   - **When**: 调用 `ChargingRecordService.getChargingRecordList(userId, status, startDate, endDate, page, size)`
   - **Then**:
     - 执行单次 SQL 查询，使用 `LEFT JOIN FETCH` 加载 ChargingPile 和 Vehicle
     - 返回 `Page<ChargingRecordResponse>` 包含 10 条记录
     - 数据库查询次数 ≤ 3（1次主查询 + 最多2次关联查询）
     - 响应时间 < 500ms（相比当前 2000ms+）

2. **场景 1.2**：查询充电记录详情
   - **Given**: 用户请求充电记录详情，recordId=123
   - **When**: 调用 `ChargingRecordService.getChargingRecordDetail(userId, recordId)`
   - **Then**:
     - 执行单次 SQL 查询，使用 `JOIN FETCH` 加载 ChargingPile 和 Vehicle
     - 如果 status=COMPLETED，额外查询 PriceConfig 计算费用明细
     - 数据库查询次数 ≤ 2
     - 响应时间 < 200ms

3. **场景 1.3**：管理端查询所有充电记录
   - **Given**: 管理员请求所有充电记录，筛选条件为 userId=null，chargingPileId=5，status=null，page=1，size=20
   - **When**: 调用 `ChargingRecordService.getAllChargingRecords(...)`
   - **Then**:
     - 执行单次 SQL 查询，使用 `LEFT JOIN FETCH`
     - 返回 `Page<ChargingRecordResponse>` 包含 20 条记录
     - 数据库查询次数 ≤ 3

**Acceptance Criteria**:
- [ ] `ChargingRecordRepository` 添加自定义查询方法，使用 `@Query` with `JOIN FETCH`
- [ ] `getChargingRecordList` 方法数据库查询次数从 O(n) 降低到 O(1)
- [ ] `getChargingRecordDetail` 方法数据库查询次数 ≤ 2
- [ ] 所有查询方法响应时间降低 60% 以上
- [ ] 现有单元测试通过，无功能回归

---

### Requirement 2: 修复后端并发竞态条件
**Priority**: HIGH
**Type**: Bug Fix

**Description**:
在 `startCharging` 方法中添加分布式锁，防止同一用户在短时间内创建多个充电记录。

**Scenarios**:
1. **场景 2.1**：并发请求开始充电
   - **Given**: 用户 userId=1 同时发送 2 个 `startCharging` 请求（chargingPileId=10, vehicleId=5）
   - **When**: 两个请求几乎同时到达后端（时间差 < 100ms）
   - **Then**:
     - 第一个请求获取分布式锁，成功创建充电记录
     - 第二个请求等待锁释放后，检查发现用户已有活跃充电记录
     - 第二个请求返回错误 `USER_ALREADY_CHARGING (4402)`
     - 数据库中仅存在 1 条 status=CHARGING 的记录

2. **场景 2.2**：锁超时处理
   - **Given**: 用户 userId=1 发送 `startCharging` 请求，但服务处理时间超过锁超时时间（30秒）
   - **When**: 锁自动释放，另一个请求尝试获取锁
   - **Then**:
     - 第一个请求完成后释放锁（或超时自动释放）
     - 第二个请求能够正常获取锁并处理
     - 不会出现死锁或永久阻塞

3. **场景 2.3**：锁粒度验证
   - **Given**: 用户 userId=1 和 userId=2 同时发送 `startCharging` 请求
   - **When**: 两个请求到达后端
   - **Then**:
     - 两个请求使用不同的锁 key（`charging:start:1` 和 `charging:start:2`）
     - 两个请求可以并发执行，互不影响
     - 两个用户都能成功创建充电记录

**Acceptance Criteria**:
- [ ] 使用 Redisson `RLock` 实现分布式锁，锁 key 格式为 `charging:start:{userId}`
- [ ] 锁超时时间设置为 30 秒，等待时间设置为 5 秒
- [ ] 并发测试：100 个并发请求，仅 1 个成功，99 个返回 `USER_ALREADY_CHARGING`
- [ ] 锁粒度测试：不同用户的请求互不影响
- [ ] 添加日志记录锁获取和释放事件

---

### Requirement 3: 前端添加加载骨架屏
**Priority**: MEDIUM
**Type**: UX Enhancement

**Description**:
在充电记录列表、详情、统计页面添加加载骨架屏，提升数据加载时的用户体验。

**Scenarios**:
1. **场景 3.1**：充电记录列表加载
   - **Given**: 用户访问充电记录列表页面 `/charging-record`
   - **When**: 页面加载中，API 请求尚未返回
   - **Then**:
     - 显示表格骨架屏，包含 10 行占位符
     - 骨架屏样式与实际表格布局一致（列数、宽度）
     - 骨架屏使用 `el-skeleton` 组件，带动画效果
     - API 返回后，骨架屏淡出，实际数据淡入

2. **场景 3.2**：充电记录详情加载
   - **Given**: 用户点击"查看详情"，跳转到 `/charging-record/123`
   - **When**: 页面加载中，API 请求尚未返回
   - **Then**:
     - 显示 5 个 section 的骨架屏（状态、基本信息、充电信息、费用明细、操作）
     - 每个 section 使用 `el-skeleton` 组件，行数与实际内容一致
     - API 返回后，骨架屏替换为实际内容

3. **场景 3.3**：充电统计加载
   - **Given**: 用户访问充电统计页面 `/charging-record/statistics`
   - **When**: 页面加载中，API 请求尚未返回
   - **Then**:
     - 显示 4 个汇总卡片的骨架屏
     - 显示表格骨架屏（12 行，对应 12 个月）
     - API 返回后，骨架屏替换为实际数据

**Acceptance Criteria**:
- [ ] `ChargingRecordList.vue` 添加 `el-skeleton` 组件，rows=10
- [ ] `ChargingRecordDetail.vue` 添加 5 个 section 的骨架屏
- [ ] `ChargingRecordStatistics.vue` 添加卡片和表格骨架屏
- [ ] 骨架屏样式与实际内容布局一致
- [ ] 加载完成后，骨架屏平滑过渡到实际内容（300ms 淡入动画）

---

### Requirement 4: 前端增强错误处理
**Priority**: MEDIUM
**Type**: UX Enhancement

**Description**:
改进前端错误处理机制，添加错误边界、重试按钮、详细错误提示。

**Scenarios**:
1. **场景 4.1**：API 请求失败
   - **Given**: 用户访问充电记录列表，后端返回 500 错误
   - **When**: API 请求失败
   - **Then**:
     - 显示 `el-empty` 组件，描述为"加载失败，请稍后重试"
     - 显示"重试"按钮，点击后重新请求 API
     - 在控制台记录详细错误信息（错误码、错误消息、请求 URL）
     - 不显示原始错误堆栈给用户

2. **场景 4.2**：网络超时
   - **Given**: 用户访问充电记录详情，网络请求超时（30秒）
   - **When**: Axios 抛出 `ECONNABORTED` 错误
   - **Then**:
     - 显示 `ElMessage.error("网络请求超时，请检查网络连接")`
     - 显示"重试"按钮
     - 记录超时事件到控制台

3. **场景 4.3**：业务错误
   - **Given**: 用户尝试结束充电，但后端返回 `CHARGING_RECORD_NOT_CHARGING (4403)`
   - **When**: API 返回业务错误
   - **Then**:
     - 显示 `ElMessage.error("充电记录状态异常，无法结束充电")`
     - 不显示"重试"按钮（业务错误不应重试）
     - 记录错误到控制台

**Acceptance Criteria**:
- [ ] 所有 API 调用添加 try-catch 错误处理
- [ ] 区分网络错误、超时错误、业务错误，显示不同提示
- [ ] 列表页面添加"重试"按钮，点击后重新加载数据
- [ ] 详情页面添加"返回列表"按钮，错误时可返回
- [ ] 控制台记录详细错误信息（错误类型、错误码、请求参数）

---

### Requirement 5: 前端响应式布局优化
**Priority**: LOW
**Type**: UX Enhancement

**Description**:
优化充电记录列表表格在移动设备上的显示，避免横向滚动，提升移动端用户体验。

**Scenarios**:
1. **场景 5.1**：移动端列表显示
   - **Given**: 用户在移动设备（宽度 375px）上访问充电记录列表
   - **When**: 页面渲染
   - **Then**:
     - 表格切换为卡片布局（每条记录一个 `el-card`）
     - 卡片显示关键信息：充电桩名称、开始时间、充电量、费用、状态
     - 点击卡片跳转到详情页面
     - 无需横向滚动

2. **场景 5.2**：平板端列表显示
   - **Given**: 用户在平板设备（宽度 768px）上访问充电记录列表
   - **When**: 页面渲染
   - **Then**:
     - 表格显示简化列：充电桩、开始时间、充电量、费用、状态、操作
     - 隐藏次要列：车辆、结束时间、充电时长
     - 表格宽度自适应，无需横向滚动

3. **场景 5.3**：桌面端列表显示
   - **Given**: 用户在桌面设备（宽度 1920px）上访问充电记录列表
   - **When**: 页面渲染
   - **Then**:
     - 表格显示所有列
     - 布局与当前实现一致

**Acceptance Criteria**:
- [ ] 使用 CSS 媒体查询检测设备宽度
- [ ] 移动端（< 768px）：表格切换为卡片布局
- [ ] 平板端（768px - 1024px）：表格显示简化列
- [ ] 桌面端（> 1024px）：表格显示所有列
- [ ] 卡片布局使用 `el-card` 组件，样式与列表页一致

---

## Success Criteria

### 后端性能指标
- [ ] 充电记录列表查询响应时间从 2000ms 降低到 500ms（降低 75%）
- [ ] 数据库查询次数从 O(n) 降低到 O(1)
- [ ] 并发测试：100 个并发 `startCharging` 请求，仅 1 个成功
- [ ] 无功能回归，所有现有测试通过

### 前端用户体验指标
- [ ] 页面加载时显示骨架屏，无空白闪烁
- [ ] API 失败时显示友好错误提示和重试按钮
- [ ] 移动端列表无需横向滚动，卡片布局清晰
- [ ] 所有页面在移动端（375px）、平板端（768px）、桌面端（1920px）正常显示

### 代码质量指标
- [ ] 后端添加单元测试，覆盖 N+1 查询优化和分布式锁逻辑
- [ ] 前端添加组件测试，覆盖骨架屏和错误处理
- [ ] 代码审查通过，无明显性能或安全问题
- [ ] 文档更新，记录优化内容和使用方法

---

## Dependencies

### 后端依赖
- **Redisson**: 已集成，版本 3.x，用于分布式锁
- **Spring Data JPA**: 版本 3.2.2，支持 `@Query` with `JOIN FETCH`
- **ReservationService**: 参考其分布式锁实现模式

### 前端依赖
- **Element Plus**: 版本 2.13.1，支持 `el-skeleton` 组件
- **Vue 3**: 版本 3.5.26，支持 Composition API
- **Pinia**: 版本 3.0.4，状态管理

### 跨模块依赖
- **后端 API 契约**: 不修改 API 接口签名，保持向后兼容
- **前端类型定义**: 不修改 TypeScript 接口，保持类型一致性

---

## Risks

### 后端风险
1. **JOIN FETCH 性能风险**：如果关联表数据量大，JOIN FETCH 可能导致内存溢出
   - **缓解措施**：添加分页限制，最大 size=100
2. **分布式锁死锁风险**：如果锁未正确释放，可能导致用户无法开始充电
   - **缓解措施**：设置锁超时时间 30 秒，使用 try-finally 确保锁释放
3. **事务回滚风险**：分布式锁在事务外，可能导致锁释放但事务未提交
   - **缓解措施**：在事务提交后释放锁，或使用 `@Transactional(propagation = REQUIRES_NEW)`

### 前端风险
1. **骨架屏布局不一致**：骨架屏与实际内容布局差异大，导致布局抖动
   - **缓解措施**：精确匹配骨架屏行数和宽度
2. **移动端卡片布局性能**：大量卡片渲染可能导致性能问题
   - **缓解措施**：使用虚拟滚动或分页加载
3. **错误重试死循环**：用户频繁点击重试按钮，导致大量 API 请求
   - **缓解措施**：添加防抖，限制重试次数

---

## Implementation Notes

### 后端实施顺序
1. 修复 N+1 查询问题（优先级 HIGH）
   - 修改 `ChargingRecordRepository`，添加 `@Query` with `JOIN FETCH`
   - 修改 `ChargingRecordServiceImpl`，使用新查询方法
   - 添加单元测试验证查询次数
2. 修复并发竞态条件（优先级 HIGH）
   - 在 `startCharging` 方法中添加 Redisson 分布式锁
   - 添加并发测试验证锁机制
   - 添加日志记录锁事件

### 前端实施顺序
1. 添加加载骨架屏（优先级 MEDIUM）
   - 修改 `ChargingRecordList.vue`，添加 `el-skeleton`
   - 修改 `ChargingRecordDetail.vue`，添加 section 骨架屏
   - 修改 `ChargingRecordStatistics.vue`，添加卡片骨架屏
2. 增强错误处理（优先级 MEDIUM）
   - 修改 Store actions，添加详细错误处理
   - 修改页面组件，添加"重试"按钮
   - 添加错误日志记录
3. 响应式布局优化（优先级 LOW）
   - 添加 CSS 媒体查询
   - 实现移动端卡片布局
   - 实现平板端简化列

### 测试策略
- **后端单元测试**：使用 JUnit 5 + Mockito，覆盖 N+1 查询和分布式锁
- **后端集成测试**：使用 Testcontainers + MySQL，验证 JOIN FETCH 查询
- **前端组件测试**：使用 Vitest + Vue Test Utils，覆盖骨架屏和错误处理
- **E2E 测试**：使用 Playwright，验证移动端响应式布局

---

## Estimated Effort

- **后端优化**：2-3 个工作日
  - N+1 查询优化：1 天
  - 并发竞态修复：1 天
  - 单元测试：0.5 天
- **前端优化**：2-3 个工作日
  - 加载骨架屏：1 天
  - 错误处理增强：1 天
  - 响应式布局：1 天
- **测试与文档**：1 个工作日

**总计**：5-7 个工作日
