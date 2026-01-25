package com.smartcharger.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量删除结果响应
 */
@Data
@Builder
public class BatchDeleteResultResponse {

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private List<FailedRecord> failedRecords;

    @Data
    @Builder
    public static class FailedRecord {
        private Long id;
        private String code;
        private String reason;
    }
}
