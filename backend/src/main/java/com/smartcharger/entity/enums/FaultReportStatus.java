package com.smartcharger.entity.enums;

/**
 * 故障报修状态枚举
 */
public enum FaultReportStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    RESOLVED("已修复");

    private final String description;

    FaultReportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
