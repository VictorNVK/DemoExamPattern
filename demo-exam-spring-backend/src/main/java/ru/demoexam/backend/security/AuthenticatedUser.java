package ru.demoexam.backend.security;

public record AuthenticatedUser(int userId, String fullName, String role) {
}
