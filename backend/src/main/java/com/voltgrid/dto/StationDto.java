package com.voltgrid.dto;

import com.voltgrid.model.ConnectorType;
import com.voltgrid.model.Station;
import com.voltgrid.model.StationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StationDto(
        UUID id,
        String externalId,
        String name,
        Double latitude,
        Double longitude,
        ConnectorType connectorType,
        Double powerKw,
        BigDecimal pricePerKwh,
        StationStatus status,
        Instant lastSeenAt,
        Instant createdAt
) {
    public static StationDto from(Station s) {
        return new StationDto(s.getId(), s.getExternalId(), s.getName(),
                s.getLatitude(), s.getLongitude(), s.getConnectorType(),
                s.getPowerKw(), s.getPricePerKwh(), s.getStatus(),
                s.getLastSeenAt(), s.getCreatedAt());
    }
}
