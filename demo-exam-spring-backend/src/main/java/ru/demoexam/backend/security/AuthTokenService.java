package ru.demoexam.backend.security;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private final Map<String, AuthenticatedUser> sessions = new ConcurrentHashMap<>();

    public String createToken(AuthenticatedUser user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public Optional<AuthenticatedUser> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
