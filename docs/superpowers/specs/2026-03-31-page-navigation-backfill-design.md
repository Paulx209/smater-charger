# 2026-03-31 page-navigation-backfill design

## 背景

当前前后端大部分业务模块都已经完成，但页面跳转后的返回体验不一致：

- 有些详情页、表单页、设置页没有明确返回按钮。
- 有些页面虽然有返回按钮，但只依赖 `router.back()`。
- 当用户直接访问详情页 URL、从收藏进入、刷新页面后再返回时，浏览器历史并不可靠。

这会导致用户进入子页面后只能依赖浏览器后退，体验不稳定。

## 目标

- 为前后端非首页业务页补齐明确的返回入口。
- 统一返回逻辑：优先返回上一页，没有可靠历史时回到模块列表页。
- 改动只聚焦页面导航，不改接口和业务数据流。

## 非目标

- 不做全局 breadcrumb 系统。
- 不改整体布局框架。
- 不调整接口、权限或状态管理结构。

## 方案

### 返回策略

统一引入轻量 `navigateBack(router, fallbackRoute)` 方法：

1. 如果当前历史状态存在可回退入口，执行 `router.back()`。
2. 否则直接跳转到该模块的主列表页。

### frontend-admin 覆盖页面

- `PriceConfigForm.vue` -> `/price-config`
- `AnnouncementForm.vue` -> `/announcement`
- `ReservationDetail.vue` -> `/reservations`
- `ChargingRecordDetail.vue` -> `/charging-records`
- `FaultReportDetail.vue` -> `/fault-reports`
- `FaultReportStatistics.vue` -> `/fault-reports`
- `WarningNoticeDetail.vue` -> `/warning-notices`
- `WarningNoticeSettings.vue` -> `/warning-notices`
- `VehicleDetail.vue` -> `/vehicles`

### frontend-client 覆盖页面

- `ChargingPileDetail.vue` -> `/charging-piles`
- `ReservationCreate.vue` -> `/reservations`
- `ReservationDetail.vue` -> `/reservations`
- `ChargingRecordDetail.vue` -> `/charging-record`
- `ChargingRecordStatistics.vue` -> `/charging-record`
- `FaultReportDetail.vue` -> `/fault-reports`
- `AnnouncementDetail.vue` -> `/announcement`
- `VehicleForm.vue` -> `/vehicles`
- `WarningNoticeSettings.vue` -> `/warning-notice`

## 验证

- `frontend-admin` `npm.cmd run type-check`
- `frontend-client` `npm.cmd run type-check`
- 人工检查目标页返回行为
