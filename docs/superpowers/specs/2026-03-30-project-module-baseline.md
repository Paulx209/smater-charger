# 项目模块基线

## 文档说明

- 文档日期：2026-03-31
- 基线原则：以当前代码现状为准，不以旧规划文档为准。
- 用途：记录一级业务模块的实现状态、边界问题和下一步建议。

## 当前总体判断

当前项目已经不是“核心模块大量缺失”的状态。
更准确的判断是：

1. 大多数核心业务模块已经具备后端基础。
2. 用户端主流程完成度整体较高。
3. 当前主要工作集中在：
   - 管理端缺口补齐
   - 前后端契约对齐
   - 历史工程问题清理

## 一级模块状态

| 模块 | backend | frontend-admin | frontend-client | 当前判断 |
| --- | --- | --- | --- | --- |
| 用户认证 | 已实现 | 不适用 | 已实现 | 已闭环 |
| 用户管理 | 已实现 | 已实现 | 不适用 | 管理端契约已对齐，可用 |
| 充电桩 | 已实现 | 已实现 | 已实现 | 已闭环 |
| 车辆 | 已实现 | 未规划 | 已实现 | 用户端闭环，后台能力未建设 |
| 预约 | 已实现 | 部分实现 | 已实现 | 用户端主流程已具备，管理端缺口明显 |
| 充电记录 | 已实现 | 已实现 | 已实现 | 已闭环 |
| 公告 | 已实现 | 已实现 | 已实现 | 已闭环，边界已清理 |
| 价格配置 | 已实现 | 已实现 | 已实现 | 已闭环，admin/client 边界已清理 |
| 故障报修 | 已实现 | 已实现 | 已实现 | 已闭环 |
| 预警通知 | 已实现 | 部分实现 | 已实现 | 管理端能力仍不完整 |
| 统计报表 | 已实现 | 已实现 | 不适用 | 管理端统计可用 |

## 已完成的重要变更

### 1. 项目基线重识别

- 以代码为准重新识别模块状态。
- 将原本失真的待办文档改造成代码优先的盘点文档。

### 2. 已完成的 OpenSpec change

- `announcement-contract-alignment`
- `fault-report-contract-alignment`
- `charging-record-admin-console-and-hardening`
- `user-management-contract-alignment`
- `announcement-store-response-alignment`
- `statistics-response-alignment`
- `price-config-boundary-alignment`

## 模块级结论

### 已闭环模块

- 用户认证
- 用户管理
- 充电桩
- 充电记录
- 公告
- 价格配置
- 故障报修

### 仍有明显缺口的模块

- 预约：缺管理端能力
- 预警通知：缺管理端能力
- 车辆：后台能力尚未建设，但是否需要仍待判断

### 仍有工程清理空间的模块

- `frontend-client` 公告响应类型
- `frontend-client` 预约、充电记录等历史 TypeScript 问题
- 旧 `charging-record-frontend` change 的归档和收尾

## 当前优先级建议

### P0

- `reservation-admin-console`
- `warning-notice-admin-alignment`

### P1

- `client-announcement-response-alignment`
- `frontend-client` 历史 TypeScript 问题按模块拆分清理
- 车辆后台能力是否需要的再次识别

### P2

- 旧 change 归档
- 持续回写项目基线和交接文档

## 最近一次状态更新

### 2026-03-30：价格配置边界对齐完成

- 后端管理接口迁移到 `/admin/price-config`
- 后端用户端只读接口保留 `/price-config/current` 和 `/price-config/estimate`
- `frontend-admin` 继续作为唯一价格配置管理端
- `frontend-client` 已移除管理员价格配置页面和路由
- `frontend-client` 现在只保留当前价格读取和费用估算能力
- 验证结果：`backend` 编译通过，`frontend-admin` 类型检查通过；`frontend-client` 仍有与价格配置无关的历史问题

## 关联文档

- `接口文档设计/待实现功能清单.md`
- `docs/superpowers/session-handoff.md`
- `docs/superpowers/specs/2026-03-30-openspec-codex-collaboration-design.md`
- `docs/superpowers/plans/2026-03-30-project-reidentification-plan.md`