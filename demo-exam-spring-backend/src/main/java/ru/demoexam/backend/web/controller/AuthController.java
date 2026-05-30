package ru.demoexam.backend.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.demoexam.backend.service.AuthService;
import ru.demoexam.backend.web.dto.LoginRequest;
import ru.demoexam.backend.web.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Авторизация", description = "Вход в систему и получение Bearer-токена")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Вход",
            description = "Доступно всем. Возвращает Bearer-токен для ролей client, manager, admin."
    )
    @SecurityRequirements
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
