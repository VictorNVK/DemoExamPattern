package ru.demoexam.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.demoexam.backend.security.SecurityRoles;
import ru.demoexam.backend.security.TokenAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER_PUBLIC_PATHS = {
            "/api/v3/api-docs",
            "/api/v3/api-docs/**",
            "/api/v3/swagger-ui",
            "/api/v3/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-resources/**",
            "/webjars/**",
    };

    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PUBLIC_PATHS).permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/files/**"
                        ).permitAll()
                        // Модуль 2: список товаров — гость, клиент, менеджер, администратор
                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                        // Модуль 3: CRUD товаров — только администратор
                        .requestMatchers(HttpMethod.GET, "/api/products/next-id").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/products/options").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/products/filter-options")
                        .hasAnyRole(SecurityRoles.MANAGER, SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/products/{productId}").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/products", "/api/products/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole(SecurityRoles.ADMIN)
                        // Модуль 4: просмотр заказов — менеджер и администратор
                        .requestMatchers(HttpMethod.GET, "/api/orders", "/api/orders/**")
                        .hasAnyRole(SecurityRoles.MANAGER, SecurityRoles.ADMIN)
                        // Модуль 4: CRUD заказов — только администратор
                        .requestMatchers(HttpMethod.POST, "/api/orders", "/api/orders/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole(SecurityRoles.ADMIN)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
