package com.smartcharger.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建预约请求DTO
 */
@Data
public class ReservationCreateRequest {

    /**
     * 充电桩ID（必填）
     */
    @NotNull(message = "充电桩ID不能为空")
    private Long chargingPileId;

    /**
     * 预约开始时间（可选，默认当前时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
}
