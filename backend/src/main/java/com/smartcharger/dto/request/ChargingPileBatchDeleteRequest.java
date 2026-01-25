package com.smartcharger.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除充电桩请求
 */
@Data
public class ChargingPileBatchDeleteRequest {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
