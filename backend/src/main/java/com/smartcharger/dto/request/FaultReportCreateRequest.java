package com.smartcharger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交故障报修请求
 */
@Data
public class FaultReportCreateRequest {

    @NotNull(message = "充电桩ID不能为空")
    private Long chargingPileId;

    @NotBlank(message = "故障描述不能为空")
    @Size(max = 500, message = "故障描述最多500字")
    private String description;
}
