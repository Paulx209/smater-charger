package com.smartcharger.dto.response;

import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电桩响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingPileResponse {

    /**
     * 充电桩ID
     */
    private Long id;

    /**
     * 充电桩编号
     */
    private String code;

    /**
     * 位置描述
     */
    private String location;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 类型
     */
    private ChargingPileType type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 功率（kW）
     */
    private BigDecimal power;

    /**
     * 状态
     */
    private ChargingPileStatus status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 距离（km），仅当传入经纬度时返回
     */
    private Double distance;

    /**
     * 充电记录数量（管理端）
     */
    private Integer chargingRecordCount;

    /**
     * 总营收（管理端）
     */
    private BigDecimal totalRevenue;

    /**
     * 统计数据（管理端详情）
     */
    private ChargingPileStatisticsResponse statistics;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
