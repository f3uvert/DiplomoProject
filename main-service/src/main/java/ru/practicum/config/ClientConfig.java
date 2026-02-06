// main-service/src/main/java/ru/practicum/config/ClientConfig.java
package ru.practicum.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.practicum.client.StatsClient;

@Configuration
@Slf4j
public class ClientConfig {

    @Value("${stats.service.docker.url:http://stats-server:9090}")
    private String statsServerUrlDocker;

    @Value("${stats.service.host.url:http://localhost:9090}")
    private String statsServerUrlHost;

    @Bean
    public StatsClient statsClient() {
        // Для работы внутри Docker используем docker URL
        log.info("Creating StatsClient with Docker URL: {}", statsServerUrlDocker);
        return new StatsClient(statsServerUrlDocker);
    }

    @Bean
    @Primary
    public StatsClient statsClientForApp() {
        return statsClient();
    }
}