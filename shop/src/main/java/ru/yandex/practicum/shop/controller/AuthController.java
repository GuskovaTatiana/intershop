package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.dto.AuthRequestDTO;
import ru.yandex.practicum.shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.shop.model.dto.UserRegistrationDto;
import ru.yandex.practicum.shop.service.UserService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final ReactiveAuthenticationManager authenticationManager;

    @PostMapping("/auth")
    public Mono<String> auth(@ModelAttribute AuthRequestDTO authDTO,
                             ServerWebExchange exchange) {

        return Mono.just(new UsernamePasswordAuthenticationToken(authDTO.getLogin(), authDTO.getPassword()))
                .flatMap(authenticationManager::authenticate)
                .flatMap(authentication -> {
                    // Сохраняем аутентификацию в сессии
                    return exchange.getSession()
                            .doOnNext(session -> {
                                // Очищаем старую сессию и создаем новую для безопасности
                                session.getAttributes().put("SPRING_SECURITY_CONTEXT",
                                        new SecurityContextImpl(authentication));
                            })
                            .then(Mono.just("redirect:/product"));
                })
                .onErrorResume(e -> {
                    log.error("Authentication error", e);
                    return Mono.just("redirect:/" + buildRedirectUrl("product", "Неверный логин или пароль"));
                });
    }

    @GetMapping("/logout")
    public Mono<String> logout(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    // Очищаем атрибуты сессии
                    session.getAttributes().remove("SPRING_SECURITY_CONTEXT");

                    // Инвалидируем сессию
                    return Mono.fromRunnable(session::invalidate)
                            .then(Mono.just(session));
                })
                .then(Mono.defer(() -> {
                    // Очищаем атрибуты exchange
                    exchange.getAttributes().remove(SecurityContext.class.getName());

                    // Добавляем параметр для отображения сообщения о успешном выходе
                    return Mono.just("redirect:/product?logoutSuccess=true");
                }))
                .onErrorResume(e -> {
                    log.error("Logout error", e);
                    // В случае ошибки все равно редиректим, но без параметра успеха
                    return Mono.just("redirect:/product");
                });
    }

    @PostMapping("/register")
    public Mono<String> register(@ModelAttribute UserRegistrationDto registrationDto) {
        return userService.registerUser(registrationDto)
                .then(Mono.just("redirect:/product"))
                .onErrorResume(e -> Mono.just("redirect:/register?error"));
    }


    private String buildRedirectUrl(String baseUrl, String errorText) {
        if (errorText == null) {
            return baseUrl;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(baseUrl);
        builder.queryParam("error", errorText);

        return builder.toUriString();
    }
}
