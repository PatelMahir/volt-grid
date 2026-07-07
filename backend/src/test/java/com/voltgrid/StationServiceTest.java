package com.voltgrid;

import com.voltgrid.dto.CreateStationRequest;
import com.voltgrid.dto.StationDto;
import com.voltgrid.exception.ConflictException;
import com.voltgrid.exception.NotFoundException;
import com.voltgrid.model.ConnectorType;
import com.voltgrid.model.Station;
import com.voltgrid.model.StationStatus;
import com.voltgrid.repository.StationRepository;
import com.voltgrid.service.StationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock StationRepository stationRepository;
    @InjectMocks StationService stationService;

    private CreateStationRequest req(String id) {
        return new CreateStationRequest(id, "Downtown", 40.0, -74.0,
                ConnectorType.CCS, 150.0, new BigDecimal("0.30"));
    }

    @Test
    void register_persistsNewStationAsAvailable() {
        when(stationRepository.existsByExternalId("st-1")).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        StationDto dto = stationService.register(req("st-1"));

        assertThat(dto.externalId()).isEqualTo("st-1");
        assertThat(dto.status()).isEqualTo(StationStatus.AVAILABLE);
        assertThat(dto.pricePerKwh()).isEqualByComparingTo("0.30");
    }

    @Test
    void register_rejectsDuplicate() {
        when(stationRepository.existsByExternalId("dup")).thenReturn(true);

        assertThatThrownBy(() -> stationService.register(req("dup")))
                .isInstanceOf(ConflictException.class);
        verify(stationRepository, never()).save(any());
    }

    @Test
    void getByExternalId_throwsWhenMissing() {
        when(stationRepository.findByExternalId("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stationService.getByExternalId("ghost"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatus_autoRegistersUnknownStation() {
        when(stationRepository.findByExternalId("new-st")).thenReturn(Optional.empty());
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        stationService.updateStatus("new-st", StationStatus.CHARGING);

        ArgumentCaptor<Station> captor = ArgumentCaptor.forClass(Station.class);
        verify(stationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StationStatus.CHARGING);
        assertThat(captor.getValue().getLastSeenAt()).isNotNull();
    }

    @Test
    void reserve_rejectsWhenNotAvailable() {
        Station charging = Station.builder().externalId("st-1").status(StationStatus.CHARGING).build();
        when(stationRepository.findByExternalId("st-1")).thenReturn(Optional.of(charging));

        assertThatThrownBy(() -> stationService.reserve("st-1"))
                .isInstanceOf(ConflictException.class);
    }
}
