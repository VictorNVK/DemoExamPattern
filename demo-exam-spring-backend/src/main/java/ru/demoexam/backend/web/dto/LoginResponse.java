package ru.demoexam.backend.web.dto;

public record LoginResponse(
        String token,
        int userId,
        String fullName,
        String role
) {
}
