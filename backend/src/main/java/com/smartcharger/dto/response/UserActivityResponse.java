package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {

    private String rangeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer activeUserCount;
    private Integer newUserCount;
    private List<DailyActivityRecord> dailyRecords;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyActivityRecord {
        private String date;
        private Integer activeUserCount;
        private Integer newUserCount;
    }
}
