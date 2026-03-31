# Spec: 修复后端 N+1 查询问题

## ADDED Requirements

### Requirement: 充电记录列表查询优化
系统 SHALL 使用批量预取策略优化充电记录列表查询，将数据库查询次数从 O(n) 降低到 O(1)（固定 3 次查询）。

#### Scenario: 查询充电记录列表（带筛选）
- **WHEN** 用户请求充电记录列表，筛选条件为 status=COMPLETED，日期范围 2026-01-01 到 2026-01-31，page=1，size=10
- **THEN** 系统执行以下查询：
  - 第 1 次查询：查询充电记录（仅 FK 字段），返回 10 条记录
  - 第 2 次查询：批量查询充电桩（IN 查询，最多 10 个 ID）
  - 第 3 次查询：批量查询车辆（IN 查询，最多 10 个 ID）
- **THEN** 系统返回 `Page<ChargingRecordResponse>` 包含 10 条记录，每条记录包含完整的充电桩和车辆信息
- **THEN** 响应时间 < 500ms（相比当前 2000ms+）

#### Scenario: 查询充电记录列表（无筛选）
- **WHEN** 用户请求充电记录列表，无筛选条件，page=1，size=20
- **THEN** 系统执行 3 次查询（主查询 + 充电桩批量查询 + 车辆批量查询）
- **THEN** 系统返回 `Page<ChargingRecordResponse>` 包含 20 条记录
- **THEN** 数据库查询次数 = 3（不随记录数量增加）

#### Scenario: 查询充电记录列表（大分页）
- **WHEN** 用户请求充电记录列表，page=1，size=100
- **THEN** 系统执行 3 次查询
- **THEN** 系统返回 `Page<ChargingRecordResponse>` 包含最多 100 条记录
- **THEN** 响应时间 < 1000ms

#### Scenario: 查询充电记录列表（超过最大分页限制）
- **WHEN** 用户请求充电记录列表，page=1，size=200
- **THEN** 系统返回 400 错误，错误消息为"分页大小不能超过 100"

---

### Requirement: 充电记录详情查询优化
系统 SHALL 使用批量预取策略优化充电记录详情查询，将数据库查询次数降低到 ≤ 2 次。

#### Scenario: 查询充电记录详情（充电中）
- **WHEN** 用户请求充电记录详情，recordId=123，status=CHARGING
- **THEN** 系统执行以下查询：
  - 第 1 次查询：查询充电记录 + 充电桩 + 车辆（使用批量预取或 JOIN）
- **THEN** 系统返回 `ChargingRecordResponse`，包含完整的充电桩和车辆信息
- **THEN** 响应时间 < 200ms

#### Scenario: 查询充电记录详情（已完成）
- **WHEN** 用户请求充电记录详情，recordId=123，status=COMPLETED
- **THEN** 系统执行以下查询：
  - 第 1 次查询：查询充电记录 + 充电桩 + 车辆
  - 第 2 次查询：查询 PriceConfig 计算费用明细
- **THEN** 系统返回 `ChargingRecordResponse`，包含费用明细（electricityFee + serviceFee）
- **THEN** 响应时间 < 200ms

#### Scenario: 查询充电记录详情（不属于当前用户）
- **WHEN** 用户请求充电记录详情，recordId=123，但该记录属于其他用户
- **THEN** 系统返回 403 错误，错误消息为"无权访问该充电记录"
- **THEN** 不执行任何数据库查询（在权限检查阶段拦截）

---

### Requirement: 管理端查询所有充电记录优化
系统 SHALL 使用批量预取策略优化管理端查询所有充电记录，支持多条件筛选且查询次数固定为 3 次。

#### Scenario: 管理端查询所有充电记录（按充电桩筛选）
- **WHEN** 管理员请求所有充电记录，筛选条件为 chargingPileId=5，page=1，size=20
- **THEN** 系统执行 3 次查询（主查询 + 充电桩批量查询 + 车辆批量查询）
- **THEN** 系统返回 `Page<ChargingRecordResponse>` 包含最多 20 条记录
- **THEN** 所有记录的 chargingPileId = 5

#### Scenario: 管理端查询所有充电记录（按用户筛选）
- **WHEN** 管理员请求所有充电记录，筛选条件为 userId=10，page=1，size=20
- **THEN** 系统执行 3 次查询
- **THEN** 系统返回 `Page<ChargingRecordResponse>` 包含最多 20 条记录
- **THEN** 所有记录的 userId = 10

#### Scenario: 管理端查询所有充电记录（多条件筛选）
- **WHEN** 管理员请求所有充电记录，筛选条件为 userId=10，chargingPileId=5，status=COMPLETED，startDate=2026-01-01，endDate=2026-01-31，page=1，size=20
- **THEN** 系统执行 3 次查询
- **THEN** 系统返回 `Page<ChargingRecordResponse>` 包含符合所有筛选条件的记录

---

### Requirement: 分页参数验证
系统 SHALL 验证分页参数，防止内存溢出和性能问题。

#### Scenario: 分页参数验证（size 超过最大值）
- **WHEN** 用户请求充电记录列表，page=1，size=200
- **THEN** 系统返回 400 错误，错误消息为"分页大小不能超过 100"

#### Scenario: 分页参数验证（page 小于 1）
- **WHEN** 用户请求充电记录列表，page=0，size=10
- **THEN** 系统返回 400 错误，错误消息为"页码必须大于等于 1"

#### Scenario: 分页参数验证（size 小于 1）
- **WHEN** 用户请求充电记录列表，page=1，size=0
- **THEN** 系统返回 400 错误，错误消息为"分页大小必须大于等于 1"

#### Scenario: 分页参数验证（合法参数）
- **WHEN** 用户请求充电记录列表，page=1，size=10
- **THEN** 系统正常处理请求，返回 10 条记录

---

### Requirement: 性能指标
系统 SHALL 满足以下性能指标。

#### Scenario: 充电记录列表查询响应时间
- **WHEN** 用户请求充电记录列表，size=10
- **THEN** 响应时间 < 500ms（P95）

#### Scenario: 充电记录详情查询响应时间
- **WHEN** 用户请求充电记录详情
- **THEN** 响应时间 < 200ms（P95）

#### Scenario: 数据库查询次数
- **WHEN** 用户请求充电记录列表，size=10
- **THEN** 数据库查询次数 ≤ 3

#### Scenario: 数据库查询次数（详情）
- **WHEN** 用户请求充电记录详情，status=COMPLETED
- **THEN** 数据库查询次数 ≤ 2
