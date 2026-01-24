package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 费用预估响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceEstimateResponse {

    /**
     * 充电量
     */
    private BigDecimal electricQuantity;

    /**
     * 每度电价格
     */
    private BigDecimal pricePerKwh;

    /**
     * 服务费单价
     */
    private BigDecimal serviceFee;

    /**
     * 总费用
     */
    private BigDecimal totalPrice;

    /**
     * 费用明细
     */
    private Breakdown breakdown;

    /**
     * 费用明细内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Breakdown {
        /**
         * 电费
         */
        private BigDecimal electricityFee;

        /**
         * 服务费总额
         */
        private BigDecimal serviceFee;
    }
}
