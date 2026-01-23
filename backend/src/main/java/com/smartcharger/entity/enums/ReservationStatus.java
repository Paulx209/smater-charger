package com.smartcharger.entity.enums;

/**
 * 预约状态枚举
 */
public enum ReservationStatus {
    PENDING("待使用"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    EXPIRED("已过期");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
