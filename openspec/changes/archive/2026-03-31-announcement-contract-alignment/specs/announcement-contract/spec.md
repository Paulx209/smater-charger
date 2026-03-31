# Spec: 公告模块契约对齐

## ADDED Requirements

### Requirement: 公告前端请求路径与后端契约一致
系统 SHALL 保证公告模块前端请求路径与后端 `AnnouncementController` 的资源路径一致，且不得重复拼接 `/api` 前缀。

#### Scenario: 管理端公告列表请求
- **WHEN** 管理员访问公告管理列表
- **THEN** 前端请求路径为 `/announcement/admin`
- **THEN** 请求实际落在后端 `/api/announcement/admin`
- **THEN** 请求路径中不得出现 `/api/api/announcement/admin`

#### Scenario: 车主端公告列表请求
- **WHEN** 用户访问公告列表页
- **THEN** 前端请求路径为 `/announcement`
- **THEN** 请求实际落在后端 `/api/announcement`
- **THEN** 请求路径中不得出现 `/api/api/announcement`

#### Scenario: 首页最新公告请求
- **WHEN** 用户访问首页公告轮播
- **THEN** 前端请求路径为 `/announcement/latest`
- **THEN** 请求实际落在后端 `/api/announcement/latest`

---

### Requirement: 管理端与车主端公告职责分离
系统 SHALL 保证公告管理能力仅存在于管理端，车主端只保留公告消费能力。

#### Scenario: 管理公告入口
- **WHEN** 用户访问 `frontend-admin`
- **THEN** 系统提供公告创建、编辑、发布、下线、删除、详情和列表能力

#### Scenario: 车主公告入口
- **WHEN** 用户访问 `frontend-client`
- **THEN** 系统仅提供公告列表、公告详情和首页最新公告能力
- **THEN** 系统不提供管理公告的路由入口

#### Scenario: 车主端不暴露管理路由
- **WHEN** 检查 `frontend-client` 路由配置
- **THEN** 不存在 `/admin/announcement`
- **THEN** 不存在 `/admin/announcement/create`
- **THEN** 不存在 `/admin/announcement/edit/:id`
