package com.smartcharger.dto.request;

import com.smartcharger.entity.enums.ChargingPileStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新充电桩状态请求
 */
@Data
public class ChargingPileStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    private ChargingPileStatus status;
}
