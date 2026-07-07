package com.voltgrid.controller;

import com.voltgrid.dto.*;
import com.voltgrid.service.CommandService;
import com.voltgrid.service.SessionService;
import com.voltgrid.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;
    private final SessionService sessionService;
    private final CommandService commandService;

    @GetMapping
    public List<StationDto> list() {
        return stationService.list();
    }

    @GetMapping("/{externalId}")
    public StationDto get(@PathVariable String externalId) {
        return stationService.getByExternalId(externalId);
    }

    @PostMapping
    public ResponseEntity<StationDto> register(@Valid @RequestBody CreateStationRequest request) {
        StationDto created = stationService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/stations/" + created.externalId())).body(created);
    }

    @PostMapping("/{externalId}/reserve")
    public StationDto reserve(@PathVariable String externalId) {
        return stationService.reserve(externalId);
    }

    @PostMapping("/{externalId}/release")
    public StationDto release(@PathVariable String externalId) {
        return stationService.release(externalId);
    }

    @GetMapping("/{externalId}/sessions")
    public List<ChargingSessionDto> sessions(
            @PathVariable String externalId,
            @RequestParam(defaultValue = "50") int limit) {
        return sessionService.recentSessions(externalId, PageRequest.of(0, Math.min(limit, 500)));
    }

    @GetMapping("/{externalId}/revenue")
    public Map<String, BigDecimal> revenue(@PathVariable String externalId) {
        return Map.of("totalRevenue", sessionService.totalRevenue(externalId));
    }

    @PostMapping("/{externalId}/commands")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendCommand(@PathVariable String externalId, @Valid @RequestBody CommandRequest request) {
        commandService.send(externalId, request);
    }
}
