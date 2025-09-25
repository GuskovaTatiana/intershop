package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.User;
import ru.yandex.practicum.shop.model.dto.UserRegistrationDto;
import ru.yandex.practicum.shop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public Mono<User> registerUser(UserRegistrationDto registrationDto) {
        return userRepository.existsByLogin(registrationDto.getLogin())
        .flatMap(usernameExists -> {
            if (usernameExists) {
                return Mono.error(new IllegalArgumentException("Username already exists"));
            }

            User user = new User();
            user.setLogin(registrationDto.getLogin());
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setFirstName(registrationDto.getFirstName());
            user.setLastName(registrationDto.getLastName());
            return userRepository.save(user);
        });
    }

    public Mono<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
