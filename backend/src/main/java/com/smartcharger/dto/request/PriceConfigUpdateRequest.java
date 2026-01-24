package com.smartcharger.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新费用配置请求DTO
 */
@Data
public class PriceConfigUpdateRequest {

    /**
     * 每度电价格（可选，>0）
     */
    @DecimalMin(value = "0.01", message = "每度电价格必须大于0")
    private BigDecimal pricePerKwh;

    /**
     * 服务费（可选，>=0）
     */
    @DecimalMin(value = "0", message = "服务费不能为负数")
    private BigDecimal serviceFee;

    /**
     * 是否激活（可选）
     */
    private Integer isActive;
}
