# client-announcement-response-alignment 实施计划

## 目标

修复 `frontend-client` 公告模块的响应类型漂移，并顺手消除 `warningNotice` store 的一个空值访问错误。

## 实施步骤

1. 新建 `client-announcement-response-alignment` OpenSpec change 目录和基础文档。
2. 调整公告 API 的返回类型写法，使其与请求拦截器的已解包数据一致。
3. 修复公告 store 和首页轮播组件的类型使用。
4. 修复 `warningNotice` store 的确定性空值访问。
5. 运行 `frontend-client` 类型检查并记录剩余历史错误。
6. 更新模块基线、交接文档和总盘点文档。

## 验证标准

- 公告模块相关类型错误消失。
- `warningNotice` store 不再报数组元素可能为空。
- 剩余 `frontend-client` 报错仅来自本次 change 范围之外。