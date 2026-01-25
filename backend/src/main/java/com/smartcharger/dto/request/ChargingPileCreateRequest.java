package com.smartcharger.dto.request;

import com.smartcharger.entity.enums.ChargingPileType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 添加充电桩请求
 */
@Data
public class ChargingPileCreateRequest {

    @NotBlank(message = "充电桩编号不能为空")
    @Size(max = 50, message = "充电桩编号最多50字符")
    private String code;

    @NotBlank(message = "位置描述不能为空")
    @Size(max = 255, message = "位置描述最多255字符")
    private String location;

    @DecimalMin(value = "-180.0", message = "经度范围为-180到180")
    @DecimalMax(value = "180.0", message = "经度范围为-180到180")
    private BigDecimal lng;

    @DecimalMin(value = "-90.0", message = "纬度范围为-90到90")
    @DecimalMax(value = "90.0", message = "纬度范围为-90到90")
    private BigDecimal lat;

    @NotNull(message = "充电桩类型不能为空")
    private ChargingPileType type;

    @NotNull(message = "功率不能为空")
    @DecimalMin(value = "0.01", message = "功率必须大于0")
    private BigDecimal power;
}
