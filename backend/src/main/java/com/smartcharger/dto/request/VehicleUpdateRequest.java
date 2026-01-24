package com.smartcharger.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新车辆请求DTO
 */
@Data
public class VehicleUpdateRequest {

    /**
     * 车牌号（可选）
     */
    @Pattern(
        regexp = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$",
        message = "车牌号格式不正确"
    )
    private String licensePlate;

    /**
     * 品牌（可选）
     */
    @Size(max = 50, message = "品牌长度不能超过50")
    private String brand;

    /**
     * 车型（可选）
     */
    @Size(max = 50, message = "车型长度不能超过50")
    private String model;

    /**
     * 电池容量（kWh，可选）
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "电池容量必须大于0")
    @DecimalMax(value = "200.0", message = "电池容量不能超过200kWh")
    private BigDecimal batteryCapacity;
}
