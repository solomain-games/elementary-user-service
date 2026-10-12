package com.solomain.elementary.user.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

/**
 * Настройки выпуска токенов ({@code elementary.jwt.*} в application.yaml).
 *
 * @param issuer        кто выпустил токен (claim {@code iss}); сервисы будут проверять его
 * @param guestTokenTtl сколько действует гостевой токен
 */
@ConfigurationProperties(prefix = "elementary.jwt")
public record JwtProperties(String issuer, Duration guestTokenTtl) {

    public JwtProperties {
        Objects.requireNonNull(issuer, "elementary.jwt.issuer");
        Objects.requireNonNull(guestTokenTtl, "elementary.jwt.guest-token-ttl");
    }
}
