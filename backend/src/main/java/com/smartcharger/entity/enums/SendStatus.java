package com.smartcharger.entity.enums;

/**
 * 发送状态枚举
 */
public enum SendStatus {
    PENDING("待发送"),
    SENT("已发送"),
    FAILED("发送失败");

    private final String description;

    SendStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
