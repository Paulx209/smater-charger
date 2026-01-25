package com.smartcharger.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充电桩统计数据响应
 */
@Data
@Builder
public class ChargingPileStatisticsResponse {

    private Integer totalChargingRecords;

    private BigDecimal totalElectricQuantity;

    private BigDecimal totalRevenue;

    private Integer avgChargingDuration;

    private BigDecimal utilizationRate;

    private Integer faultCount;

    private String lastChargingTime;
}
