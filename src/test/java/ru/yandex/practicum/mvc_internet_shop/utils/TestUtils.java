package ru.yandex.practicum.mvc_internet_shop.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class TestUtils {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public TestUtils(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void executeSQL(String resource)  {
        try {
            String sql = new String(Files.readAllBytes(Paths.get(getClass().getResource(resource).toURI())));
            jdbcTemplate.execute(sql);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
