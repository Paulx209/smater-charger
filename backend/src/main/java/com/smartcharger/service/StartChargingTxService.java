package com.smartcharger.service;

import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;

/**
 * 开始充电事务服务接口
 * 用于分离分布式锁和事务边界
 */
public interface StartChargingTxService {

    /**
     * 在事务中执行开始充电逻辑
     *
     * @param userId 用户ID
     * @param request 开始充电请求
     * @return 充电记录响应
     */
    ChargingRecordResponse startChargingInTx(Long userId, ChargingRecordStartRequest request);
}
