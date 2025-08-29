package ru.yandex.practicum.mvc_internet_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MvcInternetShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(MvcInternetShopApplication.class, args);
	}

}
