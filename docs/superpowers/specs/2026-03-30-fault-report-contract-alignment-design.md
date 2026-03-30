# 故障报修契约对齐设计

## 背景

当前故障报修模块存在明显的前后端漂移：

- 后端真实接口为 `/fault-report` 与 `/fault-report/admin/*`
- 车主端前端仍在使用 `/api/fault-reports`、`/my`、`/upload`、`/stats/pile/*`
- 车主端类型与页面依赖了后端并不存在的字段，例如 `faultType`、`images`、`processTime`、`resolveTime`
- 管理端尚未建立故障报修模块页面，导致后端已存在的管理接口无法形成前端闭环

这类问题和公告模块修复前的状态相同：代码看起来很多，但真实链路并不成立。

## 目标

本次 change 的目标是以当前后端实现为唯一基线，建立一个真实可用的故障报修闭环：

- 车主端保留最小闭环
- 管理端建立最小管理闭环
- 统计页接入已有后端接口
- 清理前端对不存在字段和接口的依赖

## 非目标

本次不处理以下内容：

- 不新增故障分类体系
- 不新增图片上传能力
- 不新增处理时间线字段
- 不扩展后端实体结构
- 不把该模块扩展成二期运维平台

## 判定结论

### 1. 后端缺的是管理端页面，不是业务字段

从当前后端代码看，故障报修已经具备：

- 车主端创建、列表、详情、取消
- 管理端列表、详情、处理、统计

因此当前主要缺口不是接口能力不足，而是：

- 前端契约写错
- 前端字段模型虚高
- 管理端页面缺失

### 2. 以下字段判定为前端多余，而不是后端缺失

- `faultType`
- `images`
- `processTime`
- `resolveTime`
- `closeTime`
- `processNote`
- `resolveNote`

这些字段在现有后端实体、请求 DTO、响应 DTO 中均没有成立的对应结构。它们不属于“后端少了一点点”，而属于前端先行假设。

### 3. 当前真实最小模型

创建请求：

- `chargingPileId`
- `description`

管理处理请求：

- `status`
- `handleRemark`

详情/列表响应核心字段：

- `id`
- `userId`
- `userName`
- `userPhone`
- `chargingPileId`
- `pileName`
- `pileLocation`
- `pileType`
- `description`
- `status`
- `statusDesc`
- `handlerId`
- `handlerName`
- `handleRemark`
- `createdTime`
- `updatedTime`

## 方案比较

### 方案 A：严格按后端真实契约收缩，推荐

- 车主端只保留描述提交、我的列表、详情、取消
- 管理端新增列表、详情、处理、筛选、分页、统计
- 清理前端所有伪字段依赖

优点：

- 闭环最真实
- 后续维护成本最低
- 风险边界清晰

缺点：

- 车主端 UI 会比当前假想功能少

### 方案 B：兼容式收缩

- 接口适配后端真实路径
- 页面结构尽量不大动
- 伪字段改成空展示或降级展示

优点：

- 视觉变化小

缺点：

- 容易继续保留错误模型
- 后续维护会反复踩坑

### 方案 C：拆成两个 change

- 本轮只修车主端契约
- 管理端另开 change

优点：

- 单次变更小

缺点：

- 不满足这轮要形成完整闭环的目标

结论：采用方案 A。

## 设计

### 一、车主端

#### 1. API 对齐

`frontend-client/src/api/faultReport.ts` 统一对齐为真实后端路径：

- `POST /fault-report`
- `GET /fault-report`
- `GET /fault-report/{id}`
- `DELETE /fault-report/{id}`

移除以下不存在或本轮不使用的接口：

- `/fault-reports/my`
- `/fault-reports/upload`
- `/fault-reports/stats/pile/*`
- `PUT /fault-reports/{id}`

#### 2. 类型收缩

`frontend-client/src/types/faultReport.ts` 收缩为后端真实字段模型。

需要移除：

- 故障类型枚举及映射
- 图片字段
- 伪时间线字段
- 与更新接口相关的虚假请求结构

