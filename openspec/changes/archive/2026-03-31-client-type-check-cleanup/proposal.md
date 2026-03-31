# Proposal: client-type-check-cleanup

## Why

rontend-client 在功能边界逐步收敛后，仍残留一组小范围但稳定复现的类型错误。这些问题主要来自历史页面类型约束过窄、第三方组件类型缺口和孤立测试残留，已经开始影响后续模块继续推进。

## What Changes

- 修复充电记录页面中的状态索引和空值消息提示报错
- 修复预约创建页缺失的 ElMessageBox 导入
- 为价格估算请求和 Element Plus MessageBox 选项补充必要类型兼容
- 清理失效的 HelloWorld 测试引用

## Impact

- rontend-client 静态检查恢复通过
- 不引入新的业务能力，也不改变已有业务流程