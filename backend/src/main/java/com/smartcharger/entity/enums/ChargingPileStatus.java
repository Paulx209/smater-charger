package com.smartcharger.entity.enums;

/**
 * 充电桩状态枚举
 */
public enum ChargingPileStatus {
    IDLE("空闲"),
    CHARGING("充电中"),
    FAULT("故障"),
    RESERVED("已预约"),
    OVERTIME("超时占位");

    private final String description;

    ChargingPileStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
