package com.smartcharger.entity.enums;

public enum ChargingEndReason {
    USER_MANUAL("用户手动结束"),
    AUTO_TARGET_REACHED("达到目标自动结束");

    private final String description;

    ChargingEndReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
