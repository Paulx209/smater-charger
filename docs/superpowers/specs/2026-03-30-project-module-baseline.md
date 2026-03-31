# 项目模块基线

## 基线说明

- 基线日期：2026-03-31
- 当前文档以代码实现为准，不再以旧计划或旧接口草稿为准。
- 目标是用一级业务模块快速判断“是否闭环”和“下一步该做什么”。

## 模块状态总览

| 模块 | backend | frontend-admin | frontend-client | 当前判断 | 备注 |
| --- | --- | --- | --- | --- | --- |
| 用户认证 | 已实现 | 不适用 | 已实现 | 已闭环 | 登录、注册、鉴权已可用 |
| 用户管理 | 已实现 | 已实现 | 不适用 | 已闭环 | 已完成契约对齐 |
| 充电桩 | 已实现 | 已实现 | 已实现 | 已闭环 | 管理与用户侧都可用 |
| 车辆 | 已实现 | 部分实现 | 已实现 | 部分实现 | 管理端能力仍需进一步确认 |
| 预约 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐管理端控制台，客户端仍有少量历史类型问题 |
| 充电记录 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐管理端独立控制台，客户端仍有少量历史类型问题 |
| 公告 | 已实现 | 已实现 | 已实现 | 已闭环 | 已完成 admin/client 契约与响应对齐 |
| 价格配置 | 已实现 | 已实现 | 已实现 | 已闭环 | admin 管理、client 只读 已收口 |
| 故障报修 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐车主端与管理端闭环 |
| 预警通知 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐管理端能力并清理客户端空值访问 |
| 统计 | 已实现 | 已实现 | 不适用 | 已闭环 | 管理端统计页已可用 |

## 已完成的关键变更

- `announcement-contract-alignment`
- `fault-report-contract-alignment`
- `charging-record-admin-console-and-hardening`
- `user-management-contract-alignment`
- `announcement-store-response-alignment`
- `statistics-response-alignment`
- `price-config-boundary-alignment`
- `reservation-admin-console`
- `warning-notice-admin-alignment`
- `client-announcement-response-alignment`

## 当前剩余重点

### 工程清理

- `frontend-client` 仍有历史 TypeScript 问题，当前主要集中在预约、充电记录和测试残留。
- 一些旧文档和旧 change 仍需要后续归档或清理，但不影响当前业务闭环。

### 待确认模块

- 车辆模块的管理端边界和是否需要独立控制台，还需要单独识别。

## 下一优先建议

### P0

- `frontend-client` 预约与充电记录类型清理

### P1

- 车辆模块管理端边界识别
- 测试残留清理（如 `HelloWorld.spec.ts`）

### P2

- 旧 change 归档与文档持续收尾