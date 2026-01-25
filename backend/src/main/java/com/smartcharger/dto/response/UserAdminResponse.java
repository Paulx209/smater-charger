package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理响应（管理端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 预警阈值（分钟）
     */
    private Integer warningThreshold;

    /**
     * 状态（1=启用，0=禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 统计数据
     */
    private UserStatisticsResponse statistics;

    /**
     * 车辆列表（仅详情接口返回）
     */
    private List<VehicleResponse> vehicles;

    /**
     * 最近充电记录（仅详情接口返回，最近5条）
     */
    private List<ChargingRecordResponse> recentChargingRecords;
}
