package com.voltgrid.controller;

import com.voltgrid.dto.StatusReading;
import com.voltgrid.service.SessionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP status ingestion — an alternative to MQTT for stations that speak REST.
 */
@RestController
@RequestMapping("/api/v1/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final SessionService sessionService;

    @PostMapping("/{externalId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@PathVariable @NotBlank String externalId, @RequestBody StatusReading reading) {
        sessionService.recordReading(externalId, reading);
    }
}
