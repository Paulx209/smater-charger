package com.smartcharger.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 添加车辆请求DTO
 */
@Data
public class VehicleCreateRequest {

    /**
     * 车牌号（必填）
     */
    @NotBlank(message = "车牌号不能为空")
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

    /**
     * 是否设为默认车辆（0=否，1=是，默认0）
     */
    @Min(value = 0, message = "isDefault只能为0或1")
    @Max(value = 1, message = "isDefault只能为0或1")
    private Integer isDefault = 0;
}
