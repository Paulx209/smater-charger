-- ============================================================
-- Database migration V3: charging lifecycle states
-- Purpose:
--   Add target-based charging and leave-confirmation fields, then clean
--   historical pile statuses that no longer match runtime facts.
-- Safety:
--   Idempotent column/index creation; FAULT piles are preserved.
-- Rollback:
--   Restore pile/record statuses from backup if historical cleanup is wrong.
--   New nullable columns may be left in place safely.
-- Verification:
--   Confirm no charging_pile.status = RESERVED remains and no CHARGING,
--   WAITING_LEAVE, or OVERTIME pile lacks a matching fact record.
-- ============================================================

USE smart_charger;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'leave_time'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `leave_time` DATETIME DEFAULT NULL COMMENT ''车辆驶离确认时间'' AFTER `end_time`',
    'SELECT ''Column leave_time already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'target_type'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `target_type` VARCHAR(20) DEFAULT NULL COMMENT ''充电目标类型'' AFTER `status`',
    'SELECT ''Column target_type already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'target_value'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `target_value` DECIMAL(10,2) DEFAULT NULL COMMENT ''用户输入的充电目标值'' AFTER `target_type`',
    'SELECT ''Column target_value already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'target_duration_minutes'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `target_duration_minutes` INT DEFAULT NULL COMMENT ''目标充电时长（分钟）'' AFTER `target_value`',
    'SELECT ''Column target_duration_minutes already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'target_kwh'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `target_kwh` DECIMAL(10,3) DEFAULT NULL COMMENT ''目标充电电量（kWh）'' AFTER `target_duration_minutes`',
    'SELECT ''Column target_kwh already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'target_end_time'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `target_end_time` DATETIME DEFAULT NULL COMMENT ''目标结束时间'' AFTER `target_kwh`',
    'SELECT ''Column target_end_time already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'end_reason'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `end_reason` VARCHAR(30) DEFAULT NULL COMMENT ''结束原因'' AFTER `target_end_time`',
    'SELECT ''Column end_reason already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND COLUMN_NAME = 'pre_end_notice_sent'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `charging_record` ADD COLUMN `pre_end_notice_sent` TINYINT NOT NULL DEFAULT 0 COMMENT ''结束前提醒是否已发送'' AFTER `end_reason`',
    'SELECT ''Column pre_end_notice_sent already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'smart_charger'
      AND TABLE_NAME = 'charging_record'
      AND INDEX_NAME = 'idx_target_end_time'
);
SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `charging_record` ADD KEY `idx_target_end_time` (`target_end_time`)',
    'SELECT ''Index idx_target_end_time already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `charging_record`
SET `status` = 'COMPLETED'
WHERE `status` = 'OVERTIME';

UPDATE `charging_pile`
SET `status` = 'IDLE'
WHERE `status` = 'RESERVED';

UPDATE `charging_pile` p
SET p.`status` = 'IDLE'
WHERE p.`status` = 'CHARGING'
  AND NOT EXISTS (
      SELECT 1
      FROM `charging_record` r
      WHERE r.`charging_pile_id` = p.`id`
        AND r.`status` = 'CHARGING'
  );

UPDATE `charging_pile` p
SET p.`status` = 'IDLE'
WHERE p.`status` IN ('WAITING_LEAVE', 'OVERTIME')
  AND NOT EXISTS (
      SELECT 1
      FROM `charging_record` r
      WHERE r.`charging_pile_id` = p.`id`
        AND r.`status` = 'COMPLETED'
        AND r.`end_time` IS NOT NULL
        AND r.`leave_time` IS NULL
  );
