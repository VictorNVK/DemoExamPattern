package ru.demoexam.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String title,
        String companyName,
        String storageRoot,
        String databaseFile
) {
    public AppProperties {
        if (title == null || title.isBlank()) {
            title = "Book Store Demo Template";
        }
        if (companyName == null || companyName.isBlank()) {
            companyName = "Book Store";
        }
        if (storageRoot == null || storageRoot.isBlank()) {
            storageRoot = "storage";
        }
        if (databaseFile == null || databaseFile.isBlank()) {
            databaseFile = "book_store_exam.db";
        }
    }
}
