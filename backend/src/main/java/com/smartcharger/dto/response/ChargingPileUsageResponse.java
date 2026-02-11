package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingPileUsageResponse {

    private String rangeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalChargingPileCount;
    private Integer usedChargingPileCount;
    private BigDecimal chargingPileUsageRate;
    private Integer idleCount;
    private Integer chargingCount;
    private Integer faultCount;
    private Integer reservedCount;
    private Integer overtimeCount;
}