分页响应需要按后端 `Page` 结构重新定义，而不是沿用当前的 `records/total/current/size` 形式。

#### 3. Store 收缩

`frontend-client/src/stores/faultReport.ts` 只保留：

- `createReport`
- `fetchMyReportList`
- `fetchReportDetail`
- `removeReport`
- `reset`

删除：

- `updateReport`
- `uploadImage`
- `fetchPileStats`
- 与错误分页结构绑定的逻辑

#### 4. 页面调整

`ChargingPileDetail.vue`

- 报修弹窗只保留“故障描述”
- 去掉故障类型选择
- 去掉图片上传
- 创建请求改为提交 `chargingPileId + description`

`FaultReportList.vue`

- 去掉按故障类型筛选
- 保留状态筛选、分页
- 去掉图片缩略图
- 去掉基于故障类型的图标逻辑
- 列表展示收敛为：桩名、位置、描述、状态、创建时间、更新时间

`FaultReportDetail.vue`

- 去掉故障类型、图片、伪时间线
- 展示真实字段：编号、状态、充电桩、描述、创建时间、更新时间、处理备注、处理人
- `PENDING` 状态允许取消

### 二、管理端

#### 1. 新增模块范围

本次新增管理端故障报修最小闭环，包括：

- 列表页
- 详情/处理页
- 统计页

#### 2. 管理端 API

新增 `frontend-admin/src/api/faultReport.ts`，对接：

- `GET /fault-report/admin/all`
- `GET /fault-report/admin/{id}`
- `PUT /fault-report/admin/{id}/handle`
- `GET /fault-report/admin/statistics`

#### 3. 管理端 Store

新增独立 store，承担：

- 列表查询
- 详情读取
- 状态处理
- 统计读取

#### 4. 管理端页面

列表页：

- 状态筛选
- 充电桩筛选
- 时间范围筛选
- 分页
- 跳转详情

详情/处理页：

- 展示报修信息
- 显示用户、充电桩、描述、状态、时间
- 支持更新状态和填写处理备注

统计页：

- 接入后端统计接口
- 展示总数、各状态数量、平均处理时长、故障最多的充电桩

#### 5. 路由

管理端新增故障报修相关路由，统一只在 `frontend-admin` 暴露，不进入车主端工程。

### 三、后端

后端默认不扩字段，不改实体。

只有在接入管理端页面时发现存在极小契约阻塞时，才允许做必要的小修正，例如：

- 分页参数解释
- 日期筛选格式
- 返回字段命名的小范围兼容

但本 change 不引入 `faultType`、图片上传或附加时间线字段。

## 验证

### 车主端验证

- 从充电桩详情提交报修
- 查看我的报修列表
- 查看报修详情
- 在 `PENDING` 状态取消报修

### 管理端验证

- 列表加载
- 状态/充电桩/时间范围筛选
- 分页切换
- 查看详情
- 提交处理结果
- 统计页加载

### 契约验证

- 前端不再访问 `/api/fault-reports`
- 前端统一访问 `/fault-report` 与 `/fault-report/admin/*`
- 车主端不再提交后端不存在的字段

## 实施顺序

1. 新建 `fault-report-contract-alignment` OpenSpec change
2. 先修车主端 API、类型、store、页面
3. 再补管理端 API、store、router
4. 再补管理端列表、详情/处理、统计页
5. 最后更新盘点文档和基线文档

## 风险说明

- 车主端当前页面对伪字段依赖较深，收缩时会牵动多文件联动修改
- 管理端为新增模块，需注意遵守现有管理端模式
- 当前仓库存在大量存量 TypeScript 错误，验证时必须区分本次引入问题与历史问题

## 结论

本次故障报修 change 不做功能扩张，只做真实闭环恢复。

判断原则是：

- 以后端真实契约为准
- 前端多余字段应删而不是硬保留
- 管理端缺的是页面，不是后端业务模型

完成后，故障报修模块将从“代码存在但链路不成立”转为“真实可用的最小闭环”。