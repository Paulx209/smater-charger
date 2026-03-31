# Design: client-type-check-cleanup

## Scope

本 change 只处理 rontend-client 的类型与工程清理，不修改后端接口，不扩展新页面。

## Decisions

- 充电记录状态映射使用更宽松的字符串索引，兼容模板读取方式。
- ReservationCreate.vue 只补缺失导入，不调整预约创建逻辑。
- 预约详情页保留现有调用方式，通过类型层补丁解决 Element Plus 和价格估算请求的约束冲突。
- 无效测试改为占位 smoke test，避免继续依赖不存在的示例组件。

## Validation

以 
pm.cmd run type-check 作为本 change 的通过条件。