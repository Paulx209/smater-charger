# Design: 充电记录管理功能优化

## Context

### Background
充电记录管理功能已完整实现（后端 8 个 REST API 端点 + 前端 3 个页面组件），但存在严重的性能问题和用户体验问题：

**后端性能瓶颈**：
- **N+1 查询问题**：`ChargingRecordServiceImpl` 在查询列表/详情时，对每条记录单独查询 `ChargingPile` 和 `Vehicle`，导致 100 条记录产生 200+ 次数据库查询
- **并发竞态条件**：`startCharging` 方法在检查"用户无活跃充电记录"和创建新记录之间存在时间窗口，可能导致同一用户创建多个充电记录

**前端用户体验问题**：
- 数据加载时显示空白，缺少加载骨架屏
- API 失败时仅显示简单错误消息，缺少重试机制
- 表格在移动设备上需要横向滚动

### Current State
- **实体模型**：`ChargingRecord` 仅包含 FK 字段（`userId`, `chargingPileId`, `vehicleId`），无 `@ManyToOne` 关联
- **查询模式**：使用派生查询方法（`findByUserId`, `findByUserIdAndStatus`），然后在 Service 层循环调用 `chargingPileRepository.findById()` 和 `vehicleRepository.findById()`
- **并发控制**：无分布式锁，仅依赖数据库事务隔离级别（READ_COMMITTED）
- **前端加载**：使用 `v-loading` 指令显示 spinner，无骨架屏

### Constraints
- **不修改 API 契约**：保持向后兼容，不修改接口签名和响应结构
- **不重构实体模型**：避免添加 `@ManyToOne` 关联，保持当前 FK-only 模式
- **使用现有技术栈**：Redisson 3.x（已集成）、Spring Data JPA 3.2.2、Element Plus 2.13.1
- **事务一致性**：`startCharging` 和 `endCharging` 必须保持事务一致性（ChargingRecord + ChargingPile + Reservation）

### Stakeholders
- **后端开发者**：需要理解 DTO 投影和分布式锁实现
- **前端开发者**：需要理解骨架屏和错误处理模式
- **运维团队**：需要监控 Redis 锁和数据库查询性能
- **最终用户**：期望更快的响应时间和更好的加载体验

---

## Goals / Non-Goals

### Goals
1. **后端性能优化**：
   - 将充电记录列表查询响应时间从 2000ms 降低到 500ms（降低 75%）
   - 将数据库查询次数从 O(n) 降低到 O(1)（3 次查询解决 N+1 问题）
   - 防止并发创建充电记录，确保每用户/每充电桩仅一个活跃充电

2. **前端用户体验提升**：
   - 添加加载骨架屏，消除空白闪烁
   - 增强错误处理，提供重试按钮和详细错误提示
   - 优化移动端布局，避免横向滚动

3. **代码质量**：
   - 添加单元测试覆盖新逻辑
   - 保持向后兼容，无功能回归
   - 文档化技术决策和实现模式

### Non-Goals
- **不重构实体模型**：不添加 `@ManyToOne` 关联，保持 FK-only 模式
- **不修改 API 契约**：不修改接口签名、响应结构、错误码
- **不实现新功能**：仅优化现有功能，不添加新业务逻辑
- **不优化其他模块**：仅优化充电记录模块，不涉及预约、车辆等模块
- **不实现 WebSocket**：前端错误处理使用轮询，不引入 WebSocket

---

## Decisions

### Decision 1: 数据加载策略 - DTO 投影 + 批量预取

**选择**：使用 DTO 投影 + 两步批量预取（而非 JOIN FETCH）

**理由**：
- **当前实体模型无关联**：`ChargingRecord` 仅有 FK 字段，使用 JOIN FETCH 需要先添加 `@ManyToOne` 关联，重构成本高
- **性能可预测**：两步查询（1 次主查询 + 2 次 IN 查询）比 N+1 查询快 10 倍以上，且查询次数固定为 3
- **内存占用低**：DTO 投影仅加载必需字段，避免 JOIN FETCH 可能导致的笛卡尔积和内存膨胀
- **易于维护**：无需修改实体模型，仅在 Service 层添加批量预取逻辑

