# 预约管理端控制台实施计划

## 文档说明

- 文档日期：2026-03-31
- 对应 change：`reservation-admin-console`
- 目标：补齐预约模块的管理端独立控制台，并形成管理端查询、详情和取消的最小闭环

## 实施原则

- 以当前代码为准，不以旧预约文档为准
- 保持小颗粒度，不把预约规则重构混入本轮
- 优先复用现有预约响应模型和管理端页面模式
- 本轮只新增管理员能力，不改用户端主流程

## 实施步骤

### 1. OpenSpec 与计划落地

- 创建 `openspec/changes/reservation-admin-console/`
- 补齐 `proposal.md`、`design.md`、`tasks.md`
- 补齐聚焦预约管理端控制台的 requirement spec

### 2. 后端管理员预约接口

- 新增管理员预约控制器
- 为预约服务增加管理端列表、详情、取消能力
- 为预约仓库补充管理端筛选查询
- 保持取消规则仅对 `PENDING` 生效
- 取消成功后同步恢复充电桩状态

### 3. frontend-admin 预约控制台

- 新增预约 API、类型和 store
- 新增预约列表页和详情页
- 在路由与导航中接入预约管理入口
- 保持用户详情抽屉中的预约子表不受影响

### 4. 文档回写

- 更新模块基线文档
- 更新项目总盘点文档
- 更新交接文档
- 完成 OpenSpec tasks 状态回写

### 5. 验证

- `backend` 执行 `mvn.cmd -q -DskipTests compile`
- `frontend-admin` 执行 `npm.cmd run type-check`
- 如有残余问题，明确区分是否属于本 change 引入

## 交付结果

完成后应形成：

- 管理端预约列表页
- 管理端预约详情页
- 管理员取消 `PENDING` 预约能力
- 代码基线与文档一致的 `reservation-admin-console` change