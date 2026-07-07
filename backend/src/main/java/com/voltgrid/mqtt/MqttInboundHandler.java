package com.voltgrid.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voltgrid.dto.StatusReading;
import com.voltgrid.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

/**
 * Consumes station status reports from {@code status/<externalId>}. Expects a
 * JSON payload of the form {@code {"status":"CHARGING","energyKwh":12.5}} and
 * drives the charging-session lifecycle. Malformed messages are logged and
 * dropped so one bad payload never stalls the subscription.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MqttInboundHandler implements MessageHandler {

    private static final String TOPIC_PREFIX = "status/";
    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = message.getPayload().toString();
        try {
            String stationId = extractStationId(topic);
            StatusReading reading = objectMapper.readValue(payload, StatusReading.class);
            sessionService.recordReading(stationId, reading);
        } catch (Exception ex) {
            log.warn("Dropping malformed status report on topic {}: {}", topic, ex.getMessage());
        }
    }

    private String extractStationId(String topic) {
        if (topic != null && topic.startsWith(TOPIC_PREFIX)) {
            return topic.substring(TOPIC_PREFIX.length());
        }
        throw new IllegalArgumentException("Unexpected topic: " + topic);
    }
}