**替代方案**：
- **方案 A**：添加 `@ManyToOne` 关联 + JOIN FETCH
  - **优点**：单次查询，代码简洁
  - **缺点**：需要重构实体模型，可能影响现有逻辑，风险高
- **方案 B**：使用 `@EntityGraph`
  - **优点**：比 JOIN FETCH 更灵活
  - **缺点**：仍需添加关联，且对复杂筛选支持有限
- **方案 C**：数据库视图 + 单表查询
  - **优点**：查询最简单
  - **缺点**：需要 DBA 支持，维护成本高

**实现模式**：
```java
// Step 1: 查询充电记录（仅 FK 字段）
Page<ChargingRecord> recordPage = chargingRecordRepository.findByFilters(...);
List<ChargingRecord> records = recordPage.getContent();

// Step 2: 批量预取充电桩
Set<Long> pileIds = records.stream().map(ChargingRecord::getChargingPileId).collect(toSet());
Map<Long, ChargingPile> pileMap = chargingPileRepository.findAllById(pileIds)
    .stream().collect(toMap(ChargingPile::getId, Function.identity()));

// Step 3: 批量预取车辆
Set<Long> vehicleIds = records.stream().map(ChargingRecord::getVehicleId).filter(Objects::nonNull).collect(toSet());
Map<Long, Vehicle> vehicleMap = vehicleRepository.findAllById(vehicleIds)
    .stream().collect(toMap(Vehicle::getId, Function.identity()));

// Step 4: 组装响应
List<ChargingRecordResponse> responses = records.stream()
    .map(record -> convertToResponse(record, pileMap.get(record.getChargingPileId()), vehicleMap.get(record.getVehicleId())))
    .collect(toList());
```

---

### Decision 2: 分布式锁粒度 - userId + pileId 双重锁

**选择**：使用 Redisson `RLock`，锁 key 为 `charging:start:{userId}` 和 `charging:pile:{pileId}`

**理由**：
- **保证双重唯一性**：同时防止"同一用户多个活跃充电"和"同一充电桩多个活跃充电"
- **避免死锁**：按固定顺序获取锁（先 userId 锁，后 pileId 锁），避免循环等待
- **性能可接受**：两个锁的获取时间 < 10ms，对用户体验影响小

**替代方案**：
- **方案 A**：仅 userId 锁
  - **优点**：实现简单
  - **缺点**：无法防止多个用户同时使用同一充电桩
- **方案 B**：数据库悲观锁（SELECT ... FOR UPDATE）
  - **优点**：无 Redis 依赖
  - **缺点**：锁竞争时性能差，可能导致死锁
- **方案 C**：数据库唯一约束
  - **优点**：最终一致性保障
  - **缺点**：仅能检测冲突，无法预防，用户体验差

**实现模式**：
```java
// 外层方法：获取锁
public ChargingRecordResponse startCharging(Long userId, ChargingRecordStartRequest request) {
    RLock userLock = redissonClient.getLock("charging:start:" + userId);
    RLock pileLock = redissonClient.getLock("charging:pile:" + request.getChargingPileId());

    try {
        // 按固定顺序获取锁，避免死锁
        boolean userLocked = userLock.tryLock(5, 30, TimeUnit.SECONDS);
        if (!userLocked) {
            throw new BusinessException(ResultCode.SYSTEM_BUSY);
        }

        boolean pileLocked = pileLock.tryLock(5, 30, TimeUnit.SECONDS);
        if (!pileLocked) {
            throw new BusinessException(ResultCode.CHARGING_PILE_BUSY);
        }

        // 调用内层事务方法
        return startChargingTxService.startChargingInTx(userId, request);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new BusinessException(ResultCode.SYSTEM_ERROR);
    } finally {
        if (pileLock.isHeldByCurrentThread()) {
            pileLock.unlock();
        }
        if (userLock.isHeldByCurrentThread()) {
            userLock.unlock();
        }
    }
}

// 内层方法：事务操作
@Transactional
public ChargingRecordResponse startChargingInTx(Long userId, ChargingRecordStartRequest request) {
    // 1. 重新检查用户无活跃充电记录
    Optional<ChargingRecord> existingRecord = chargingRecordRepository.findByUserIdAndStatus(userId, CHARGING);
    if (existingRecord.isPresent()) {
        throw new BusinessException(ResultCode.USER_ALREADY_CHARGING);
    }

    // 2. 验证充电桩状态
    ChargingPile pile = chargingPileRepository.findById(request.getChargingPileId())
        .orElseThrow(() -> new BusinessException(ResultCode.CHARGING_PILE_NOT_FOUND));
    if (pile.getStatus() == ChargingPileStatus.CHARGING) {
        throw new BusinessException(ResultCode.CHARGING_PILE_NOT_IDLE);
    }

    // 3. 创建充电记录 + 更新充电桩状态 + 更新预约状态
    // ... (现有逻辑)
}
```

