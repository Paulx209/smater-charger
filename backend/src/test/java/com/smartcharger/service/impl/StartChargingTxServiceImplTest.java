package com.smartcharger.service.impl;

import com.smartcharger.dto.request.ChargingRecordStartRequest;
import com.smartcharger.dto.response.ChargingRecordResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.ChargingRecord;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import com.smartcharger.entity.enums.ChargingTargetType;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.repository.ReservationRepository;
import com.smartcharger.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartChargingTxServiceImplTest {

    @Mock
    private ChargingRecordRepository chargingRecordRepository;

    @Mock
    private ChargingPileRepository chargingPileRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private StartChargingTxServiceImpl service;

    @Test
    void startChargingWithDurationTargetStoresEstimatedKwhAndEndTime() {
        ChargingPile pile = buildIdlePile(new BigDecimal("6.00"));
        when(chargingRecordRepository.findByUserIdAndStatus(any(), any())).thenReturn(Optional.empty());
        when(chargingPileRepository.findById(1L)).thenReturn(Optional.of(pile));
        when(reservationRepository.findByChargingPileIdAndStatusAndEndTimeAfter(any(), any(), any()))
                .thenReturn(List.of());
        when(chargingRecordRepository.save(any(ChargingRecord.class))).thenAnswer(invocation -> {
            ChargingRecord record = invocation.getArgument(0);
            record.setId(10L);
            return record;
        });
        when(chargingPileRepository.save(any(ChargingPile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargingRecordStartRequest request = new ChargingRecordStartRequest();
        request.setChargingPileId(1L);
        request.setTargetType(ChargingTargetType.DURATION);
        request.setTargetValue(new BigDecimal("30"));

        ChargingRecordResponse response = service.startChargingInTx(99L, request);

        assertThat(response.getTargetDurationMinutes()).isEqualTo(30);
        assertThat(response.getTargetKwh()).isEqualByComparingTo("3.000");
        assertThat(response.getTargetEndTime()).isNotNull();
        assertThat(pile.getStatus()).isEqualTo(ChargingPileStatus.CHARGING);
    }

    @Test
    void startChargingWithKwhTargetStoresEstimatedDuration() {
        ChargingPile pile = buildIdlePile(new BigDecimal("6.00"));
        when(chargingRecordRepository.findByUserIdAndStatus(any(), any())).thenReturn(Optional.empty());
        when(chargingPileRepository.findById(1L)).thenReturn(Optional.of(pile));
        when(reservationRepository.findByChargingPileIdAndStatusAndEndTimeAfter(any(), any(), any()))
                .thenReturn(List.of());
        when(chargingRecordRepository.save(any(ChargingRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chargingPileRepository.save(any(ChargingPile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargingRecordStartRequest request = new ChargingRecordStartRequest();
        request.setChargingPileId(1L);
        request.setTargetType(ChargingTargetType.KWH);
        request.setTargetValue(new BigDecimal("5"));

        ChargingRecordResponse response = service.startChargingInTx(99L, request);

        assertThat(response.getTargetDurationMinutes()).isEqualTo(50);
        assertThat(response.getTargetKwh()).isEqualByComparingTo("5.000");
        assertThat(response.getTargetEndTime()).isNotNull();
    }

    private ChargingPile buildIdlePile(BigDecimal power) {
        ChargingPile pile = new ChargingPile();
        pile.setId(1L);
        pile.setCode("P-001");
        pile.setLocation("A1");
        pile.setType(ChargingPileType.AC);
        pile.setPower(power);
        pile.setStatus(ChargingPileStatus.IDLE);
        return pile;
    }
}
