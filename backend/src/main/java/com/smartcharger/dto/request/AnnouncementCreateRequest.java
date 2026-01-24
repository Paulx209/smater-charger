package com.smartcharger.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcharger.entity.enums.AnnouncementStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建公告请求
 */
@Data
public class AnnouncementCreateRequest {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题最多200字")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