---

### Decision 3: 事务边界 - 外层锁 + 内层事务

**选择**：使用外层非事务方法获取锁，内层代理事务方法执行数据库操作

**理由**：
- **避免锁释放时机问题**：如果在同一方法中获取锁和开启事务，锁可能在事务提交前释放，导致并发问题
- **确保事务完整性**：内层方法使用 `@Transactional`，Spring AOP 代理确保事务在方法返回后提交
- **锁持有时间最小化**：锁仅在业务逻辑执行期间持有，事务提交后立即释放

**替代方案**：
- **方案 A**：锁和事务在同一方法
  - **优点**：代码简洁
  - **缺点**：锁可能在事务提交前释放，导致并发问题
- **方案 B**：使用 `TransactionTemplate` 手动管理事务
  - **优点**：精确控制事务边界
  - **缺点**：代码复杂，可读性差

---

### Decision 4: 分页限制 - 强制最大 size=100

**选择**：在 Controller 层验证 `size` 参数，强制 `size <= 100` 且 `page >= 1`

**理由**：
- **防止内存溢出**：批量预取时，如果 `size` 过大，可能导致内存溢出
- **防止性能问题**：大分页查询会导致数据库和应用服务器性能下降
- **用户体验**：100 条记录已足够满足用户需求，更大的分页无实际意义

**实现模式**：
```java
@GetMapping
public Result<Page<ChargingRecordResponse>> getChargingRecordList(
        @RequestParam(required = false) ChargingRecordStatus status,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
        @RequestParam(defaultValue = "1") @Min(1) Integer page,
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {
    // ...
}
```

---

### Decision 5: 前端骨架屏 - Element Plus `el-skeleton`

**选择**：使用 Element Plus 官方 `el-skeleton` 组件

**理由**：
- **官方支持**：Element Plus 2.13.1 原生支持，文档完善，无需额外依赖
- **易于维护**：组件 API 稳定，升级 Element Plus 时自动兼容
- **与现有风格一致**：骨架屏样式与 Element Plus 其他组件一致，视觉统一
- **性能好**：使用 CSS 动画，无 JS 计算，性能优于自定义实现

**替代方案**：
- **方案 A**：自定义骨架屏组件
  - **优点**：完全可控，样式灵活
  - **缺点**：需要额外开发和维护，升级 Element Plus 时可能不兼容
- **方案 B**：第三方骨架屏库（如 vue-content-loader）
  - **优点**：功能丰富
  - **缺点**：增加依赖，可能与 Element Plus 样式冲突

**实现模式**：
```vue
<template>
  <el-card v-loading="loading">
    <el-skeleton v-if="loading" :rows="10" animated />
    <el-table v-else :data="chargingRecords">
      <!-- 表格列 -->
    </el-table>
  </el-card>
</template>
```

---

### Decision 6: 前端错误处理 - 统一错误处理 + 重试按钮

**选择**：在 Pinia Store actions 中统一处理错误，页面组件显示重试按钮

**理由**：
- **代码复用**：错误处理逻辑集中在 Store，所有组件共享
- **用户体验**：区分网络错误、超时错误、业务错误，显示不同提示和操作
- **易于维护**：错误处理逻辑变更时，仅需修改 Store，无需修改所有组件

