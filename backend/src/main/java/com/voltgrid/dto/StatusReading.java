package com.voltgrid.dto;

import com.voltgrid.model.StationStatus;

/**
 * Payload a station publishes to {@code status/<externalId>} over MQTT, or posts
 * to the HTTP ingestion endpoint. {@code energyKwh} is the session meter reading.
 */
public record StatusReading(
        StationStatus status,
        Double energyKwh
) {}
