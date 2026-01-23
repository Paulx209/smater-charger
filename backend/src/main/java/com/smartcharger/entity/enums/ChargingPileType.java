package com.smartcharger.entity.enums;

/**
 * 充电桩类型枚举
 */
public enum ChargingPileType {
    AC("交流慢充"),
    DC("直流快充");

    private final String description;

    ChargingPileType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
