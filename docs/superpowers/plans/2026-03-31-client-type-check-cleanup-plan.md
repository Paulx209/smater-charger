# 实施计划：client-type-check-cleanup

## 步骤

1. 恢复误改文件到最近稳定提交状态。
2. 重新运行 rontend-client 的 	ype-check，确认真实剩余错误边界。
3. 修复充电记录页面的状态索引与空值消息提示问题。
4. 修复预约创建页缺失的 ElMessageBox 导入。
5. 用类型补丁兼容预约详情页的价格估算和 MessageBox 选择配置。
6. 替换孤立的 HelloWorld 测试残留。
7. 再次运行 	ype-check 并回写文档。

## 交付标准

- rontend-client 的 
pm.cmd run type-check 通过
- 本轮改动保持在前端类型与工程层
- OpenSpec、项目基线和交接文档同步更新