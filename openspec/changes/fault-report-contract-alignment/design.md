# Design: 故障报修契约对齐

## Problem

故障报修模块当前存在三类真实问题：

1. 前端请求路径与后端控制器契约不一致
2. 车主端数据模型依赖后端不存在的字段
3. 管理端缺少使用既有后端接口的前端页面

如果不先修复这些问题，故障报修模块将继续处于“代码存在但链路不成立”的状态。

## Goals

1. 统一以前端 `baseURL=/api` 配置为基础，纠正所有故障报修请求路径
2. 让车主端只依赖后端真实存在的字段与能力
3. 让管理端能够完成列表、筛选、详情、处理、统计

## Non-Goals

1. 不增加故障分类、图片、附件或扩展时间线
2. 不改造后端为新的二期业务模型
3. 不把本 change 扩展成完整运维平台

## Decisions

### Decision 1: 以后端真实契约为唯一基线

前端统一对齐：

- `POST /fault-report`
- `GET /fault-report`
- `GET /fault-report/{id}`
- `DELETE /fault-report/{id}`
- `GET /fault-report/admin/all`
- `GET /fault-report/admin/{id}`
- `PUT /fault-report/admin/{id}/handle`
- `GET /fault-report/admin/statistics`

### Decision 2: 车主端收缩为真实最小闭环

车主端只保留：

- 提交报修描述
- 我的报修列表
- 报修详情
- 待处理状态取消

移除或停止依赖：

- `faultType`
- `images`
- `processTime`
- `resolveTime`
- `closeTime`
- `processNote`
- `resolveNote`
- `/my`
- `/upload`
- `/stats/pile/*`

### Decision 3: 管理能力只在 admin 应用中暴露

管理端新增：

- 故障报修列表页
- 故障报修详情与处理页
- 故障统计页

这些能力只存在于 `frontend-admin`。

## Target State

### Backend

后端保持现有实体与接口模型不变，默认不扩字段。

### Frontend Client

车主端请求只访问：

- `/fault-report`
- `/fault-report/{id}`

车主端页面不再展示和提交不存在字段。

### Frontend Admin

管理端请求访问：

- `/fault-report/admin/all`
- `/fault-report/admin/{id}`
- `/fault-report/admin/{id}/handle`
- `/fault-report/admin/statistics`

管理端形成完整故障报修管理闭环。

## Validation

1. 活跃源码中不再出现 `/api/fault-reports`
2. 车主端不再提交后端不存在字段
3. 管理端具备列表、筛选、详情、处理、统计能力
4. 故障报修管理入口只存在于 `frontend-admin`