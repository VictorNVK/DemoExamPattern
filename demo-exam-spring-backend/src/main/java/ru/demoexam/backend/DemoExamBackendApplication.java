package ru.demoexam.backend;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoExamBackendApplication {

    public static void main(String[] args) {
        ensureStorageDirectories();
        SpringApplication.run(DemoExamBackendApplication.class, args);
    }

    private static void ensureStorageDirectories() {
        try {
            Path storageRoot = Path.of("storage");
            Files.createDirectories(storageRoot.resolve("database"));
            Files.createDirectories(storageRoot.resolve("images"));
            Files.createDirectories(Path.of("input"));
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось создать каталоги storage.", exception);
        }
    }
}
