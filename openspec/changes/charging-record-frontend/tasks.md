# Tasks: 充电记录管理功能后端优化

## 1. 准备工作

- [ ] 1.1 创建新的 Service 类 `StartChargingTxService`（用于内层事务方法）
- [ ] 1.2 在 `pom.xml` 中验证 Redisson 依赖已存在
- [ ] 1.3 创建测试数据库和测试数据（100 条充电记录用于性能测试）
- [ ] 1.4 配置 JMeter 并发测试脚本

## 2. N+1 查询优化 - Service 层重构

- [x] 2.1 修改 `ChargingRecordServiceImpl.getChargingRecordList()` 方法，实现批量预取逻辑
- [x] 2.2 修改 `ChargingRecordServiceImpl.getChargingRecordDetail()` 方法，实现批量预取逻辑
- [x] 2.3 修改 `ChargingRecordServiceImpl.getAllChargingRecords()` 方法，实现批量预取逻辑
- [x] 2.4 提取批量预取逻辑为私有方法 `batchFetchPiles()` 和 `batchFetchVehicles()`
- [x] 2.5 添加 `@Transactional(readOnly = true)` 注解到所有查询方法

## 3. N+1 查询优化 - Controller 层验证

- [x] 3.1 在 `ChargingRecordController.getChargingRecordList()` 方法中添加分页参数验证（`@Min(1) @Max(100)`）
- [x] 3.2 在 `ChargingRecordController.getAllChargingRecords()` 方法中添加分页参数验证
- [x] 3.3 添加全局异常处理，捕获 `ConstraintViolationException` 并返回 400 错误

## 4. N+1 查询优化 - 单元测试

- [ ] 4.1 创建 `ChargingRecordServiceImplTest` 测试类
- [ ] 4.2 添加测试：`testGetChargingRecordList_BatchFetch()`，验证查询次数 ≤ 3
- [ ] 4.3 添加测试：`testGetChargingRecordDetail_BatchFetch()`，验证查询次数 ≤ 2
- [ ] 4.4 添加测试：`testGetAllChargingRecords_BatchFetch()`，验证查询次数 ≤ 3
- [ ] 4.5 使用 Mockito 验证 Repository 方法调用次数

## 5. N+1 查询优化 - 性能测试

- [ ] 5.1 使用 JMeter 测试优化前的响应时间（size=10, 100 次请求）
- [ ] 5.2 部署优化后的代码到测试环境
- [ ] 5.3 使用 JMeter 测试优化后的响应时间（size=10, 100 次请求）
- [ ] 5.4 验证响应时间降低 60% 以上
- [ ] 5.5 使用数据库慢查询日志验证查询次数 ≤ 3

## 6. 并发竞态修复 - 分布式锁实现

- [x] 6.1 创建 `StartChargingTxService` 类，添加 `@Service` 和 `@Transactional` 注解
- [x] 6.2 将 `ChargingRecordServiceImpl.startCharging()` 的事务逻辑移动到 `StartChargingTxService.startChargingInTx()`
- [x] 6.3 在 `ChargingRecordServiceImpl.startCharging()` 中添加 Redisson 锁获取逻辑（userId 锁）
- [x] 6.4 在 `ChargingRecordServiceImpl.startCharging()` 中添加 Redisson 锁获取逻辑（pileId 锁）
- [x] 6.5 实现锁获取顺序：先 userId 锁，后 pileId 锁
- [x] 6.6 实现锁释放顺序：先 pileId 锁，后 userId 锁（在 finally 块中）
- [x] 6.7 配置锁参数：waitTime=5秒，leaseTime=30秒

## 7. 并发竞态修复 - 错误处理

- [x] 7.1 添加锁获取超时处理，返回 `SYSTEM_BUSY` 错误
- [x] 7.2 添加锁获取失败后的状态重新检查逻辑
- [x] 7.3 添加 `InterruptedException` 处理，恢复中断标志
- [x] 7.4 添加 Redis 连接异常处理，返回 `SYSTEM_ERROR` 错误
- [x] 7.5 在 `ResultCode` 枚举中添加 `SYSTEM_BUSY` 和 `CHARGING_PILE_BUSY` 错误码

## 8. 并发竞态修复 - 日志和监控

