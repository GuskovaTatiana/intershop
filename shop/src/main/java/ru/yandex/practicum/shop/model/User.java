package ru.yandex.practicum.shop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Indexed;
import ru.yandex.practicum.shop.model.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "t_users")
public class User {

    @Id
    private Integer id;
    private String login;
    private String password;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;

}
