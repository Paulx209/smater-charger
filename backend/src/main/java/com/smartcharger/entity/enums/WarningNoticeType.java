package com.smartcharger.entity.enums;

/**
 * Warning notice type.
 */
public enum WarningNoticeType {
    IDLE_REMINDER("空闲提醒"),
    OVERTIME_WARNING("超时占位预警"),
    FAULT_NOTICE("故障通知"),
    RESERVATION_REMINDER("预约提醒"),
    CHARGING_ENDING_SOON("充电即将结束提醒"),
    CHARGING_COMPLETED("充电完成提醒");

    private final String description;

    WarningNoticeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
