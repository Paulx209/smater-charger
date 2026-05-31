package com.smartcharger.entity.enums;

public enum ChargingTargetType {
    DURATION("按时长"),
    KWH("按电量");

    private final String description;

    ChargingTargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
