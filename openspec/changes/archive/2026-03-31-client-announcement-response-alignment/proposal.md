# Proposal: client-announcement-response-alignment

## Why

`frontend-client` 公告模块仍在按 `AxiosResponse` 理解接口返回值，但运行时请求拦截器已经返回了业务数据本体。这个偏差导致 store 和首页轮播组件存在一组稳定的类型错误，影响后续继续清理客户端工程问题。

## What Changes

- 对齐 `frontend-client` 公告 API 的响应类型写法
- 修复公告 store 与首页轮播组件的类型漂移
- 顺手修复 `warningNotice` store 的确定性空值访问问题
- 回写 OpenSpec 与项目基线文档

## Impact

- 不改变公告模块现有功能行为
- 不涉及后端接口和页面结构改动
- 可以把 `frontend-client` 的剩余问题继续收敛到更小的后续 change