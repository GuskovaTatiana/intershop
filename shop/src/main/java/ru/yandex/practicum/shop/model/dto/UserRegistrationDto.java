package ru.yandex.practicum.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
@AllArgsConstructor
public class UserRegistrationDto {

    private String login;
    private String password;
    private String firstName;
    private String lastName;
}
