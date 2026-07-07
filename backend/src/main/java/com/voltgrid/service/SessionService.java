package com.voltgrid.service;

import com.voltgrid.dto.ChargingSessionDto;
import com.voltgrid.dto.StatusReading;
import com.voltgrid.model.ChargingSession;
import com.voltgrid.model.SessionStatus;
import com.voltgrid.model.Station;
import com.voltgrid.model.StationStatus;
import com.voltgrid.repository.ChargingSessionRepository;
import com.voltgrid.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final ChargingSessionRepository sessionRepository;
    private final StationRepository stationRepository;
    private final StationService stationService;

    /**
     * Applies a status/meter reading from a station and drives the charging
     * session lifecycle:
     * <ul>
     *   <li>{@code CHARGING} — opens a session if none is active, then records energy.</li>
     *   <li>any other status — finalizes the active session, computing cost from
     *       the station's per-kWh price.</li>
     * </ul>
     */
    @Transactional
    public void recordReading(String stationExternalId, StatusReading reading) {
        StationStatus status = reading.status() != null ? reading.status() : StationStatus.AVAILABLE;
        stationService.updateStatus(stationExternalId, status);

        Optional<ChargingSession> active = sessionRepository
                .findFirstByStationExternalIdAndStatusOrderByStartedAtDesc(stationExternalId, SessionStatus.ACTIVE);

        if (status == StationStatus.CHARGING) {
            ChargingSession session = active.orElseGet(() -> ChargingSession.builder()
                    .stationExternalId(stationExternalId)
                    .energyKwh(0.0)
                    .status(SessionStatus.ACTIVE)
                    .startedAt(Instant.now())
                    .build());
            if (reading.energyKwh() != null) {
                session.setEnergyKwh(reading.energyKwh());
            }
            sessionRepository.save(session);
        } else {
            active.ifPresent(session -> {
                if (reading.energyKwh() != null) {
                    session.setEnergyKwh(reading.energyKwh());
                }
                session.setStatus(SessionStatus.COMPLETED);
                session.setEndedAt(Instant.now());
                session.setCost(computeCost(stationExternalId, session.getEnergyKwh()));
                sessionRepository.save(session);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<ChargingSessionDto> recentSessions(String stationExternalId, Pageable pageable) {
        return sessionRepository
                .findByStationExternalIdOrderByStartedAtDesc(stationExternalId, pageable)
                .map(ChargingSessionDto::from)
                .getContent();
    }

    @Transactional(readOnly = true)
    public BigDecimal totalRevenue(String stationExternalId) {
        return sessionRepository.totalRevenue(stationExternalId);
    }

    private BigDecimal computeCost(String stationExternalId, double energyKwh) {
        BigDecimal price = stationRepository.findByExternalId(stationExternalId)
                .map(Station::getPricePerKwh)
                .orElse(BigDecimal.ZERO);
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(energyKwh)).setScale(2, RoundingMode.HALF_UP);
    }
}
