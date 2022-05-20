package com.itsamekawa.anyabot.config;

import com.itsamekawa.anyabot.util.EventListener;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource("classpath:applicationSECRET.yml")
public class BotConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotConfiguration.class);

    @Value("${token}")
    private String token;

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<EventListener<T>> eventListeners) {
        GatewayDiscordClient client = null;

        try {
            client = DiscordClientBuilder.create(token)
                    .build()
                    .login()
                    .block();

            for(EventListener<T> listener : eventListeners) {
                assert client != null;
                client.on(listener.getEventType())
                        .flatMap(listener::execute)
                        .onErrorResume(listener::handleError)
                        .subscribe();
            }
        }
        catch (Exception e) {
            LOGGER.error("invalid token", e);
        }

        return client;
    }
}