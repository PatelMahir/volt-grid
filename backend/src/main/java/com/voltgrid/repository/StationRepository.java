package com.voltgrid.repository;

import com.voltgrid.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StationRepository extends JpaRepository<Station, UUID> {
    Optional<Station> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
}
