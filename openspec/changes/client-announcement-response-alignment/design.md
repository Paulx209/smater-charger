# Design: client-announcement-response-alignment

## Summary

本次 change 只做客户端公告响应类型对齐，不调整业务行为。核心是让 API 层明确返回已解包的数据结构，从而让 store 和组件都不再把返回值当作 `AxiosResponse` 使用。

## Detailed Design

### API Layer

- `getAnnouncementList` 返回 `Promise<AnnouncementClientListResponse>`
- `getAnnouncementDetail` 返回 `Promise<AnnouncementClientInfo>`
- `getLatestAnnouncements` 返回 `Promise<AnnouncementClientInfo[]>`

### Store Layer

- 保持现有状态字段不变
- 直接使用解包后的结果对象更新列表、分页和详情状态

### Adjacent Cleanup

- `AnnouncementCarousel.vue` 直接消费公告数组结果
- `warningNotice` store 在删除时使用安全访问，避免严格模式下的空值告警

## Validation

- 运行 `frontend-client` 类型检查
- 公告模块相关错误应全部消失
- 记录剩余与本 change 无关的历史错误