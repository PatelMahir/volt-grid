package com.voltgrid.service;

import com.voltgrid.dto.CreateStationRequest;
import com.voltgrid.dto.StationDto;
import com.voltgrid.exception.ConflictException;
import com.voltgrid.exception.NotFoundException;
import com.voltgrid.model.Station;
import com.voltgrid.model.StationStatus;
import com.voltgrid.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    @Transactional(readOnly = true)
    public List<StationDto> list() {
        return stationRepository.findAll().stream().map(StationDto::from).toList();
    }

    /** Cached read path — station availability is queried far more than it changes. */
    @Cacheable(value = "stations", key = "#externalId")
    @Transactional(readOnly = true)
    public StationDto getByExternalId(String externalId) {
        return stationRepository.findByExternalId(externalId)
                .map(StationDto::from)
                .orElseThrow(() -> new NotFoundException("Station not found: " + externalId));
    }

    @CacheEvict(value = "stations", key = "#request.externalId()")
    @Transactional
    public StationDto register(CreateStationRequest request) {
        if (stationRepository.existsByExternalId(request.externalId())) {
            throw new ConflictException("Station already exists: " + request.externalId());
        }
        Station station = Station.builder()
                .externalId(request.externalId())
                .name(request.name())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .connectorType(request.connectorType())
                .powerKw(request.powerKw())
                .pricePerKwh(request.pricePerKwh())
                .status(StationStatus.AVAILABLE)
                .build();
        return StationDto.from(stationRepository.save(station));
    }

    /**
     * Applies a status reported by the station (over MQTT or HTTP) and stamps
     * last-seen. Auto-registers unknown stations so a freshly deployed unit is
     * never dropped. Evicts the cached view.
     */
    @CacheEvict(value = "stations", key = "#externalId")
    @Transactional
    public Station updateStatus(String externalId, StationStatus status) {
        Station station = stationRepository.findByExternalId(externalId)
                .orElseGet(() -> Station.builder()
                        .externalId(externalId)
                        .name(externalId)
                        .status(StationStatus.OFFLINE)
                        .build());
        station.setStatus(status);
        station.setLastSeenAt(Instant.now());
        return stationRepository.save(station);
    }

    /** Reserve an available station for a driver. */
    @CacheEvict(value = "stations", key = "#externalId")
    @Transactional
    public StationDto reserve(String externalId) {
        Station station = requireStation(externalId);
        if (station.getStatus() != StationStatus.AVAILABLE) {
            throw new ConflictException("Station not available for reservation: " + externalId);
        }
        station.setStatus(StationStatus.RESERVED);
        return StationDto.from(stationRepository.save(station));
    }

    @CacheEvict(value = "stations", key = "#externalId")
    @Transactional
    public StationDto release(String externalId) {
        Station station = requireStation(externalId);
        station.setStatus(StationStatus.AVAILABLE);
        return StationDto.from(stationRepository.save(station));
    }

    private Station requireStation(String externalId) {
        return stationRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Station not found: " + externalId));
    }
}
