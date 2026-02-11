-- ============================================================
-- 数据库迁移脚本 V2：修复数据模型不一致问题
-- 创建时间：2026-02-01
-- 描述：修复 warning_notice 和 system_config 表结构与实体类不一致的问题
-- ============================================================

USE smart_charger;

-- ============================================================
-- 1. 修复 warning_notice 表：添加 overtime_minutes 字段
-- ============================================================

-- 检查字段是否已存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'warning_notice'
      AND COLUMN_NAME = 'overtime_minutes'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `warning_notice` ADD COLUMN `overtime_minutes` INT DEFAULT NULL COMMENT ''超时分钟数'' AFTER `content`',
    'SELECT ''Column overtime_minutes already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. 修复 system_config 表：添加 user_id 字段和复合唯一索引
-- ============================================================

-- 2.1 检查 user_id 字段是否已存在
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'system_config'
      AND COLUMN_NAME = 'user_id'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `system_config` ADD COLUMN `user_id` BIGINT DEFAULT NULL COMMENT ''用户ID（NULL表示全局配置）'' AFTER `id`',
    'SELECT ''Column user_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.2 删除旧的唯一索引（如果存在）
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'system_config'
      AND INDEX_NAME = 'idx_config_key'
);

SET @sql = IF(@index_exists > 0,
    'ALTER TABLE `system_config` DROP INDEX `idx_config_key`',
    'SELECT ''Index idx_config_key does not exist'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.3 创建新的复合唯一索引
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'system_config'
      AND INDEX_NAME = 'idx_user_config'
);

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `system_config` ADD UNIQUE KEY `idx_user_config` (`user_id`, `config_key`)',
    'SELECT ''Index idx_user_config already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.4 添加外键约束（如果不存在）
SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'system_config'
      AND CONSTRAINT_NAME = 'fk_system_config_user'
);

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE `system_config` ADD CONSTRAINT `fk_system_config_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE',
    'SELECT ''Foreign key fk_system_config_user already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. 数据迁移：将现有全局配置的 user_id 设置为 NULL
-- ============================================================

-- 确保现有的全局配置 user_id 为 NULL
UPDATE `system_config` SET `user_id` = NULL WHERE `user_id` IS NOT NULL AND `config_key` IN (
    'default_warning_threshold',
    'platform_name',
    'reservation_duration',
    'max_reservation_count'
);

-- ============================================================
-- 4. 验证迁移结果
-- ============================================================

-- 显示 warning_notice 表结构
SELECT 'warning_notice table structure:' AS info;
DESCRIBE `warning_notice`;

-- 显示 system_config 表结构
SELECT 'system_config table structure:' AS info;
DESCRIBE `system_config`;

-- 显示 system_config 索引
SELECT 'system_config indexes:' AS info;
SHOW INDEX FROM `system_config`;

-- 显示迁移完成信息
SELECT '✅ 数据库迁移 V2 完成！' AS message,
       NOW() AS completed_at;
