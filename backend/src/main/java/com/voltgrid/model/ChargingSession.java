package com.voltgrid.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charging_sessions", indexes = {
        @Index(name = "idx_session_station_time", columnList = "station_external_id, started_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "station_external_id", nullable = false, length = 128)
    private String stationExternalId;

    @Column(name = "energy_kwh", nullable = false)
    private Double energyKwh;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SessionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}
