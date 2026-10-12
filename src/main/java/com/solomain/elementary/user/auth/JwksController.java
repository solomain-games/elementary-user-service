package com.solomain.elementary.user.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * Публикация открытого ключа в формате JWKS (RFC 7517) — задача П3, ADR 0003.
 *
 * <p>Другие сервисы (gateway, game-service) скачивают отсюда открытый ключ и сами проверяют подпись токенов,
 * не обращаясь к user-service на каждый запрос. Адрес {@code /.well-known/jwks.json} — общепринятое соглашение:
 * библиотеки проверки JWT ищут ключи именно там.
 */
@RestController
public class JwksController {

    /** Сервисы могут кэшировать ключи; при смене ключа старый ещё какое-то время остаётся в ответе. */
    private static final CacheControl CACHE = CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic();

    private final Map<String, Object> jwks;

    public JwksController(RSAKey jwtSigningKey) {
        // toPublicJWK() отбрасывает закрытую часть ключа: наружу уходят только n и e
        this.jwks = new JWKSet(jwtSigningKey.toPublicJWK()).toJSONObject();
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok().cacheControl(CACHE).body(jwks);
    }
}
