# Vehicle Admin Console Design

## 背景

当前项目的车辆模块已经具备后端与车主端闭环，但 `frontend-admin` 还没有独立的车辆管理控制台。管理员只能间接从用户相关页面看到局部车辆信息，无法按全局维度查询、查看或删除车辆。

## 目标

本次变更补齐车辆模块的管理端最小闭环：

- 后端提供管理员车辆列表、详情、删除接口
- `frontend-admin` 提供车辆列表页和详情页
- 支持按用户 ID 和车牌号筛选
- 保持车主端车辆主流程不变

## 范围

### 包含

- `GET /admin/vehicles`
- `GET /admin/vehicles/{id}`
- `DELETE /admin/vehicles/{id}`
- 管理端车辆列表、详情、删除与导航入口
- OpenSpec 与项目总览文档同步

### 不包含

- 车辆审核流
- 车辆图片、证件管理
- 批量导入导出
- 管理端设置默认车辆
- 车主端车辆能力重构

## 后端设计

- 新增 `AdminVehicleController`
- 在 `VehicleService` / `VehicleServiceImpl` 中补管理端列表、详情、删除能力
- 管理端列表按 `userId`、`licensePlate` 筛选，并返回分页结构
- 删除逻辑复用现有车辆删除语义：
  - 删除默认车辆时，自动把该用户剩余车辆中的第一辆提升为默认

## 前端设计

- 新增 `frontend-admin` 的 `api/types/store/views`
- 列表页负责筛选、分页、查看详情、删除
- 详情页负责展示车辆基础信息、所属用户、时间信息，并支持删除
- 导航新增“车辆管理”入口

## 风险与验证

主要风险是删除车辆后默认车切换语义必须保持一致，且不能影响车主端已有车辆接口。

验证重点：

- `backend` 编译通过
- `frontend-admin` type-check 通过
- 管理端车辆列表、详情、删除链路可用
- 车主端车辆接口不回退
