package com.smartcharger.entity.enums;

/**
 * 公告状态枚举
 */
public enum AnnouncementStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布");

    private final String description;

    AnnouncementStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
