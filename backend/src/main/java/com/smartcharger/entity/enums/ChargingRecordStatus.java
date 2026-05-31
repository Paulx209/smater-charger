package com.smartcharger.entity.enums;

/**
 * Charging record lifecycle status.
 */
public enum ChargingRecordStatus {
    CHARGING("充电中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    ChargingRecordStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
