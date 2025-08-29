package ru.yandex.practicum.mvc_internet_shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value(value = "${images.baseUrl}") String imageBaseUrl;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**") // URL, по которому будут доступны ресурсы
                .addResourceLocations("classpath:/images/"); // Путь к директории с ресурсами
    }
}
