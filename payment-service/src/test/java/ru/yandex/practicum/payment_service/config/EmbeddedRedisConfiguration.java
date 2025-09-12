package ru.yandex.practicum.payment_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import redis.embedded.RedisServer;


import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfiguration {

    /**
     * Создаём в контексте бин RedisServer
     */
    @Bean(destroyMethod = "stop") // Останавливаем сервер при закрытии контекста
    public RedisServer redisServer() throws IOException {
        var redisServer = new RedisServer();
        redisServer.start(); // Запускаем прямо во время инициализации бина
        return redisServer;
    }


}
