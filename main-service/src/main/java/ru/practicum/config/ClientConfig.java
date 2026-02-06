// main-service/src/main/java/ru/practicum/config/ClientConfig.java
package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.client.StatsClient;

@Configuration
public class ClientConfig {

    @Value("${stats.service.url}")
    private String statsServerUrl;

    @Bean
    public StatsClient statsClient() {
        return new StatsClient(statsServerUrl);
    }
}