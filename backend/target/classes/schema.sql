-- Smart Charger Management System Database Schema
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_charger DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_charger;

-- 1. 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `warning_threshold` INT DEFAULT NULL COMMENT '个性化占位预警阈值（分钟）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态（0-禁用, 1-启用）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`),
    UNIQUE KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 3. 用户角色关联表
CREATE TABLE `user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 4. 权限表
CREATE TABLE `permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 5. 角色权限关联表
CREATE TABLE `role_permission` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 6. 车辆信息表
CREATE TABLE `vehicle` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `license_plate` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    `model` VARCHAR(50) DEFAULT NULL COMMENT '车型',
    `battery_capacity` DECIMAL(10,2) DEFAULT NULL COMMENT '电池容量（kWh）',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认车辆（0-否, 1-是）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_license_plate` (`license_plate`),
    CONSTRAINT `fk_vehicle_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆信息表';

-- 7. 充电桩表
CREATE TABLE `charging_pile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(50) NOT NULL COMMENT '充电桩编号',
    `location` VARCHAR(255) NOT NULL COMMENT '位置描述',
    `lng` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `lat` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `type` VARCHAR(20) NOT NULL COMMENT '类型（AC-交流慢充, DC-直流快充）',
    `power` DECIMAL(10,2) NOT NULL COMMENT '功率（kW）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'IDLE' COMMENT '实时状态（IDLE-空闲, CHARGING-充电中, FAULT-故障, RESERVED-已预约, OVERTIME-超时占位）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_code` (`code`),
    KEY `idx_status` (`status`),
    KEY `idx_location` (`lng`, `lat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电桩表';

-- 8. 预约记录表
CREATE TABLE `reservation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `charging_pile_id` BIGINT NOT NULL COMMENT '充电桩ID',
    `start_time` DATETIME NOT NULL COMMENT '预约开始时间',
    `end_time` DATETIME NOT NULL COMMENT '预约结束时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '预约状态（PENDING-待使用, COMPLETED-已完成, CANCELLED-已取消, EXPIRED-已过期）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_charging_pile_id` (`charging_pile_id`),
    KEY `idx_status` (`status`),
    KEY `idx_time_range` (`start_time`, `end_time`),
    CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reservation_charging_pile` FOREIGN KEY (`charging_pile_id`) REFERENCES `charging_pile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';

-- 9. 充电记录表
CREATE TABLE `charging_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `charging_pile_id` BIGINT NOT NULL COMMENT '充电桩ID',
    `vehicle_id` BIGINT DEFAULT NULL COMMENT '车辆ID',
    `start_time` DATETIME NOT NULL COMMENT '充电开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '充电结束时间',
    `duration` INT DEFAULT NULL COMMENT '充电时长（分钟）',
    `electric_quantity` DECIMAL(10,2) DEFAULT NULL COMMENT '充电电量（kWh）',
    `fee` DECIMAL(10,2) DEFAULT NULL COMMENT '费用（元）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'CHARGING' COMMENT '订单状态（CHARGING-充电中, COMPLETED-已完成, CANCELLED-已取消, OVERTIME-超时占位）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_charging_pile_id` (`charging_pile_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    CONSTRAINT `fk_charging_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_charging_record_charging_pile` FOREIGN KEY (`charging_pile_id`) REFERENCES `charging_pile` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_charging_record_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电记录表';

-- 10. 预警通知表
CREATE TABLE `warning_notice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `charging_pile_id` BIGINT DEFAULT NULL COMMENT '充电桩ID',
    `charging_record_id` BIGINT DEFAULT NULL COMMENT '充电记录ID',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型（IDLE_REMINDER-空闲提醒, OVERTIME_WARNING-超时占位预警, FAULT_NOTICE-故障通知, RESERVATION_REMINDER-预约提醒）',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读（0-未读, 1-已读）',
    `send_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态（PENDING-待发送, SENT-已发送, FAILED-发送失败）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_send_status` (`send_status`),
    CONSTRAINT `fk_warning_notice_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_warning_notice_charging_pile` FOREIGN KEY (`charging_pile_id`) REFERENCES `charging_pile` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_warning_notice_charging_record` FOREIGN KEY (`charging_record_id`) REFERENCES `charging_record` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警通知表';

-- 11. 系统公告表
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `admin_id` BIGINT NOT NULL COMMENT '发布管理员ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态（DRAFT-草稿, PUBLISHED-已发布）',
    `start_time` DATETIME DEFAULT NULL COMMENT '生效开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '生效结束时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    CONSTRAINT `fk_announcement_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 12. 故障报修表
CREATE TABLE `fault_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '报修用户ID',
    `charging_pile_id` BIGINT NOT NULL COMMENT '充电桩ID',
    `description` TEXT NOT NULL COMMENT '故障描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态（PENDING-待处理, PROCESSING-处理中, RESOLVED-已修复）',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handle_remark` TEXT DEFAULT NULL COMMENT '处理备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_charging_pile_id` (`charging_pile_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_fault_report_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_fault_report_charging_pile` FOREIGN KEY (`charging_pile_id`) REFERENCES `charging_pile` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fault_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='故障报修表';

-- 13. 系统配置表
CREATE TABLE `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 14. 费用配置表
CREATE TABLE `price_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `charging_pile_type` VARCHAR(20) NOT NULL COMMENT '充电桩类型（AC, DC）',
    `price_per_kwh` DECIMAL(10,2) NOT NULL COMMENT '每度电价格（元/kWh）',
    `service_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务费（元）',
    `start_time` DATETIME DEFAULT NULL COMMENT '生效开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '生效结束时间',
    `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用, 1-启用）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_active` (`charging_pile_type`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用配置表';

-- 插入初始数据

-- 插入角色数据
INSERT INTO `role` (`name`, `description`) VALUES
('ADMIN', '管理员角色，拥有系统管理权限'),
('CAR_OWNER', '车主角色，使用充电服务');

-- 插入权限数据
INSERT INTO `permission` (`name`, `description`) VALUES
('user:manage', '用户管理权限'),
('charging_pile:manage', '充电桩管理权限'),
('order:manage', '订单管理权限'),
('announcement:manage', '公告管理权限'),
('fault_report:manage', '故障报修管理权限'),
('statistics:view', '数据统计查看权限'),
('charging:use', '使用充电服务权限'),
('reservation:manage', '预约管理权限');

-- 为管理员角色分配权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `role` r, `permission` p
WHERE r.name = 'ADMIN' AND p.name IN (
    'user:manage', 'charging_pile:manage', 'order:manage',
    'announcement:manage', 'fault_report:manage', 'statistics:view'
);

-- 为车主角色分配权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `role` r, `permission` p
WHERE r.name = 'CAR_OWNER' AND p.name IN (
    'charging:use', 'reservation:manage'
);

-- 插入系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('default_warning_threshold', '15', '默认占位预警阈值（分钟）'),
('platform_name', '智能充电桩管理系统', '平台名称'),
('reservation_duration', '120', '预约时长（分钟）'),
('max_reservation_count', '3', '用户最大预约数量');

-- 插入费用配置数据
INSERT INTO `price_config` (`charging_pile_type`, `price_per_kwh`, `service_fee`, `is_active`) VALUES
('AC', 1.20, 0.50, 1),
('DC', 1.80, 1.00, 1);
