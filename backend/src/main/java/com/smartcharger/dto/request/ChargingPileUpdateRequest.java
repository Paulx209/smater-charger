package com.smartcharger.dto.request;

import com.smartcharger.entity.enums.ChargingPileType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新充电桩请求
 */
@Data
public class ChargingPileUpdateRequest {

    @Size(max = 50, message = "充电桩编号最多50字符")
    private String code;

    @Size(max = 255, message = "位置描述最多255字符")
    private String location;

    @DecimalMin(value = "-180.0", message = "经度范围为-180到180")
    @DecimalMax(value = "180.0", message = "经度范围为-180到180")
    private BigDecimal lng;

    @DecimalMin(value = "-90.0", message = "纬度范围为-90到90")
    @DecimalMax(value = "90.0", message = "纬度范围为-90到90")
    private BigDecimal lat;

    private ChargingPileType type;

    @DecimalMin(value = "0.01", message = "功率必须大于0")
    private BigDecimal power;
}
