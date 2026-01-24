package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 充电桩可用性检查响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCheckResponse {

    /**
     * 是否可用
     */
    private Boolean available;

    /**
     * 不可用原因
     */
    private String reason;

    /**
     * 冲突的预约列表（如果有）
     */
    private List<ConflictReservation> conflictReservations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictReservation {
        private Long reservationId;
        private String startTime;
        private String endTime;
    }
}
