package com.solomain.elementary.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Настройки безопасности.
 * Пока открыты только эндпоинты проверки состояния, всё остальное требует аутентификации.
 * Гостевой вход (П2) и публикация открытого ключа (П3) будут открыты здесь же.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                // Сервис не хранит сессии: каждый запрос будет нести свой токен
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF защищает сессии на cookie; с токенами в заголовке он не нужен
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
