package com.smartcharger.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Excel导入结果响应
 */
@Data
@Builder
public class ImportResultResponse {

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private List<FailedRecord> failedRecords;

    @Data
    @Builder
    public static class FailedRecord {
        private Integer row;
        private String code;
        private String reason;
    }
}
