package com.voltgrid.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true, length = 128)
    private String externalId;

    @Column(nullable = false, length = 200)
    private String name;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", length = 16)
    private ConnectorType connectorType;

    @Column(name = "power_kw")
    private Double powerKw;

    @Column(name = "price_per_kwh", precision = 8, scale = 4)
    private BigDecimal pricePerKwh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StationStatus status;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = StationStatus.OFFLINE;
    }
}
