package com.smartcharger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcharger.entity.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private Long chargingPileId;

    private String chargingPileCode;

    private String chargingPileLocation;

    private BigDecimal chargingPileLng;

    private BigDecimal chargingPileLat;

    private String chargingPileType;

    private String chargingPileTypeDesc;

    private BigDecimal chargingPilePower;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private ReservationStatus status;

    private String statusDesc;

    private Long remainingMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}