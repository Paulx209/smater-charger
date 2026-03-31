# 预警通知管理端对齐实施计划

## 文档说明

- 文档日期：2026-03-31
- 对应 change：`warning-notice-admin-alignment`
- 目标：补齐预警通知模块的管理端独立控制台，并形成列表、详情、批量已读、删除和全局阈值设置的最小闭环

## 实施原则

- 以当前代码为准，不以旧预警文档为准
- 保持小颗粒度，不把通知生成规则重构混入本轮
- 优先复用现有用户侧预警通知响应模型和系统配置存储方式
- 本轮只新增管理员能力，不改用户端主流程

## 实施步骤

### 1. OpenSpec 与计划落地

- 创建 `openspec/changes/warning-notice-admin-alignment/`
- 补齐 `proposal.md`、`design.md`、`tasks.md`
- 补齐聚焦管理端控制台的 requirement spec

### 2. 后端管理员预警通知接口

- 新增管理员预警通知控制器
- 为预警通知服务增加管理端列表、详情、批量已读、删除、全局阈值能力
- 为预警通知仓库补充管理端筛选查询
- 保持用户阈值和全局阈值分离

### 3. frontend-admin 预警通知控制台

- 新增预警通知 API、类型和 store
- 新增列表页、详情页、设置页
- 在路由与导航中接入预警通知管理入口
- 保持用户端通知中心与阈值设置不受影响

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

- 管理端预警通知列表页
- 管理端预警通知详情页
- 管理端全局阈值设置页
- 管理员批量已读与删除能力
- 代码基线与文档一致的 `warning-notice-admin-alignment` change