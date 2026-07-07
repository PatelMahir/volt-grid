package com.voltgrid.mqtt;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * Outbound MQTT gateway. Backend calls this to push a command to a station;
 * the target topic is supplied per-message via the {@code topic} header.
 */
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {
    void sendToStation(String payload, @Header(MqttHeaders.TOPIC) String topic);
}
