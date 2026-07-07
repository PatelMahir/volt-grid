package com.voltgrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voltgrid.controller.StationController;
import com.voltgrid.dto.CommandRequest;
import com.voltgrid.dto.CreateStationRequest;
import com.voltgrid.dto.StationDto;
import com.voltgrid.exception.GlobalExceptionHandler;
import com.voltgrid.exception.NotFoundException;
import com.voltgrid.model.ConnectorType;
import com.voltgrid.model.StationStatus;
import com.voltgrid.service.CommandService;
import com.voltgrid.service.SessionService;
import com.voltgrid.service.StationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StationController.class)
@Import(GlobalExceptionHandler.class)
class StationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean StationService stationService;
    @MockBean SessionService sessionService;
    @MockBean CommandService commandService;

    private StationDto sampleDto() {
        return new StationDto(UUID.randomUUID(), "st-1", "Downtown", 40.0, -74.0,
                ConnectorType.CCS, 150.0, new BigDecimal("0.30"),
                StationStatus.AVAILABLE, null, Instant.now());
    }

    @Test
    void post_createsStation() throws Exception {
        when(stationService.register(any(CreateStationRequest.class))).thenReturn(sampleDto());

        var body = new CreateStationRequest("st-1", "Downtown", 40.0, -74.0,
                ConnectorType.CCS, 150.0, new BigDecimal("0.30"));
        mockMvc.perform(post("/api/v1/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("st-1"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void post_rejectsBlankExternalId() throws Exception {
        var body = new CreateStationRequest("", "Downtown", 40.0, -74.0,
                ConnectorType.CCS, 150.0, new BigDecimal("0.30"));
        mockMvc.perform(post("/api/v1/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_returns404WhenMissing() throws Exception {
        when(stationService.getByExternalId("ghost")).thenThrow(new NotFoundException("nope"));

        mockMvc.perform(get("/api/v1/stations/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postCommand_isAccepted() throws Exception {
        mockMvc.perform(post("/api/v1/stations/st-1/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommandRequest(CommandRequest.Command.START))))
                .andExpect(status().isAccepted());

        verify(commandService).send(eq("st-1"), any(CommandRequest.class));
    }
}
