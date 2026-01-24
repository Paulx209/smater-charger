package com.smartcharger.dto.request;

import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 附近充电桩查询请求DTO
 */
@Data
public class NearbyQueryRequest {

    /**
     * 中心点经度
     */
    @NotNull(message = "经度不能为空")
    private BigDecimal lng;

    /**
     * 中心点纬度
     */
    @NotNull(message = "纬度不能为空")
    private BigDecimal lat;

    /**
     * 搜索半径（km），默认5km
     */
    private Double radius = 5.0;

    /**
     * 充电桩类型
     */
    private ChargingPileType type;

    /**
     * 状态筛选
     */
    private ChargingPileStatus status;
}
