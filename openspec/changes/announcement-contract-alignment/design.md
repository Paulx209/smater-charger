# Design: 公告模块契约对齐

## Problem

公告模块当前已经有后端控制器、管理端页面、车主端页面和首页轮播，但仍存在两个真实问题：

1. 两端前端 `announcement` API 文件都写死了 `/api/announcement...` 路径，而请求实例的 `baseURL` 已经是 `/api`。
2. `frontend-client` 中存在 `/admin/announcement` 系列页面和对应 store 调用，管理能力与车主能力未分层。

如果不先修复这两个问题，公告模块会继续处于“页面存在但契约不可靠”的状态。

## Goals

1. 保证公告模块前端请求路径与后端控制器契约一致
2. 保证管理端与车主端的公告职责边界清晰
3. 保证首页公告轮播、车主公告列表/详情、管理端公告管理都能沿着各自端口工作

## Non-Goals

1. 不新增公告审批、定时发布增强等功能
2. 不调整公告数据库结构
3. 不抽象跨项目通用 API 层

## Decisions

### Decision 1: 统一以前端 `baseURL=/api` 为基准

公告 API 路径统一去掉多余的 `/api` 前缀。

原因：

- 当前 `request.ts` 已统一配置 `baseURL: '/api'`
- 继续保留 `/api/announcement` 会拼出 `/api/api/announcement`
- 其他正常模块，如 `auth`、`reservation`、`charging-record` 都已经采用“仅写资源路径”的方式

### Decision 2: 管理端和车主端保留各自独立的公告 API 文件与 store

不在本 change 中强制合并 `frontend-admin` 和 `frontend-client` 的公告 store。

原因：

- 当前两端仓库已独立存在
- 本次目标是先把契约修正和边界拉直，而不是做横向抽象
- 两端都可以继续保留独立 store，但只能调用各自应负责的公告能力

### Decision 3: 车主端移除公告管理入口

`frontend-client` 中的 `/admin/announcement` 相关路由和页面视为越界能力，应从车主端移除。

原因：

- 车主端职责是消费公告，不是管理公告
- 继续保留会让模块识别结果失真
- 管理端已经有独立的公告管理页承接该能力

## Target State

### Backend

后端继续保留现有契约：

- `GET /announcement`
- `GET /announcement/{id}`
- `GET /announcement/latest`
- `GET /announcement/admin`
- `GET /announcement/admin/{id}`
- `POST /announcement/admin`
- `PUT /announcement/admin/{id}`
- `PUT /announcement/admin/{id}/publish`
- `PUT /announcement/admin/{id}/unpublish`
- `DELETE /announcement/admin/{id}`

### Frontend Admin

管理端只负责：

- 公告列表
- 公告详情/编辑
- 发布/下线
- 删除/创建

其 API 调用目标应为：

- `/announcement/admin`
- `/announcement/admin/{id}`
- `/announcement/admin/{id}/publish`
- `/announcement/admin/{id}/unpublish`

### Frontend Client

车主端只负责：

- 公告列表
- 公告详情
- 首页最新公告轮播

其 API 调用目标应为：

- `/announcement`
- `/announcement/{id}`
- `/announcement/latest`

车主端不再承担：

- 管理公告列表
- 管理公告表单
- 发布/下线/删除/创建

## Risks

### Risk 1: 路由移除后残留引用

`frontend-client` 中如果仍有组件或 store 引用管理公告能力，删除路由后会出现死链或构建错误。

缓解：

- 先用搜索确认所有 `/admin/announcement` 入口
- 删除前同步清理对应页面、store 引用和导航入口

### Risk 2: 两端 store 内容重复

管理端与车主端仍保留独立公告 store，会有重复代码。

缓解：

- 本次先接受重复，保证边界正确
- 未来如确有收益，再单独建 change 做抽象

## Validation

1. 管理端公告页面的所有请求不再出现 `/api/api/...`
2. 车主端公告列表、公告详情、首页轮播请求不再出现 `/api/api/...`
3. `frontend-client` 中不再暴露 `/admin/announcement` 路由
4. 管理端继续保留完整公告管理能力
