package com.smartcharger.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class PasswordResetRequest {

    @Size(min = 8, message = "密码至少8位")
    private String newPassword;
}
