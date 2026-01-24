package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阈值配置响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdConfigResponse {

    /**
     * 预警阈值（分钟）
     */
    private Integer threshold;
}
