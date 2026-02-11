# Frontend-Admin 模块文档

[根目录](../CLAUDE.md) > **frontend-admin**

---

## 变更记录 (Changelog)

### 2026-02-01 17:23:13
- 初始化管理端前端模块文档
- 扫描完成：2个管理页面模块（费用配置、公告管理）
- 识别核心功能：价格策略管理、系统公告发布、用户管理

---

## 模块职责

Frontend-Admin 是 Smart Charger 系统的**管理端前端应用**，基于 Vue 3 + TypeScript + Element Plus 构建，为系统管理员提供后台管理功能。主要职责包括：

- **费用配置管理**：创建、编辑、删除价格策略，设置峰谷电价
- **系统公告管理**：发布、编辑、删除系统公告，管理公告状态
- **用户管理**：查看用户列表、修改用户状态、重置密码（待完善）
- **数据统计**：查看充电桩使用情况、收入统计（待完善）

---

## 入口与启动

### 主入口文件
- **文件路径**：`src/main.ts`
- **核心依赖**：
  - Vue 3.5.26
  - Element Plus 2.13.1
  - Pinia 3.0.4
  - Vue Router 4.6.4

### 启动方式
```bash
cd frontend-admin
npm install
npm run dev
# 默认访问：http://localhost:5174
```

### 构建生产版本
```bash
npm run build
# 输出目录：dist/
```

### 开发工具
- **类型检查**：`npm run type-check`
- **代码检查**：`npm run lint`
- **代码格式化**：`npm run format`
- **单元测试**：`npm run test:unit`

---

## 对外接口

### 路由配置 (src/router/index.ts)

| 路由路径 | 组件 | 功能 | 权限要求 |
|---------|------|------|---------|
| `/login` | Login.vue | 管理员登录 | 无 |
| `/` | - | 重定向到 `/price-config` | - |
| `/price-config` | PriceConfigList.vue | 价格配置列表 | 管理员 |
| `/price-config/add` | PriceConfigForm.vue | 新增价格配置 | 管理员 |
| `/price-config/:id/edit` | PriceConfigForm.vue | 编辑价格配置 | 管理员 |
| `/announcement` | AnnouncementList.vue | 公告列表 | 管理员 |
| `/announcement/create` | AnnouncementForm.vue | 创建公告 | 管理员 |
| `/announcement/edit/:id` | AnnouncementForm.vue | 编辑公告 | 管理员 |

### 路由守卫
- **认证检查**：所有需要 `requiresAuth: true` 的路由会检查 Token
- **权限检查**：`requiresAdmin: true` 的路由需要管理员角色
- **自动跳转**：
  - 未登录访问受保护路由 → 跳转到 `/login`
  - 已登录访问登录页 → 跳转到首页

---

## 关键依赖与配置

### 核心依赖 (package.json)
- **UI 框架**：Element Plus 2.13.1 + @element-plus/icons-vue 2.3.2
- **状态管理**：Pinia 3.0.4
- **HTTP 客户端**：Axios 1.13.2
- **富文本编辑器**：@vueup/vue-quill 1.2.0 + quill 2.0.3
- **构建工具**：Vite 7.3.1
- **开发工具**：
  - TypeScript 5.9.3
  - Vue TSC 3.2.2
  - ESLint 9.39.2
  - Prettier 3.8.1
  - Vitest 4.0.17

### Vite 配置
- **插件**：
  - `@vitejs/plugin-vue`：Vue 3 单文件组件支持
  - `@vitejs/plugin-vue-jsx`：JSX 语法支持
  - `vite-plugin-vue-devtools`：Vue DevTools 集成
- **别名**：`@` → `src/`

### Axios 配置 (src/utils/request.ts)
- **基础 URL**：`http://localhost:8080/api`
- **请求拦截器**：自动添加 `Authorization: Bearer {token}`
- **响应拦截器**：
  - 统一处理错误（401 自动跳转登录）
  - 提取 `data` 字段简化调用

---

## 数据模型

### TypeScript 类型定义

#### 1. PriceConfig (价格配置)
**文件**：`src/types/priceConfig.ts`
```typescript
interface PriceConfig {
  id?: number
  name: string
  pricePerKwh: number          // 每度电价格
  serviceFee: number           // 服务费
  peakHourMultiplier: number   // 峰时倍率
  effectiveDate: string        // 生效日期
  expiryDate?: string          // 失效日期
}
```

