package com.voltgrid.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mqtt")
@Getter
@Setter
public class MqttProperties {
    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;
    private String inboundTopic;
    private String commandTopicPrefix;
}
