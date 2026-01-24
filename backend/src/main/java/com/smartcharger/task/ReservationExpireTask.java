package com.smartcharger.task;

import com.smartcharger.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约过期处理定时任务
 */
@Slf4j
@Component
public class ReservationExpireTask {

    private final ReservationService reservationService;

    public ReservationExpireTask(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 每分钟执行一次，处理过期的预约
     */
    @Scheduled(fixedRate = 60000)
    public void handleExpiredReservations() {
        log.debug("开始执行预约过期检查任务");
        try {
            reservationService.handleExpiredReservations();
        } catch (Exception e) {
            log.error("处理过期预约任务失败: {}", e.getMessage(), e);
        }
    }
}
