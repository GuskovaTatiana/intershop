package ru.yandex.practicum.mvc_internet_shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Configuration
public class WebConfig implements WebFluxConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**") // URL, по которому будут доступны ресурсы
                .addResourceLocations("classpath:/images/"); // Путь к директории с ресурсами
    }
}
