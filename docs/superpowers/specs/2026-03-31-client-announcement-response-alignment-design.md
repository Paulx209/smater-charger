# 客户端公告响应类型对齐设计

## 背景

`frontend-client` 的公告模块已经在运行时按“解包后的 data”工作，但类型层仍把接口返回值当成 `AxiosResponse`，导致 `store` 和轮播组件出现一组稳定的 TypeScript 报错。

## 目标

- 对齐 `frontend-client` 公告 API 的响应类型定义。
- 修复公告列表、公告详情、首页公告轮播这条链上的类型漂移。
- 顺手修复 `warningNotice` store 中一个确定的空值访问问题。

## 范围

### 包含

- `frontend-client/src/api/announcement.ts`
- `frontend-client/src/stores/announcement.ts`
- `frontend-client/src/components/AnnouncementCarousel.vue`
- `frontend-client/src/stores/warningNotice.ts`
- 对应 OpenSpec 与基线文档回写

### 不包含

- 预约、充电记录等其他 `frontend-client` 历史 TypeScript 问题
- 公告模块 UI 改版
- 后端公告接口调整

## 方案

1. 把公告 API 改成显式返回解包后的业务类型，而不是依赖默认 `AxiosResponse` 泛型。
2. 保持公告 store 的状态结构不变，只修正接口调用后的结果类型。
3. 保持首页轮播和公告列表/详情行为不变，只消除类型漂移。
4. 在 `warningNotice` store 中把数组元素读取改成安全访问，消除严格模式下的空值报错。

## 验证

- 重新运行 `frontend-client` 的 `npm.cmd run type-check`
- 确认公告模块相关报错清零
- 剩余报错若存在，应属于本次 change 之外的历史问题