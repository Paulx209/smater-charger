# Tasks: 公告模块契约对齐

## 1. 现状确认

- [x] 1.1 确认后端 `AnnouncementController` 的真实路由契约
- [x] 1.2 确认 `frontend-admin` 公告 API、store、页面引用链
- [x] 1.3 确认 `frontend-client` 公告 API、store、页面、首页轮播引用链
- [x] 1.4 列出 `frontend-client` 中所有 `/admin/announcement` 相关入口

## 2. 管理端契约校正

- [x] 2.1 修正 `frontend-admin/src/api/announcement.ts` 中多余的 `/api` 前缀
- [x] 2.2 验证管理端公告 store 与页面调用路径保持一致
- [x] 2.3 验证创建、编辑、发布、下线、删除、列表、详情的请求路径全部正确

## 3. 车主端契约校正

- [x] 3.1 修正 `frontend-client/src/api/announcement.ts` 中多余的 `/api` 前缀
- [x] 3.2 验证车主端公告列表、详情、最新公告接口调用路径正确
- [x] 3.3 确认首页公告轮播继续使用车主侧公告接口

## 4. 端边界收敛

- [x] 4.1 从 `frontend-client` 路由中移除 `/admin/announcement` 相关入口
- [x] 4.2 清理 `frontend-client` 中不应继续保留的管理公告页面引用
- [x] 4.3 保证管理公告能力仅由 `frontend-admin` 承担

## 5. 验证

- [x] 5.1 本地检查两端公告相关请求路径不再出现 `/api/api/...`
- [ ] 5.2 验证管理端公告管理链路
- [ ] 5.3 验证车主端公告列表、详情、首页轮播链路
- [x] 5.4 更新基线文档中公告模块状态

> 说明：`5.2` 与 `5.3` 仍待运行时验证。当前仓库存在多处历史 TypeScript 错误，且沙箱环境下 `vite`/`esbuild` 构建会遇到 `spawn EPERM`，本轮只能完成静态链路校验。