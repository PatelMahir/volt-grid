package com.voltgrid.dto;

import com.voltgrid.model.ChargingSession;
import com.voltgrid.model.SessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChargingSessionDto(
        UUID id,
        String stationExternalId,
        Double energyKwh,
        BigDecimal cost,
        SessionStatus status,
        Instant startedAt,
        Instant endedAt
) {
    public static ChargingSessionDto from(ChargingSession s) {
        return new ChargingSessionDto(s.getId(), s.getStationExternalId(), s.getEnergyKwh(),
                s.getCost(), s.getStatus(), s.getStartedAt(), s.getEndedAt());
    }
}
