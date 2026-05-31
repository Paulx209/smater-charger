package com.smartcharger.dto.request;

import com.smartcharger.entity.enums.ChargingTargetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Start charging request DTO.
 */
@Data
public class ChargingRecordStartRequest {

    @NotNull(message = "充电桩ID不能为空")
    private Long chargingPileId;

    private Long vehicleId;

    @NotNull(message = "充电目标类型不能为空")
    private ChargingTargetType targetType;

    @NotNull(message = "充电目标值不能为空")
    @DecimalMin(value = "0.01", message = "充电目标值必须大于0")
    private BigDecimal targetValue;
}
