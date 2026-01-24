package com.smartcharger.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建费用配置请求DTO
 */
@Data
public class PriceConfigCreateRequest {

    /**
     * 充电桩类型（必填，AC/DC）
     */
    @NotBlank(message = "充电桩类型不能为空")
    private String chargingPileType;

    /**
     * 每度电价格（必填，>0）
     */
    @NotNull(message = "每度电价格不能为空")
    @DecimalMin(value = "0.01", message = "每度电价格必须大于0")
    private BigDecimal pricePerKwh;

    /**
     * 服务费（必填，>=0）
     */
    @NotNull(message = "服务费不能为空")
    @DecimalMin(value = "0", message = "服务费不能为负数")
    private BigDecimal serviceFee;

    /**
     * 生效开始时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 生效结束时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 是否激活（可选，默认1）
     */
    private Integer isActive = 1;
}
