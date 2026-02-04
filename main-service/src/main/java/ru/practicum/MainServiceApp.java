package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.practicum.client.StatsClient;

@SpringBootApplication
public class MainServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(MainServiceApp.class, args);
    }

    @Bean
    public StatsClient statsClient() {
        // URL сервиса статистики - измените на нужный
        return new StatsClient("http://localhost:9090");
    }
}