package com.smartcharger.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求DTO
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Size(max = 255, message = "头像URL长度不能超过255")
    private String avatar;

    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String name;

    private Integer warningThreshold;
}
