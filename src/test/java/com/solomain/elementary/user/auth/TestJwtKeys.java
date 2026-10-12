package com.solomain.elementary.user.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Кодировщик и декодер JWT на тестовых ключах из {@code src/test/resources/test-keys}.
 */
final class TestJwtKeys {

    static final RSAPublicKey PUBLIC_KEY = read("test-keys/jwt-public.pem", true);
    static final RSAPrivateKey PRIVATE_KEY = read("test-keys/jwt-private.pem", false);

    private TestJwtKeys() {
    }

    static JwtEncoder encoder() {
        RSAKey key = new RSAKey.Builder(PUBLIC_KEY).privateKey(PRIVATE_KEY).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    /** Декодер, который проверяет подпись открытым ключом и срок действия токена. */
    static JwtDecoder decoder() {
        return NimbusJwtDecoder.withPublicKey(PUBLIC_KEY).build();
    }

    @SuppressWarnings("unchecked")
    private static <T> T read(String path, boolean isPublic) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return (T) (isPublic ? RsaKeyConverters.x509().convert(in) : RsaKeyConverters.pkcs8().convert(in));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
