# 设计：client-type-check-cleanup

## 背景

rontend-client 在完成公告模块响应对齐后，仍残留一组小颗粒度的静态类型错误，集中在充电记录列表、充电统计、预约创建、预约详情以及孤立测试文件。这些问题不属于新功能缺口，而是现有功能链路中的类型与工程边界噪音，会持续干扰后续开发。

## 目标

- 清理 rontend-client 当前剩余的确定性 	ype-check 错误
- 不改动预约、充电记录、价格配置的业务行为
- 用最小改动恢复客户端工程基线

## 方案

1. 保持页面行为不变，只修复类型和导入问题。
2. 对充电记录状态映射放宽索引类型，消除模板中对状态枚举的隐式 ny 报错。
3. 对价格估算请求类型和 Element Plus MessageBox 选项做类型补丁，兼容现有预约详情页的调用方式。
4. 将缺失组件引用的测试替换成占位 smoke test，避免无效历史测试继续阻塞校验。

## 影响范围

- rontend-client/src/views/ChargingRecordList.vue
- rontend-client/src/views/ChargingRecordStatistics.vue
- rontend-client/src/views/ReservationCreate.vue
- rontend-client/src/types/chargingRecord.ts
- rontend-client/src/types/priceConfig.ts
- rontend-client/src/types/element-plus.d.ts
- rontend-client/src/components/__tests__/HelloWorld.spec.ts

## 验证

- 运行 rontend-client 的 
pm.cmd run type-check
- 确认本轮之后客户端静态检查通过
- 不扩散到其它模块的业务重构