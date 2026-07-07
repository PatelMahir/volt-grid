package com.voltgrid.repository;

import com.voltgrid.model.ChargingSession;
import com.voltgrid.model.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession, UUID> {

    Page<ChargingSession> findByStationExternalIdOrderByStartedAtDesc(String stationExternalId, Pageable pageable);

    Optional<ChargingSession> findFirstByStationExternalIdAndStatusOrderByStartedAtDesc(
            String stationExternalId, SessionStatus status);

    @Query("select coalesce(sum(s.cost), 0) from ChargingSession s " +
           "where s.stationExternalId = :stationExternalId and s.status = com.voltgrid.model.SessionStatus.COMPLETED")
    BigDecimal totalRevenue(@Param("stationExternalId") String stationExternalId);
}