- [x] 8.1 添加锁获取成功日志：`log.info("Acquired lock for user {} and pile {}", userId, pileId)`
- [x] 8.2 添加锁获取失败日志：`log.warn("Failed to acquire lock for user {}, reason: {}", userId, reason)`
- [x] 8.3 添加锁释放日志：`log.info("Released lock for user {} and pile {}", userId, pileId)`
- [ ] 8.4 添加锁持有时间监控（使用 Micrometer 或 Prometheus）
- [ ] 8.5 添加锁获取失败率监控

## 9. 并发竞态修复 - 单元测试

- [ ] 9.1 创建 `StartChargingConcurrencyTest` 测试类
- [ ] 9.2 添加测试：`testStartCharging_ConcurrentRequests_SameUser()`，验证仅 1 个成功
- [ ] 9.3 添加测试：`testStartCharging_ConcurrentRequests_SamePile()`，验证仅 1 个成功
- [ ] 9.4 添加测试：`testStartCharging_ConcurrentRequests_DifferentUsers()`，验证都成功
- [ ] 9.5 添加测试：`testStartCharging_LockTimeout()`，验证返回 `SYSTEM_BUSY`
- [ ] 9.6 使用 Mockito 模拟 Redisson 锁行为

## 10. 并发竞态修复 - 集成测试

- [ ] 10.1 创建 `StartChargingIntegrationTest` 测试类（使用 `@SpringBootTest`）
- [ ] 10.2 启动嵌入式 Redis（使用 Testcontainers 或 embedded-redis）
- [ ] 10.3 添加测试：使用 `ExecutorService` 模拟 10 个并发请求（同一用户）
- [ ] 10.4 验证数据库中仅存在 1 条 status=CHARGING 的记录
- [ ] 10.5 验证 9 个请求返回 `USER_ALREADY_CHARGING` 错误

## 11. 并发竞态修复 - 性能测试

- [ ] 11.1 使用 JMeter 模拟 100 个并发 `startCharging` 请求（同一用户，同一充电桩）
- [ ] 11.2 验证仅 1 个请求成功，99 个请求返回错误
- [ ] 11.3 验证数据库中仅存在 1 条 status=CHARGING 的记录
- [ ] 11.4 使用 JMeter 模拟 100 个并发 `startCharging` 请求（100 个不同用户，100 个不同充电桩）
- [ ] 11.5 验证所有请求都成功
- [ ] 11.6 验证锁获取时间 < 100ms（P95）

## 12. 数据库索引优化

- [ ] 12.1 添加复合索引：`idx_user_status_start_time` (user_id, status, start_time)
- [ ] 12.2 添加复合索引：`idx_pile_status_start_time` (charging_pile_id, status, start_time)
- [ ] 12.3 使用 `EXPLAIN` 验证查询计划使用索引
- [ ] 12.4 创建数据库迁移脚本（Flyway 或 Liquibase）

## 13. 代码审查和文档

- [ ] 13.1 代码自审：检查所有修改的方法，确保无明显问题
- [ ] 13.2 更新 JavaDoc 注释，说明批量预取逻辑和分布式锁机制
- [ ] 13.3 更新 `CLAUDE.md` 文档，记录优化内容
- [ ] 13.4 创建 Pull Request，添加详细的变更说明
- [ ] 13.5 邀请团队成员进行 Code Review

## 14. 部署和验证

- [ ] 14.1 在测试环境部署优化后的代码
- [ ] 14.2 执行冒烟测试：手动测试开始充电、结束充电、查询列表、查询详情
- [ ] 14.3 执行性能测试：验证响应时间和查询次数
- [ ] 14.4 执行并发测试：验证锁机制正确性
- [ ] 14.5 监控测试环境 1 小时，确认无异常日志和错误
- [ ] 14.6 灰度发布到生产环境（10% 流量）
- [ ] 14.7 监控生产环境 2 小时，确认无异常
- [ ] 14.8 全量发布到生产环境
- [ ] 14.9 监控生产环境 24 小时，确认优化效果

## 15. 回滚准备

- [ ] 15.1 准备回滚脚本（回滚到上一版本）
- [ ] 15.2 准备数据库索引回滚脚本（删除新增索引）
- [ ] 15.3 文档化回滚步骤和决策标准
- [ ] 15.4 在测试环境验证回滚流程
