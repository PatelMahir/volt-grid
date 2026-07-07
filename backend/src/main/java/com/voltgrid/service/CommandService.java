package com.voltgrid.service;

import com.voltgrid.config.MqttProperties;
import com.voltgrid.dto.CommandRequest;
import com.voltgrid.mqtt.MqttGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Sends remote start/stop commands to a charging station over MQTT
 * ({@code commands/<externalId>}). The gateway is absent under the test profile,
 * so this degrades to a no-op send when MQTT is disabled.
 */
@Service
@RequiredArgsConstructor
public class CommandService {

    private final ObjectProvider<MqttGateway> gatewayProvider;
    private final MqttProperties props;
    private final StationService stationService;

    public void send(String stationExternalId, CommandRequest request) {
        // Validates the station exists (throws NotFoundException otherwise).
        stationService.getByExternalId(stationExternalId);
        String topic = props.getCommandTopicPrefix() + stationExternalId;
        MqttGateway gateway = gatewayProvider.getIfAvailable();
        if (gateway != null) {
            gateway.sendToStation(request.command().name(), topic);
        }
    }
}
