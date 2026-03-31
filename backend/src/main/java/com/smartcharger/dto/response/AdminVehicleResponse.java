package com.smartcharger.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminVehicleResponse {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String licensePlate;
    private String brand;
    private String model;
    private BigDecimal batteryCapacity;
    private Integer isDefault;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
