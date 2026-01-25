package com.smartcharger.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量更新用户状态请求
 */
@Data
public class BatchUserStatusUpdateRequest {

    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String reason;
}
