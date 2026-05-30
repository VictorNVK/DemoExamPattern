package ru.demoexam.backend.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.demoexam.backend.config.AppProperties;

@Component
@RequiredArgsConstructor
public class StoragePaths {

    private final AppProperties appProperties;

    public Path storageRoot() {
        return Path.of(appProperties.storageRoot()).toAbsolutePath().normalize();
    }

    public Path databaseDirectory() {
        return storageRoot().resolve("database");
    }

    public Path imagesDirectory() {
        return storageRoot().resolve("images");
    }

    public void ensureCreated() {
        try {
            Files.createDirectories(databaseDirectory());
            Files.createDirectories(imagesDirectory());
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось создать каталоги storage.", exception);
        }
    }
}
