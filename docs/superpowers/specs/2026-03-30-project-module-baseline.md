# 项目模块基线

## 文档说明

- 文档日期：2026-03-31
- 基线原则：以当前代码现状为准，不以旧规划文档为准
- 用途：记录一级业务模块的实现状态、边界问题和下一步建议

## 当前总体判断

当前项目已经不再是“核心功能未实现”，而是“多数核心业务已闭环，剩余缺口集中在少数管理端能力与客户端历史工程问题”。

到目前为止，用户管理、公告、故障报修、充电记录、价格配置、预约等模块都已经完成一轮代码优先的边界收敛。现在更合适的推进方式，是继续按一级业务模块收口剩余后台能力，而不是重新铺大范围计划。

## 一级模块状态

| 模块 | backend | frontend-admin | frontend-client | 当前判断 |
| --- | --- | --- | --- | --- |
| 用户认证 | 已实现 | 部分实现 | 已实现 | 登录鉴权已闭环 |
| 用户管理 | 已实现 | 已实现 | 部分实现 | 管理端主链路可用 |
| 充电桩 | 已实现 | 已实现 | 已实现 | 前后端闭环 |
| 车辆 | 已实现 | 待确认 | 已实现 | 用户端可用，后台能力仍需确认 |
| 预约 | 已实现 | 已实现 | 已实现 | 管理端控制台已补齐，模块已闭环 |
| 充电记录 | 已实现 | 已实现 | 已实现 | 管理端独立控制台已补齐 |
| 公告 | 已实现 | 已实现 | 已实现 | 边界已收敛，客户端仍有历史类型问题待清理 |
| 价格配置 | 已实现 | 已实现 | 已实现 | 已完成 admin/client 边界拆分 |
| 故障报修 | 已实现 | 已实现 | 已实现 | 管理端处理和统计已补齐 |
| 预警通知 | 已实现 | 部分实现 | 已实现 | 管理端仍是下一优先缺口 |
| 统计 | 已实现 | 已实现 | 部分实现 | 管理端统计已可用，客户端无独立重点 |

## 已完成的重要变更

- `announcement-contract-alignment`
- `fault-report-contract-alignment`
- `charging-record-admin-console-and-hardening`
- `user-management-contract-alignment`
- `announcement-store-response-alignment`
- `statistics-response-alignment`
- `price-config-boundary-alignment`
- `reservation-admin-console`

## 模块级结论

### 已闭环模块

- 用户认证
- 用户管理
- 充电桩
- 预约
- 充电记录
- 公告
- 价格配置
- 故障报修

### 仍有明确缺口的模块

- 预警通知：缺管理端能力
- 车辆：后台是否需要独立管理端能力仍需确认

### 工程层剩余问题

- `frontend-client` 公告响应类型仍有历史问题
- `frontend-client` 预约、充电记录等还残留一批 TypeScript 清理项
- 旧 `charging-record-frontend` change 仍建议后续做归档或清理

## 当前优先级建议

### P0

- `warning-notice-admin-alignment`

### P1

- `client-announcement-response-alignment`
- `frontend-client` 历史 TypeScript 问题分批清理
- 车辆模块后台能力再识别

### P2

- 旧 change 归档
- 进一步收敛项目总览文档与旧接口文档

## 最近一次状态更新

### 2026-03-31 预约管理端控制台补齐

本次新增了预约模块的管理端独立控制台，核心结果如下：

- 后端新增 `/admin/reservations` 列表、详情、取消接口
- 管理端新增预约列表页与详情页
- 管理员现在可以查看、筛选并取消 `PENDING` 状态预约
- 预约模块已从“管理端明显缺口”推进到“前后端闭环”
- 验证结果：`backend` 编译通过，`frontend-admin` `type-check` 通过

## 关联文档

- `接口文档设计/待实现功能清单.md`
- `docs/superpowers/session-handoff.md`
- `docs/superpowers/specs/2026-03-30-openspec-codex-collaboration-design.md`
- `docs/superpowers/plans/2026-03-30-project-reidentification-plan.md`