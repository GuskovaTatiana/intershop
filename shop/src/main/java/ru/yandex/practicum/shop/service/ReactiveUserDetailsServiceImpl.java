package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.User;
import ru.yandex.practicum.shop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl  implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByLogin(username)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new UsernameNotFoundException("User not found: " + username))))
                .map(this::convertToUserDetails);
    }

    private UserDetails convertToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPassword())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}