#### 2. Announcement (公告)
**文件**：`src/types/announcement.ts`
```typescript
interface Announcement {
  id?: number
  title: string
  content: string              // 富文本内容
  priority: number             // 优先级 (1-5)
  status: 'DRAFT' | 'PUBLISHED' | 'EXPIRED'
  publishTime?: string
  expiryTime?: string
}
```

#### 3. User (用户)
**文件**：`src/types/user.ts`
```typescript
interface User {
  id: number
  username: string
  email: string
  phone?: string
  realName?: string
  enabled: boolean
  roles: string[]
}
```

---

## 状态管理 (Pinia Stores)

### 1. priceConfigStore (src/stores/priceConfig.ts)
**职责**：管理价格配置的增删改查
- **状态**：
  - `priceConfigs: PriceConfig[]`：价格配置列表
  - `currentConfig: PriceConfig | null`：当前编辑的配置
- **操作**：
  - `fetchPriceConfigs()`：获取列表
  - `createPriceConfig(data)`：创建配置
  - `updatePriceConfig(id, data)`：更新配置
  - `deletePriceConfig(id)`：删除配置

### 2. announcementStore (src/stores/announcement.ts)
**职责**：管理系统公告
- **状态**：
  - `announcements: Announcement[]`：公告列表
  - `currentAnnouncement: Announcement | null`：当前编辑的公告
- **操作**：
  - `fetchAnnouncements()`：获取列表
  - `createAnnouncement(data)`：创建公告
  - `updateAnnouncement(id, data)`：更新公告
  - `deleteAnnouncement(id)`：删除公告
  - `publishAnnouncement(id)`：发布公告

### 3. userStore (src/stores/user.ts)
**职责**：管理当前登录用户信息
- **状态**：
  - `token: string | null`：JWT Token
  - `userInfo: User | null`：用户信息
- **操作**：
  - `login(username, password)`：登录
  - `logout()`：登出
  - `fetchUserInfo()`：获取用户信息

### 4. userManagementStore (src/stores/userManagement.ts)
**职责**：管理用户列表（管理端）
- **状态**：
  - `users: User[]`：用户列表
- **操作**：
  - `fetchUsers()`：获取用户列表
  - `updateUserStatus(id, enabled)`：启用/禁用用户
  - `resetPassword(id)`：重置密码

---

## API 调用层

### 1. priceConfigApi (src/api/priceConfig.ts)
```typescript
// 获取价格配置列表
getPriceConfigs(): Promise<PriceConfig[]>

// 创建价格配置
createPriceConfig(data: PriceConfig): Promise<PriceConfig>

// 更新价格配置
updatePriceConfig(id: number, data: PriceConfig): Promise<PriceConfig>

// 删除价格配置
deletePriceConfig(id: number): Promise<void>
```

### 2. announcementApi (src/api/announcement.ts)
```typescript
// 获取公告列表
getAnnouncements(): Promise<Announcement[]>

// 创建公告
createAnnouncement(data: Announcement): Promise<Announcement>

// 更新公告
updateAnnouncement(id: number, data: Announcement): Promise<Announcement>

// 删除公告
deleteAnnouncement(id: number): Promise<void>

// 发布公告
publishAnnouncement(id: number): Promise<void>
```

### 3. authApi (src/api/auth.ts)
```typescript
// 管理员登录
login(username: string, password: string): Promise<{ token: string, userInfo: User }>

// 获取当前用户信息
getUserInfo(): Promise<User>
```

### 4. userApi (src/api/user.ts)
```typescript
// 获取用户列表
getUsers(): Promise<User[]>

// 更新用户状态
updateUserStatus(id: number, enabled: boolean): Promise<void>

// 重置用户密码
resetPassword(id: number): Promise<{ newPassword: string }>
```

---

## 测试与质量

### 测试现状
- **单元测试**：仅有示例测试 `src/components/__tests__/HelloWorld.spec.ts`
- **测试框架**：Vitest 4.0.17 + Vue Test Utils 2.4.6
- **运行测试**：`npm run test:unit`

### 代码质量工具
- **ESLint**：配置文件 `eslint.config.ts`
  - 规则：Vue 推荐规则 + TypeScript 规则
  - 自动修复：`npm run lint`
- **Prettier**：代码格式化
  - 配置：`prettier.config.js`（如存在）
  - 格式化：`npm run format`
