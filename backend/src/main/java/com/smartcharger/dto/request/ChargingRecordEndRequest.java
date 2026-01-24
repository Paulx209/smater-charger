package com.smartcharger.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结束充电请求DTO
 */
@Data
public class ChargingRecordEndRequest {

    /**
     * 充电量（必填，>0）
     */
    @NotNull(message = "充电量不能为空")
    @DecimalMin(value = "0.01", message = "充电量必须大于0")
    private BigDecimal electricQuantity;
}
