package com.smartcharger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcharger.entity.enums.ChargingPileType;
import com.smartcharger.entity.enums.FaultReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 故障报修响应
 */
@Data
@Builder
public class FaultReportResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String userPhone;

    private Long chargingPileId;

    private String pileName;

    private String pileLocation;

    private ChargingPileType pileType;

    private String description;

    private FaultReportStatus status;

    private String statusDesc;

    private Long handlerId;

    private String handlerName;

    private String handleRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}
