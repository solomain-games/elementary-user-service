package com.solomain.elementary.user.auth;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

/**
 * Выпуск JWT (ADR 0003): токен подписывает только user-service закрытым ключом по RS256,
 * остальные сервисы проверяют подпись открытым ключом.
 */
@Service
public class TokenService {

    static final String GUEST_ROLE = "GUEST";
    private static final String GUEST_ID_PREFIX = "g-";

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public TokenService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Создаёт нового гостя и выпускает ему токен.
     *
     * <p>Гость нигде не сохраняется: всё, что о нём известно, лежит в самом токене (ADR 0002).
     * Поэтому повторный вход с тем же именем даёт <b>нового</b> гостя с новым идентификатором.
     *
     * @param name уже проверенное имя
     */
    public AuthResponse issueGuestToken(String name) {
        String guestId = GUEST_ID_PREFIX + randomHex(8);
        // JWT хранит время с точностью до секунды — обрезаем заранее, чтобы expiresAt в ответе совпадал с exp
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(properties.guestTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(guestId)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("name", name)
                .claim("role", GUEST_ROLE)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AuthResponse(token, expiresAt, new AuthResponse.UserInfo(guestId, name, GUEST_ROLE));
    }

    /** Случайная строка из {@code bytes} байт в шестнадцатеричном виде (8 байт = 16 символов, 2^64 вариантов). */
    private String randomHex(int bytes) {
        byte[] buffer = new byte[bytes];
        random.nextBytes(buffer);
        return HexFormat.of().formatHex(buffer);
    }
}
