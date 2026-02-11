# 数据库迁移指南

## 迁移脚本：V2__fix_schema_inconsistency.sql

### 目的
修复数据模型不一致问题，确保数据库表结构与 JPA 实体类定义完全一致。

### 修复内容

#### 1. warning_notice 表
- **问题**：实体类有 `overtime_minutes` 字段，但数据库表缺少该字段
- **修复**：添加 `overtime_minutes INT DEFAULT NULL` 字段

#### 2. system_config 表
- **问题1**：实体类有 `user_id` 字段，但数据库表缺少该字段
- **问题2**：唯一约束不一致（实体类要求 `user_id + config_key` 复合唯一，数据库只有 `config_key` 唯一）
- **修复**：
  - 添加 `user_id BIGINT DEFAULT NULL` 字段
  - 删除旧的 `idx_config_key` 唯一索引
  - 创建新的 `idx_user_config (user_id, config_key)` 复合唯一索引
  - 添加外键约束 `fk_system_config_user`

### 执行方式

#### 方式 1：手动执行（推荐用于生产环境）

```bash
# 1. 备份数据库
mysqldump -u root -p smart_charger > backup_before_v2_$(date +%Y%m%d_%H%M%S).sql

# 2. 执行迁移脚本
mysql -u root -p smart_charger < backend/src/main/resources/db/migration/V2__fix_schema_inconsistency.sql

# 3. 验证迁移结果
mysql -u root -p smart_charger -e "DESCRIBE warning_notice;"
mysql -u root -p smart_charger -e "DESCRIBE system_config;"
mysql -u root -p smart_charger -e "SHOW INDEX FROM system_config;"
```

#### 方式 2：使用 Flyway（如果项目集成了 Flyway）

```bash
# 1. 确保 pom.xml 中有 Flyway 依赖
# 2. 将迁移脚本放在 src/main/resources/db/migration/ 目录
# 3. 启动应用，Flyway 会自动执行迁移
mvn spring-boot:run
```

#### 方式 3：使用 JPA 自动建表（仅开发环境）

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=update
```

**注意**：此方式会自动同步实体类到数据库，但不推荐用于生产环境。

### 迁移安全性

✅ **幂等性**：脚本可以重复执行，不会报错
✅ **数据保护**：不会删除或修改现有数据
✅ **回滚方案**：如果迁移失败，可以从备份恢复

### 回滚方案

如果迁移后出现问题，可以执行以下回滚操作：

```sql
-- 回滚 warning_notice 表
ALTER TABLE `warning_notice` DROP COLUMN `overtime_minutes`;

-- 回滚 system_config 表
ALTER TABLE `system_config` DROP FOREIGN KEY `fk_system_config_user`;
ALTER TABLE `system_config` DROP INDEX `idx_user_config`;
ALTER TABLE `system_config` ADD UNIQUE KEY `idx_config_key` (`config_key`);
ALTER TABLE `system_config` DROP COLUMN `user_id`;
```

或者直接从备份恢复：

```bash
mysql -u root -p smart_charger < backup_before_v2_YYYYMMDD_HHMMSS.sql
```

### 验证清单

迁移完成后，请验证以下内容：

- [ ] `warning_notice` 表包含 `overtime_minutes` 字段
- [ ] `system_config` 表包含 `user_id` 字段
- [ ] `system_config` 表有 `idx_user_config (user_id, config_key)` 唯一索引
- [ ] `system_config` 表有 `fk_system_config_user` 外键约束
- [ ] 现有数据完整性未受影响
- [ ] 应用启动正常，无 JPA 映射错误

### 影响评估

- **停机时间**：约 1-2 分钟（取决于数据量）
- **数据风险**：低（仅添加字段和索引，不修改现有数据）
- **应用兼容性**：完全兼容，无需修改应用代码

### 后续步骤

迁移完成后，建议：

1. 重启应用，验证 JPA 实体映射正常
2. 运行单元测试，确保业务逻辑正常
3. 检查日志，确认无 SQL 异常
4. 更新 `schema.sql` 文件（已完成）

---

**迁移脚本版本**：V2
**创建时间**：2026-02-01
**状态**：✅ 已创建，待执行
