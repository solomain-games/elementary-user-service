package com.solomain.elementary.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Настройки безопасности.
 * Открыты проверка состояния и вход; всё остальное требует аутентификации.
 * Гостевой вход (П2) и публикация открытого ключа (П3) будут открыты здесь же.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        // Вход открыт всем: токена у пользователя ещё нет
                        .requestMatchers(HttpMethod.POST, "/api/auth/guest").permitAll()
                        // Открытый ключ публичен по определению: им только проверяют подпись
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        // Служебная страница ошибок Spring: иначе любая ошибка превращалась бы в 401
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // Сервис не хранит сессии: каждый запрос будет нести свой токен
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF защищает сессии на cookie; с токенами в заголовке он не нужен
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
