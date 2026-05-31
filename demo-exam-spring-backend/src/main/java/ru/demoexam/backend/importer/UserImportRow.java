package ru.demoexam.backend.importer;

public record UserImportRow(
        String fullName,
        String login,
        String password,
        String role
) {
}
