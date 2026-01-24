package com.smartcharger.dto.request;

import com.smartcharger.entity.enums.FaultReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 处理故障报修请求
 */
@Data
public class FaultReportHandleRequest {

    @NotNull(message = "处理状态不能为空")
    private FaultReportStatus status;

    @Size(max = 500, message = "处理备注最多500字")
    private String handleRemark;
}
