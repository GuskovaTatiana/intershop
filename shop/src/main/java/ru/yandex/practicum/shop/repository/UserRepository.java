package ru.yandex.practicum.shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.User;


public interface UserRepository extends R2dbcRepository<User, Integer> {
    Mono<User> findByLogin(String login);

    Mono<Boolean> existsByLogin(String login);
}