**实现模式**：
```typescript
// Store action
const fetchChargingRecordList = async (params?: ChargingRecordQueryParams) => {
  try {
    loading.value = true
    const result = await getChargingRecordList(params)
    chargingRecords.value = result.content
    total.value = result.totalElements
  } catch (error: any) {
    console.error('获取充电记录列表失败:', error)

    // 区分错误类型
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('网络请求超时，请检查网络连接')
    } else if (error.response?.status >= 500) {
      ElMessage.error('服务器错误，请稍后重试')
    } else {
      ElMessage.error(error.message || '加载失败，请稍后重试')
    }

    throw error // 抛出错误，让组件处理重试逻辑
  } finally {
    loading.value = false
  }
}

// 组件
<template>
  <el-empty v-if="error" description="加载失败">
    <el-button type="primary" @click="handleRetry">重试</el-button>
  </el-empty>
</template>
```

---

### Decision 7: 前端响应式布局 - CSS 媒体查询 + 条件渲染

**选择**：使用 CSS 媒体查询检测设备宽度，条件渲染表格或卡片布局

**理由**：
- **原生支持**：CSS 媒体查询是 Web 标准，无需额外依赖
- **性能好**：浏览器原生支持，无 JS 计算开销
- **易于维护**：断点清晰，逻辑简单

**实现模式**：
```vue
<template>
  <!-- 桌面端：完整表格 -->
  <el-table v-if="!isMobile" :data="chargingRecords" class="desktop-table">
    <!-- 所有列 -->
  </el-table>

  <!-- 移动端：卡片布局 -->
  <div v-else class="mobile-cards">
    <el-card v-for="record in chargingRecords" :key="record.id" class="record-card">
      <div class="card-header">{{ record.pileName }}</div>
      <div class="card-body">
        <div>开始时间：{{ formatDate(record.startTime) }}</div>
        <div>充电量：{{ record.electricQuantity }} 度</div>
        <div>费用：¥{{ record.fee }}</div>
        <el-tag :type="getStatusType(record.status)">{{ getStatusText(record.status) }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
@media (max-width: 768px) {
  .desktop-table {
    display: none;
  }
}

@media (min-width: 769px) {
  .mobile-cards {
    display: none;
  }
}
</style>
```

---

## Risks / Trade-offs

### Risk 1: 批量预取可能导致内存溢出
**描述**：如果用户请求 `size=100`，批量预取 100 个充电桩和 100 个车辆，可能导致内存占用过高。

**缓解措施**：
- 强制 `size <= 100`，在 Controller 层验证
- 使用 DTO 投影，仅加载必需字段，减少内存占用
- 监控 JVM 堆内存使用情况，设置告警阈值

---

### Risk 2: 分布式锁超时可能导致用户体验差
**描述**：如果 Redis 负载高，锁获取可能超时（5 秒），用户需要等待较长时间。

**缓解措施**：
- 监控锁获取时间，设置告警阈值（> 1 秒）
- 优化 Redis 配置，增加连接池大小
- 在锁获取失败时，返回友好错误提示："系统繁忙，请稍后重试"

---

### Risk 3: Redis 故障可能导致无法开始充电
**描述**：如果 Redis 不可用，分布式锁无法获取，所有 `startCharging` 请求都会失败。

**缓解措施**：
- 添加数据库唯一约束作为最终一致性保障（生成列 + 唯一索引）
- 实现 Redis 健康检查，故障时自动降级到数据库锁
- 监控 Redis 可用性，设置告警

---

### Risk 4: 骨架屏布局与实际内容不一致
**描述**：如果骨架屏行数或宽度与实际内容差异大，可能导致布局抖动。

**缓解措施**：
- 精确匹配骨架屏行数和宽度
- 使用 CSS 过渡动画，平滑切换骨架屏和实际内容
- 在开发环境测试不同数据量下的布局效果

---

### Risk 5: 移动端卡片布局性能问题
**描述**：如果充电记录数量很多（100 条），卡片渲染可能导致性能问题。

**缓解措施**：
- 使用虚拟滚动（如 `vue-virtual-scroller`）
- 限制移动端分页大小（默认 10 条）
- 使用 CSS `will-change` 优化渲染性能

