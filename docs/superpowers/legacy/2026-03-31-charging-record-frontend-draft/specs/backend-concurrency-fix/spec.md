# Spec: 修复后端并发竞态条件

## ADDED Requirements

### Requirement: 分布式锁防止并发创建充电记录
系统 SHALL 使用 Redisson 分布式锁防止同一用户在短时间内创建多个充电记录。

#### Scenario: 并发请求开始充电（同一用户）
- **WHEN** 用户 userId=1 同时发送 2 个 `startCharging` 请求（chargingPileId=10, vehicleId=5），时间差 < 100ms
- **THEN** 第一个请求获取分布式锁（key=`charging:start:1`），成功创建充电记录
- **THEN** 第二个请求等待锁释放后，检查发现用户已有活跃充电记录
- **THEN** 第二个请求返回错误 `USER_ALREADY_CHARGING (4402)`
- **THEN** 数据库中仅存在 1 条 status=CHARGING 的记录（userId=1）

#### Scenario: 并发请求开始充电（不同用户）
- **WHEN** 用户 userId=1 和 userId=2 同时发送 `startCharging` 请求，时间差 < 100ms
- **THEN** 两个请求使用不同的锁 key（`charging:start:1` 和 `charging:start:2`）
- **THEN** 两个请求可以并发执行，互不影响
- **THEN** 两个用户都能成功创建充电记录

#### Scenario: 锁获取超时
- **WHEN** 用户 userId=1 发送 `startCharging` 请求，但锁已被其他请求持有超过 5 秒
- **THEN** 系统返回错误 `SYSTEM_BUSY`，错误消息为"系统繁忙，请稍后重试"
- **THEN** 不创建充电记录

#### Scenario: 锁释放时机
- **WHEN** 用户 userId=1 成功开始充电
- **THEN** 系统在事务提交后释放锁
- **THEN** 其他请求可以立即获取锁

---

### Requirement: 充电桩级别并发控制
系统 SHALL 使用分布式锁防止多个用户同时使用同一充电桩。

#### Scenario: 并发请求开始充电（同一充电桩）
- **WHEN** 用户 userId=1 和 userId=2 同时发送 `startCharging` 请求（chargingPileId=10），时间差 < 100ms
- **THEN** 第一个请求获取充电桩锁（key=`charging:pile:10`），成功创建充电记录
- **THEN** 第二个请求等待锁释放后，检查发现充电桩状态为 CHARGING
- **THEN** 第二个请求返回错误 `CHARGING_PILE_NOT_IDLE (4202)`
- **THEN** 数据库中仅存在 1 条 status=CHARGING 的记录（chargingPileId=10）

#### Scenario: 并发请求开始充电（不同充电桩）
- **WHEN** 用户 userId=1 和 userId=2 同时发送 `startCharging` 请求（chargingPileId=10 和 chargingPileId=20），时间差 < 100ms
- **THEN** 两个请求使用不同的锁 key（`charging:pile:10` 和 `charging:pile:20`）
- **THEN** 两个请求可以并发执行，互不影响
- **THEN** 两个用户都能成功创建充电记录

---

### Requirement: 锁获取顺序防止死锁
系统 SHALL 按固定顺序获取锁（先 userId 锁，后 pileId 锁），防止死锁。

#### Scenario: 按固定顺序获取锁
- **WHEN** 用户 userId=1 发送 `startCharging` 请求（chargingPileId=10）
- **THEN** 系统先获取 userId 锁（key=`charging:start:1`）
- **THEN** 系统再获取 pileId 锁（key=`charging:pile:10`）
- **THEN** 系统执行业务逻辑
- **THEN** 系统按相反顺序释放锁（先释放 pileId 锁，后释放 userId 锁）

#### Scenario: 锁获取失败时的清理
- **WHEN** 用户 userId=1 发送 `startCharging` 请求，成功获取 userId 锁，但 pileId 锁获取失败
- **THEN** 系统释放 userId 锁
- **THEN** 系统返回错误 `CHARGING_PILE_BUSY`

---

### Requirement: 事务边界与锁释放时机
系统 SHALL 确保锁在事务提交后释放，防止锁释放时机问题。

#### Scenario: 锁在事务提交后释放
- **WHEN** 用户 userId=1 成功开始充电
- **THEN** 系统在外层方法中获取锁
- **THEN** 系统在内层事务方法中执行数据库操作（创建充电记录 + 更新充电桩状态 + 更新预约状态）
- **THEN** 系统在内层事务方法返回后（事务已提交），释放锁
- **THEN** 其他请求获取锁后，能够看到已提交的数据

