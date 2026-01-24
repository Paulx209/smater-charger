package com.smartcharger.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 故障统计响应
 */
@Data
@Builder
public class FaultStatisticsResponse {

    private Integer totalCount;

    private Integer pendingCount;

    private Integer processingCount;

    private Integer resolvedCount;

    private Integer avgHandleTime;

    private List<TopFaultPile> topFaultPiles;

    @Data
    @Builder
    public static class TopFaultPile {
        private Long chargingPileId;
        private String pileName;
        private Integer faultCount;
    }
}
