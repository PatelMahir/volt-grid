package com.voltgrid;

import com.voltgrid.dto.StatusReading;
import com.voltgrid.model.ChargingSession;
import com.voltgrid.model.SessionStatus;
import com.voltgrid.model.Station;
import com.voltgrid.model.StationStatus;
import com.voltgrid.repository.ChargingSessionRepository;
import com.voltgrid.repository.StationRepository;
import com.voltgrid.service.SessionService;
import com.voltgrid.service.StationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock ChargingSessionRepository sessionRepository;
    @Mock StationRepository stationRepository;
    @Mock StationService stationService;
    @InjectMocks SessionService sessionService;

    @Test
    void charging_opensSessionWhenNoneActive() {
        when(sessionRepository.findFirstByStationExternalIdAndStatusOrderByStartedAtDesc(
                "st-1", SessionStatus.ACTIVE)).thenReturn(Optional.empty());

        sessionService.recordReading("st-1", new StatusReading(StationStatus.CHARGING, 4.2));

        verify(stationService).updateStatus("st-1", StationStatus.CHARGING);
        ArgumentCaptor<ChargingSession> captor = ArgumentCaptor.forClass(ChargingSession.class);
        verify(sessionRepository).save(captor.capture());
        ChargingSession opened = captor.getValue();
        assertThat(opened.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(opened.getEnergyKwh()).isEqualTo(4.2);
        assertThat(opened.getStartedAt()).isNotNull();
    }

    @Test
    void available_finalizesActiveSessionAndComputesCost() {
        ChargingSession active = ChargingSession.builder()
                .stationExternalId("st-1").energyKwh(0.0)
                .status(SessionStatus.ACTIVE).startedAt(java.time.Instant.now()).build();
        when(sessionRepository.findFirstByStationExternalIdAndStatusOrderByStartedAtDesc(
                "st-1", SessionStatus.ACTIVE)).thenReturn(Optional.of(active));
        when(stationRepository.findByExternalId("st-1")).thenReturn(Optional.of(
                Station.builder().externalId("st-1").pricePerKwh(new BigDecimal("0.30")).build()));

        // Station reports 10 kWh delivered, now available again.
        sessionService.recordReading("st-1", new StatusReading(StationStatus.AVAILABLE, 10.0));

        verify(stationService).updateStatus("st-1", StationStatus.AVAILABLE);
        ArgumentCaptor<ChargingSession> captor = ArgumentCaptor.forClass(ChargingSession.class);
        verify(sessionRepository).save(captor.capture());
        ChargingSession closed = captor.getValue();
        assertThat(closed.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(closed.getEnergyKwh()).isEqualTo(10.0);
        assertThat(closed.getCost()).isEqualByComparingTo("3.00"); // 10 kWh * 0.30
        assertThat(closed.getEndedAt()).isNotNull();
    }

    @Test
    void available_withNoActiveSession_doesNotSaveSession() {
        when(sessionRepository.findFirstByStationExternalIdAndStatusOrderByStartedAtDesc(
                "st-1", SessionStatus.ACTIVE)).thenReturn(Optional.empty());

        sessionService.recordReading("st-1", new StatusReading(StationStatus.AVAILABLE, null));

        verify(stationService).updateStatus(eq("st-1"), eq(StationStatus.AVAILABLE));
        verify(sessionRepository, never()).save(any());
    }
}