- **TypeScript**：严格类型检查
  - 配置：`tsconfig.json`
  - 检查：`npm run type-check`

### 建议补充
1. **组件测试**：为 PriceConfigForm、AnnouncementForm 编写测试
2. **E2E 测试**：使用 Playwright/Cypress 测试完整流程
3. **API Mock**：使用 MSW 模拟后端接口

---

## 常见问题 (FAQ)

### Q1: 如何添加新的管理页面？
1. 在 `src/views/admin/` 下创建 Vue 组件
2. 在 `src/router/index.ts` 中添加路由配置
3. 在 `src/api/` 下创建 API 调用函数
4. 在 `src/stores/` 下创建 Pinia Store（如需要）
5. 在 `src/types/` 下定义 TypeScript 类型

### Q2: 如何修改后端 API 地址？
- 修改 `src/utils/request.ts` 中的 `baseURL`
- 或通过环境变量配置（`.env.development`、`.env.production`）

### Q3: 如何使用富文本编辑器？
- 已集成 Quill 编辑器（`@vueup/vue-quill`）
- 在 `AnnouncementForm.vue` 中有使用示例
- 文档：https://vueup.github.io/vue-quill/

### Q4: 如何调试 API 请求？
- 打开浏览器 DevTools → Network 标签
- 查看请求/响应详情
- 使用 Vue DevTools 查看 Pinia Store 状态

### Q5: 如何处理 Token 过期？
- Axios 响应拦截器会自动捕获 401 错误
- 自动清除 Token 并跳转到登录页
- 代码位置：`src/utils/request.ts`

---

## 相关文件清单

### 核心目录结构
```
frontend-admin/
├── src/
│   ├── main.ts                               # 应用入口
│   ├── App.vue                               # 根组件
│   ├── router/
│   │   └── index.ts                          # 路由配置
│   ├── views/
│   │   ├── Login.vue                         # 登录页
│   │   └── admin/                            # 管理页面
│   │       ├── PriceConfigList.vue           # 价格配置列表
│   │       ├── PriceConfigForm.vue           # 价格配置表单
│   │       ├── AnnouncementList.vue          # 公告列表
│   │       └── AnnouncementForm.vue          # 公告表单
│   ├── components/                           # 可复用组件
│   │   └── __tests__/                        # 组件测试
│   ├── api/                                  # API 调用层
│   │   ├── auth.ts                           # 认证 API
│   │   ├── priceConfig.ts                    # 价格配置 API
│   │   ├── announcement.ts                   # 公告 API
│   │   └── user.ts                           # 用户管理 API
│   ├── stores/                               # Pinia 状态管理
│   │   ├── user.ts                           # 用户状态
│   │   ├── priceConfig.ts                    # 价格配置状态
│   │   ├── announcement.ts                   # 公告状态
│   │   └── userManagement.ts                 # 用户管理状态
│   ├── types/                                # TypeScript 类型定义
│   │   ├── user.ts
│   │   ├── priceConfig.ts
│   │   └── announcement.ts
│   ├── utils/                                # 工具函数
│   │   ├── request.ts                        # Axios 封装
│   │   └── auth.ts                           # 认证工具
│   └── assets/                               # 静态资源
│       └── main.css                          # 全局样式
├── package.json                              # 依赖配置
├── vite.config.ts                            # Vite 配置
├── tsconfig.json                             # TypeScript 配置
├── eslint.config.ts                          # ESLint 配置
└── vitest.config.ts                          # Vitest 配置
```

### 关键文件路径
- **应用入口**：`src/main.ts`
- **路由配置**：`src/router/index.ts`
- **Axios 封装**：`src/utils/request.ts`
- **认证工具**：`src/utils/auth.ts`

---

## 待完善功能

### 高优先级
1. **用户管理页面**：完善用户列表、状态管理、密码重置界面
2. **数据统计页面**：充电桩使用率、收入统计、用户活跃度
3. **充电桩管理**：充电桩 CRUD、状态监控、故障处理

### 中优先级
4. **权限管理**：角色管理、权限分配
5. **日志查看**：系统日志、操作日志
6. **导出功能**：数据导出为 Excel

### 低优先级
7. **主题切换**：暗色模式支持
8. **国际化**：多语言支持

---

**文档生成时间**：2026-02-01 17:23:13
**扫描文件数**：17个 TypeScript 文件
**覆盖率**：核心管理功能已完整扫描，部分功能待开发
