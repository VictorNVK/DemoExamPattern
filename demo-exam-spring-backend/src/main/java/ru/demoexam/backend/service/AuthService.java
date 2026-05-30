package ru.demoexam.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.demoexam.backend.domain.User;
import ru.demoexam.backend.repository.UserRepository;
import ru.demoexam.backend.security.AuthTokenService;
import ru.demoexam.backend.security.AuthenticatedUser;
import ru.demoexam.backend.web.dto.LoginRequest;
import ru.demoexam.backend.web.dto.LoginResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;

    @Transactional(readOnly = true)
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        User user = userRepository
                .findByLoginAndPasswordAndActiveTrue(request.login().trim(), request.password())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден или пароль введен неверно."));

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getFullName(),
                user.getRole()
        );
        String token = authTokenService.createToken(authenticatedUser);
        return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getFullName(), user.getRole()));
    }
}
