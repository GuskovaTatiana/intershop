package ru.yandex.practicum.shop.config;

import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.repository.UserRepository;
import ru.yandex.practicum.shop.service.ReactiveUserDetailsServiceImpl;
import org.springframework.web.server.WebSession;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    ReactiveUserDetailsService reactiveUserDetailsService(UserRepository userRepository) {
//        return new ReactiveUserDetailsServiceImpl(userRepository);
//    }
    @Bean
    public ReactiveAuthenticationManager authenticationManager(
            ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .pathMatchers("/product/**", "/login/**", "/register", "/auth").permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/product")
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/product"))
                        .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/product?error"))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((exchange, authentication) ->
                                exchange.getExchange().getSession()
                                       .flatMap(WebSession::invalidate) // удаляем сессию
                                        .then(Mono.fromRunnable(() -> {
                                            exchange.getExchange().getResponse()
                                                    .setStatusCode(HttpStatus.OK); // отвечаем 200 OK
                                        }))
                        )
                )
                .oauth2Client(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Для упрощения в разработке
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .build();
    }

}

//th:text="${#strings.substring(#authentication.name, 0, 1).toUpperCase()}