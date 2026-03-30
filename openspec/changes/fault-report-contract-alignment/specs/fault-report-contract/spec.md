# Spec: 故障报修契约对齐

## ADDED Requirements

### Requirement: 车主端故障报修请求路径必须与后端真实契约一致
系统 SHALL 保证车主端故障报修请求只访问 `FaultReportController` 已存在的用户侧接口。

#### Scenario: 车主端提交报修
- **WHEN** 用户从充电桩详情提交故障报修
- **THEN** 前端请求路径为 `/fault-report`
- **THEN** 请求体只包含后端创建接口支持的字段

#### Scenario: 车主端获取报修列表
- **WHEN** 用户访问我的故障报修列表
- **THEN** 前端请求路径为 `/fault-report`
- **THEN** 请求参数与后端分页接口保持一致

#### Scenario: 车主端不再访问不存在接口
- **WHEN** 检查活跃前端源码
- **THEN** 不存在 `/api/fault-reports/my`
- **THEN** 不存在 `/api/fault-reports/upload`
- **THEN** 不存在 `/api/fault-reports/stats/pile/*`

---

### Requirement: 车主端只依赖后端真实字段模型
系统 SHALL 保证车主端故障报修页面与 store 不再依赖后端不存在的字段。

#### Scenario: 提交报修字段
- **WHEN** 用户提交故障报修
- **THEN** 前端只提交 `chargingPileId` 与 `description`

#### Scenario: 列表与详情字段
- **WHEN** 用户查看故障报修列表或详情
- **THEN** 页面展示字段来源于 `FaultReportResponse`
- **THEN** 页面不依赖 `faultType`、`images`、`processTime`、`resolveTime`、`closeTime`

---

### Requirement: 管理端必须形成故障报修闭环
系统 SHALL 在 `frontend-admin` 提供故障报修的列表、详情处理和统计能力。

#### Scenario: 管理端列表
- **WHEN** 管理员访问故障报修列表页
- **THEN** 系统提供状态、充电桩、时间范围筛选
- **THEN** 系统提供分页能力

#### Scenario: 管理端处理
- **WHEN** 管理员访问故障报修详情页
- **THEN** 系统允许更新状态并填写处理备注
- **THEN** 前端请求路径为 `/fault-report/admin/{id}/handle`

#### Scenario: 管理端统计
- **WHEN** 管理员访问故障统计页
- **THEN** 系统请求 `/fault-report/admin/statistics`
- **THEN** 系统展示总量、状态分布、平均处理时长和高频故障充电桩