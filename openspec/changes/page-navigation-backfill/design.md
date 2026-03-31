# Design: page-navigation-backfill

## Strategy

为 `frontend-admin` 和 `frontend-client` 分别提供 `navigateBack(router, fallbackRoute)`。

逻辑：

1. 如果浏览器历史里存在上一路由，执行 `router.back()`。
2. 否则执行 `router.push(fallbackRoute)`。

## Scope

### Admin

- 价格配置表单
- 公告表单
- 预约详情
- 充电记录详情
- 故障报修详情
- 故障报修统计
- 预警通知详情
- 预警通知设置
- 车辆详情

### Client

- 充电桩详情
- 预约创建
- 预约详情
- 充电记录详情
- 充电记录统计
- 故障报修详情
- 公告详情
- 车辆表单
- 预警设置

## Validation

- `frontend-admin` `npm.cmd run type-check`
- `frontend-client` `npm.cmd run type-check`
- 人工回退检查