#### Scenario: 事务回滚时的锁释放
- **WHEN** 用户 userId=1 发送 `startCharging` 请求，但业务逻辑抛出异常（如充电桩不存在）
- **THEN** 系统回滚事务
- **THEN** 系统在 finally 块中释放锁
- **THEN** 其他请求可以立即获取锁

---

### Requirement: 锁超时配置
系统 SHALL 配置合理的锁超时时间，防止锁永久持有。

#### Scenario: 锁等待超时
- **WHEN** 用户 userId=1 发送 `startCharging` 请求，但锁已被其他请求持有
- **THEN** 系统等待最多 5 秒尝试获取锁
- **THEN** 如果 5 秒后仍未获取到锁，系统返回错误 `SYSTEM_BUSY`

#### Scenario: 锁租约超时
- **WHEN** 用户 userId=1 成功获取锁，但业务逻辑执行时间超过 30 秒
- **THEN** 锁自动过期释放
- **THEN** 其他请求可以获取锁
- **THEN** 原请求继续执行，但可能导致数据不一致（需要监控和告警）

---

### Requirement: 锁失败时的业务逻辑
系统 SHALL 在锁获取失败时，返回友好的错误提示。

#### Scenario: 锁获取超时后重新检查状态
- **WHEN** 用户 userId=1 发送 `startCharging` 请求，锁获取超时
- **THEN** 系统重新检查用户是否已有活跃充电记录
- **THEN** 如果有活跃充电记录，系统返回 `USER_ALREADY_CHARGING (4402)`
- **THEN** 如果无活跃充电记录，系统返回 `SYSTEM_BUSY`

#### Scenario: 锁获取失败的日志记录
- **WHEN** 用户 userId=1 发送 `startCharging` 请求，锁获取失败
- **THEN** 系统记录日志：`[WARN] Failed to acquire lock for user 1, reason: timeout`
- **THEN** 系统记录锁等待时间和失败原因

---

### Requirement: Redis 故障时的降级策略
系统 SHALL 在 Redis 不可用时，拒绝开始充电请求，防止数据不一致。

#### Scenario: Redis 不可用时拒绝请求
- **WHEN** 用户 userId=1 发送 `startCharging` 请求，但 Redis 不可用
- **THEN** 系统捕获 Redis 连接异常
- **THEN** 系统返回错误 `SYSTEM_ERROR`，错误消息为"系统维护中，请稍后重试"
- **THEN** 系统记录告警日志：`[ERROR] Redis unavailable, rejecting startCharging request`

#### Scenario: Redis 恢复后正常处理
- **WHEN** Redis 恢复可用后，用户 userId=1 发送 `startCharging` 请求
- **THEN** 系统正常获取锁并处理请求

---

### Requirement: 并发测试验证
系统 SHALL 通过并发测试验证锁机制的正确性。

#### Scenario: 100 个并发请求测试
- **WHEN** 使用 JMeter 模拟 100 个并发 `startCharging` 请求（同一用户 userId=1，同一充电桩 chargingPileId=10）
- **THEN** 仅 1 个请求成功创建充电记录
- **THEN** 99 个请求返回错误 `USER_ALREADY_CHARGING (4402)` 或 `CHARGING_PILE_NOT_IDLE (4202)`
- **THEN** 数据库中仅存在 1 条 status=CHARGING 的记录

#### Scenario: 不同用户并发请求测试
- **WHEN** 使用 JMeter 模拟 100 个并发 `startCharging` 请求（100 个不同用户，100 个不同充电桩）
- **THEN** 所有请求都能成功创建充电记录
- **THEN** 数据库中存在 100 条 status=CHARGING 的记录

---

### Requirement: 监控和告警
系统 SHALL 监控锁性能和失败率，设置告警阈值。

#### Scenario: 锁获取时间监控
- **WHEN** 系统运行时
- **THEN** 系统记录每次锁获取的时间
- **THEN** 如果锁获取时间 > 1 秒，系统发送告警

#### Scenario: 锁获取失败率监控
- **WHEN** 系统运行时
- **THEN** 系统记录锁获取失败的次数和原因
- **THEN** 如果锁获取失败率 > 5%，系统发送告警

#### Scenario: Redis 可用性监控
- **WHEN** 系统运行时
- **THEN** 系统定期检查 Redis 连接状态
- **THEN** 如果 Redis 不可用，系统发送告警
