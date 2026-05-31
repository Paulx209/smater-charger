package com.smartcharger.entity.enums;

/**
 * Charging pile runtime status.
 */
public enum ChargingPileStatus {
    IDLE("空闲"),
    CHARGING("充电中"),
    WAITING_LEAVE("待驶离"),
    FAULT("故障"),
    OVERTIME("超时占位");

    private final String description;

    ChargingPileStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
