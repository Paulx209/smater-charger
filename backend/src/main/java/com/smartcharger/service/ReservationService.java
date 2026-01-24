package com.smartcharger.service;

import com.smartcharger.dto.request.ReservationCreateRequest;
import com.smartcharger.dto.response.ReservationResponse;
import com.smartcharger.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;

/**
 * 预约服务接口
 */
public interface ReservationService {

    /**
     * 创建预约
     */
    ReservationResponse createReservation(Long userId, ReservationCreateRequest request);

    /**
     * 取消预约
     */
    void cancelReservation(Long userId, Long id);

    /**
     * 查询我的预约列表
     */
    Page<ReservationResponse> getMyReservations(Long userId, ReservationStatus status,
                                                 Integer page, Integer size);

    /**
     * 获取预约详情
     */
    ReservationResponse getReservationById(Long userId, Long id);

    /**
     * 获取当前进行中的预约
     */
    ReservationResponse getCurrentReservation(Long userId);

    /**
     * 处理过期预约（定时任务调用）
     */
    void handleExpiredReservations();
}
