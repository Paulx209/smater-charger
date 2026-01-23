package com.smartcharger.entity.enums;

/**
 * 充电记录状态枚举
 */
public enum ChargingRecordStatus {
    CHARGING("充电中"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    OVERTIME("超时占位");

    private final String description;

    ChargingRecordStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
