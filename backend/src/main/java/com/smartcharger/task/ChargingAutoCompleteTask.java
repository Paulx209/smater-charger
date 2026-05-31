package com.smartcharger.task;

import com.smartcharger.service.ChargingRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChargingAutoCompleteTask {

    private final ChargingRecordService chargingRecordService;

    @Scheduled(fixedRate = 60000)
    public void autoCompleteDueChargingRecords() {
        log.debug("Start auto complete charging task");
        chargingRecordService.autoCompleteDueChargingRecords();
    }

    @Scheduled(fixedRate = 60000)
    public void sendPreEndChargingReminders() {
        log.debug("Start pre-end charging reminder task");
        chargingRecordService.sendPreEndChargingReminders();
    }
}
