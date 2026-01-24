package com.smartcharger.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置预警阈值请求DTO
 */
@Data
public class ThresholdConfigRequest {

    /**
     * 预警阈值（分钟），范围10-60
     */
    @NotNull(message = "预警阈值不能为空")
    @Min(value = 10, message = "预警阈值不能小于10分钟")
    @Max(value = 60, message = "预警阈值不能大于60分钟")
    private Integer threshold;
}
