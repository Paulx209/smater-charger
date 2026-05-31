package com.smartcharger.task;

import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.ChargingRecord;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.service.WarningNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Sends repeated overtime occupancy notices for completed charging records
 * whose owners have not confirmed leaving.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OvertimeWarningTask {

    private final ChargingRecordRepository chargingRecordRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final WarningNoticeService warningNoticeService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void checkOvertimeCharging() {
        log.info("Start overtime occupancy warning task");

        List<ChargingRecord> notLeftRecords = chargingRecordRepository
                .findByStatusAndEndTimeIsNotNullAndLeaveTimeIsNull(ChargingRecordStatus.COMPLETED);

        int warningCount = 0;
        for (ChargingRecord record : notLeftRecords) {
            try {
                Optional<ChargingPile> pileOpt = chargingPileRepository.findById(record.getChargingPileId());
                if (pileOpt.isEmpty()) {
                    continue;
                }

                ChargingPile pile = pileOpt.get();
                if (pile.getStatus() == ChargingPileStatus.IDLE
                        || pile.getStatus() == ChargingPileStatus.WAITING_LEAVE
                        || pile.getStatus() == ChargingPileStatus.OVERTIME) {
                    pile.setStatus(ChargingPileStatus.OVERTIME);
                    chargingPileRepository.save(pile);
                }

                int minutes = (int) Duration.between(record.getEndTime(), LocalDateTime.now()).toMinutes();
                warningNoticeService.createOvertimeWarning(
                        record.getUserId(),
                        record.getChargingPileId(),
                        record.getId(),
                        pile.getCode(),
                        Math.max(0, minutes)
                );
                warningCount++;
            } catch (Exception e) {
                log.error("Failed to process overtime occupancy: recordId={}", record.getId(), e);
            }
        }

        log.info("Overtime occupancy warning task completed: warningCount={}", warningCount);
    }
}
