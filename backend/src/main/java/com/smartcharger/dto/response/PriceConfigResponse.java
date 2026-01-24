package com.smartcharger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用配置响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceConfigResponse {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 充电桩类型
     */
    private String chargingPileType;

    /**
     * 每度电价格
     */
    private BigDecimal pricePerKwh;

    /**
     * 服务费
     */
    private BigDecimal serviceFee;

    /**
     * 生效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 是否激活
     */
    private Integer isActive;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}
