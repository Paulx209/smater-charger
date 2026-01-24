package com.smartcharger.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始充电请求DTO
 */
@Data
public class ChargingRecordStartRequest {

    /**
     * 充电桩ID（必填）
     */
    @NotNull(message = "充电桩ID不能为空")
    private Long chargingPileId;

    /**
     * 车辆ID（可选）
     */
    private Long vehicleId;
}
