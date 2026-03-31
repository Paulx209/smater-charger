# 项目模块基线

## 基线说明

- 更新时间：2026-03-31
- 以当前代码仓库为准
- 本文用于记录一级业务模块的实现状态与差异收敛结果

## 模块总览

| 模块 | backend | frontend-admin | frontend-client | 当前状态 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 用户认证 | 已实现 | 无独立页面 | 已实现 | 已闭环 | 登录、注册、鉴权已可用 |
| 用户管理 | 已实现 | 已实现 | 无管理端需求 | 已闭环 | 管理端契约已对齐 |
| 充电桩 | 已实现 | 已实现 | 已实现 | 已闭环 | 管理端与车主端都可用 |
| 车辆 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐管理端车辆控制台 |
| 预约 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐管理端列表、详情、取消 |
| 充电记录 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐管理端独立控制台 |
| 公告 | 已实现 | 已实现 | 已实现 | 已闭环 | admin/client 边界与响应契约已对齐 |
| 价格配置 | 已实现 | 已实现 | 已实现 | 已闭环 | admin 管理、client 只读已拆分 |
| 故障报修 | 已实现 | 已实现 | 已实现 | 已闭环 | 车主端与管理端闭环已形成 |
| 预警通知 | 已实现 | 已实现 | 已实现 | 已闭环 | 已补齐管理端控制台与阈值设置 |
| 统计分析 | 已实现 | 已实现 | 无独立需求 | 已闭环 | 管理端统计页可用 |

## 已完成变更

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
- `client-type-check-cleanup`
- `vehicle-admin-console`

## 当前工程结论

- `backend` 编译通过
- `frontend-admin` type-check 通过
- `frontend-client` type-check 通过
- 当前主线缺口已经从“功能未实现”转向“仓库卫生清理与持续验收”

## 2026-03-31 全仓库冒烟验证

- `backend` 编译通过
- `frontend-admin` build 通过
- `frontend-client` build 通过
- 当前未发现新的阻塞性构建错误
