package ru.yandex.practicum.mvc_internet_shop.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class TestUtils {

    @Autowired
    private R2dbcEntityTemplate r2dbcTemplate;

    public TestUtils(R2dbcEntityTemplate r2dbcTemplate) {
        this.r2dbcTemplate = r2dbcTemplate;
    }

    public void executeSQL(String resource)  {
        try {
            String sql = new String(Files.readAllBytes(Paths.get(getClass().getResource(resource).toURI())));
            r2dbcTemplate.getDatabaseClient().sql(sql).fetch().rowsUpdated().block();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
