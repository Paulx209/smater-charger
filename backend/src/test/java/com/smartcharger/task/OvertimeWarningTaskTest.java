package com.smartcharger.task;

import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.ChargingRecord;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.service.WarningNoticeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeWarningTaskTest {

    @Mock
    private ChargingRecordRepository chargingRecordRepository;

    @Mock
    private ChargingPileRepository chargingPileRepository;

    @Mock
    private WarningNoticeService warningNoticeService;

    @InjectMocks
    private OvertimeWarningTask task;

    @Test
    void checkOvertimeChargingSendsNoticeEveryRun() {
        ChargingRecord record = new ChargingRecord();
        record.setId(10L);
        record.setUserId(99L);
        record.setChargingPileId(1L);
        record.setStatus(ChargingRecordStatus.COMPLETED);
        record.setEndTime(LocalDateTime.now().minusMinutes(7));

        ChargingPile pile = new ChargingPile();
        pile.setId(1L);
        pile.setCode("P-001");
        pile.setStatus(ChargingPileStatus.WAITING_LEAVE);

        when(chargingRecordRepository.findByStatusAndEndTimeIsNotNullAndLeaveTimeIsNull(
                ChargingRecordStatus.COMPLETED)).thenReturn(List.of(record));
        when(chargingPileRepository.findById(1L)).thenReturn(Optional.of(pile));
        when(chargingPileRepository.save(any(ChargingPile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        task.checkOvertimeCharging();
        task.checkOvertimeCharging();

        assertThat(pile.getStatus()).isEqualTo(ChargingPileStatus.OVERTIME);
        verify(warningNoticeService, times(2)).createOvertimeWarning(
                eq(99L), eq(1L), eq(10L), eq("P-001"), any());
    }
}
