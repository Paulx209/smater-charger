package com.smartcharger.entity.enums;

/**
 * 预警通知类型枚举
 */
public enum WarningNoticeType {
    IDLE_REMINDER("空闲提醒"),
    OVERTIME_WARNING("超时占位预警"),
    FAULT_NOTICE("故障通知"),
    RESERVATION_REMINDER("预约提醒");

    private final String description;

    WarningNoticeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
