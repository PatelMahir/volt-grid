package com.voltgrid.config;

import com.voltgrid.mqtt.MqttInboundHandler;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

/**
 * Wires the Paho MQTT client into Spring Integration channels. Disabled under
 * the {@code test} profile so the test context does not require a live broker.
 */
@Configuration
@Profile("!test")
@IntegrationComponentScan(basePackages = "com.voltgrid.mqtt")
@RequiredArgsConstructor
public class MqttConfig {

    private final MqttProperties props;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{props.getBrokerUrl()});
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        if (StringUtils.hasText(props.getUsername())) {
            options.setUserName(props.getUsername());
            options.setPassword(props.getPassword().toCharArray());
        }
        factory.setConnectionOptions(options);
        return factory;
    }

    // ---- Inbound: status/+ -> handler ----

    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        props.getClientId() + "-in", mqttClientFactory(), props.getInboundTopic());
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInboundChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public MessageHandler inboundHandler(MqttInboundHandler handler) {
        return handler;
    }

    // ---- Outbound: backend -> commands/<externalId> ----

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(props.getClientId() + "-out", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultQos(1);
        return handler;
    }
}
