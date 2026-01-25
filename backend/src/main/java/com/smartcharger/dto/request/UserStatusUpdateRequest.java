package com.smartcharger.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户状态请求
 */
@Data
public class UserStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String reason;
}
