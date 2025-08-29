package ru.yandex.practicum.mvc_internet_shop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * сервис по работе с файлами
 * */
@Service
public class FileService {
    private final String uploadDirectory = "/images";
//    @Value(value = "${images.baseUrl}")
    String imageBaseUrl = "classpath:";

    /**
     * Сохраняет файл и возвращает URL для доступа к нему.
     *
     * @throws IOException  Если произошла ошибка при сохранении файла
     */
    public String storeFile(MultipartFile file) throws IOException {
        String filename = generateUniqueFilename(file.getOriginalFilename());
        Path uploadPath = Paths.get(imageBaseUrl + uploadDirectory);

        // Создаем папку, если она не существует
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new IOException("Not create Directories " + imageBaseUrl + uploadDirectory, e);
            }
        }
        Path filePath = Paths.get(imageBaseUrl + uploadDirectory, filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to store file " + filename, e);
        }

        return uploadDirectory + "/" + filename;
    }


    /**
     * Генерирует уникальное имя файла.
     *
     * @return Уникальное имя файла
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }
}
