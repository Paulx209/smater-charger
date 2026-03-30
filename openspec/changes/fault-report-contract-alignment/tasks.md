# Tasks: fault-report-contract-alignment

## 1. OpenSpec

- [x] 1.1 创建 `fault-report-contract-alignment` change 目录
- [x] 1.2 写入 proposal、design、tasks、spec

## 2. 车主端契约收缩

- [x] 2.1 重写 `frontend-client/src/api/faultReport.ts` 以对齐真实后端路径
- [x] 2.2 收缩 `frontend-client/src/types/faultReport.ts` 到后端真实字段
- [x] 2.3 简化 `frontend-client/src/stores/faultReport.ts`
- [x] 2.4 调整 `ChargingPileDetail.vue` 报修弹窗为仅描述提交
- [x] 2.5 调整 `FaultReportList.vue` 为真实字段列表
- [x] 2.6 调整 `FaultReportDetail.vue` 为真实字段详情

## 3. 管理端闭环

- [x] 3.1 新增 `frontend-admin` 故障报修 types、api、store
- [x] 3.2 新增管理端列表页
- [x] 3.3 新增管理端详情处理页
- [x] 3.4 新增管理端统计页
- [x] 3.5 新增管理端路由与导航入口

## 4. 验证与文档

- [x] 4.1 搜索确认源码中不再使用 `/api/fault-reports`
- [x] 4.2 更新项目盘点文档
- [x] 4.3 更新模块基线文档
- [x] 4.4 运行针对性的 type-check，并记录仓库原有阻塞项