---

## Migration Plan

### Phase 1: 后端 N+1 查询优化（1-2 天）
1. **修改 Service 层**：
   - 在 `ChargingRecordServiceImpl` 中添加批量预取逻辑
   - 修改 `getChargingRecordList`、`getChargingRecordDetail`、`getAllChargingRecords` 方法
2. **添加单元测试**：
   - 测试批量预取逻辑
   - 验证查询次数 ≤ 3
3. **性能测试**：
   - 使用 JMeter 测试响应时间
   - 验证响应时间降低 60% 以上
4. **部署**：
   - 灰度发布，监控错误率和响应时间
   - 无问题后全量发布

### Phase 2: 后端并发竞态修复（1 天）
1. **添加分布式锁**：
   - 在 `ChargingRecordServiceImpl` 中添加外层锁方法
   - 创建内层事务方法 `startChargingInTx`
2. **添加并发测试**：
   - 使用 JMeter 模拟 100 个并发请求
   - 验证仅 1 个成功，99 个返回 `USER_ALREADY_CHARGING`
3. **添加监控**：
   - 监控锁获取时间和失败率
   - 设置告警阈值
4. **部署**：
   - 灰度发布，监控锁性能
   - 无问题后全量发布

### Phase 3: 前端骨架屏（1 天）
1. **修改页面组件**：
   - 在 `ChargingRecordList.vue`、`ChargingRecordDetail.vue`、`ChargingRecordStatistics.vue` 中添加 `el-skeleton`
2. **测试**：
   - 在不同网络速度下测试加载效果
   - 验证骨架屏与实际内容布局一致
3. **部署**：
   - 构建生产版本
   - 部署到 CDN

### Phase 4: 前端错误处理（1 天）
1. **修改 Store actions**：
   - 在 `chargingRecordStore` 中添加统一错误处理
2. **修改页面组件**：
   - 添加"重试"按钮
   - 添加错误状态显示
3. **测试**：
   - 模拟网络错误、超时错误、业务错误
   - 验证错误提示和重试功能
4. **部署**：
   - 构建生产版本
   - 部署到 CDN

### Phase 5: 前端响应式布局（1 天）
1. **修改 `ChargingRecordList.vue`**：
   - 添加 CSS 媒体查询
   - 实现移动端卡片布局
2. **测试**：
   - 在移动端（375px）、平板端（768px）、桌面端（1920px）测试
   - 验证无横向滚动
3. **部署**：
   - 构建生产版本
   - 部署到 CDN

### Rollback Strategy
- **后端**：如果出现严重性能问题或数据不一致，立即回滚到上一版本
- **前端**：如果出现严重 UI 问题，立即回滚到上一版本
- **数据库**：无 schema 变更，无需回滚

---

## Open Questions

### Q1: 是否需要添加数据库唯一约束作为最终一致性保障？
**背景**：Codex 建议添加生成列 + 唯一索引，防止 Redis 故障时创建重复充电记录。

**选项**：
- **A**：添加唯一约束（推荐）
- **B**：仅依赖 Redis 锁

**决策**：待用户确认

---

### Q2: 是否需要实现 Redis 健康检查和降级策略？
**背景**：如果 Redis 不可用，分布式锁无法获取，所有 `startCharging` 请求都会失败。

**选项**：
- **A**：实现健康检查，故障时降级到数据库锁
- **B**：仅监控 Redis 可用性，故障时人工介入

**决策**：待用户确认

---

### Q3: 前端是否需要实现虚拟滚动优化移动端性能？
**背景**：如果充电记录数量很多（100 条），移动端卡片渲染可能导致性能问题。

**选项**：
- **A**：实现虚拟滚动（需要引入 `vue-virtual-scroller`）
- **B**：限制移动端分页大小（默认 10 条）

**决策**：待用户确认

---

### Q4: 是否需要添加后端集成测试？
**背景**：当前仅有单元测试计划，缺少集成测试验证完整流程。

**选项**：
- **A**：添加集成测试（使用 Testcontainers + MySQL）
- **B**：仅依赖单元测试和手动测试

**决策**：待用户确认
