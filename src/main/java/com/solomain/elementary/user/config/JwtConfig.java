package com.solomain.elementary.user.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.solomain.elementary.user.auth.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;

/**
 * Ключи и кодировщик JWT.
 *
 * <p>Ключи читаются из PEM-файлов, путь к которым задаётся настройками
 * {@code elementary.jwt.private-key} и {@code elementary.jwt.public-key} (например, {@code file:./secrets/jwt-private.pem}).
 * Закрытый ключ — в формате PKCS#8 ({@code BEGIN PRIVATE KEY}), открытый — X.509 ({@code BEGIN PUBLIC KEY}).
 * Ключи никогда не хранятся в репозитории.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    /**
     * Пара ключей в формате JWK. Идентификатор ключа ({@code kid}) — отпечаток открытого ключа (RFC 7638):
     * он одинаковый при каждом запуске и меняется, только если сменить ключ. По {@code kid} сервисы
     * понимают, каким ключом проверять подпись, — это пригодится для смены ключей без простоя.
     */
    @Bean
    RSAKey jwtSigningKey(@Value("${elementary.jwt.public-key}") Resource publicKeyFile,
                         @Value("${elementary.jwt.private-key}") Resource privateKeyFile) throws Exception {
        RSAPublicKey publicKey;
        try (InputStream in = publicKeyFile.getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(in);
        }
        RSAPrivateKey privateKey;
        try (InputStream in = privateKeyFile.getInputStream()) {
            privateKey = RsaKeyConverters.pkcs8().convert(in);
        }
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyIDFromThumbprint()
                .build();
    }

    @Bean
    JwtEncoder jwtEncoder(RSAKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwtSigningKey)));
    }

    /** Часы приложения; в тестах подменяются на фиксированные. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
