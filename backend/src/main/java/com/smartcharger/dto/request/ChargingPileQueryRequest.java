package com.smartcharger.dto.request;

import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充电桩查询请求DTO
 */
@Data
public class ChargingPileQueryRequest {

    /**
     * 位置关键词搜索
     */
    private String keyword;

    /**
     * 充电桩类型
     */
    private ChargingPileType type;

    /**
     * 充电桩状态
     */
    private ChargingPileStatus status;

    /**
     * 当前经度（用于距离排序）
     */
    private BigDecimal lng;

    /**
     * 当前纬度
     */
    private BigDecimal lat;

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页数量，默认10
     */
    private Integer size = 10;
}
