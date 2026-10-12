package com.solomain.elementary.user.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TokenService — выпуск гостевых токенов")
class TokenServiceTest {

    // Сейчас + немного: токен из теста должен быть действителен для декодера, который смотрит на реальные часы
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Duration TTL = Duration.ofHours(24);

    private final TokenService service = new TokenService(
            TestJwtKeys.encoder(),
            new JwtProperties("elementary-test", TTL),
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("токен подписан закрытым ключом и проверяется открытым")
    void tokenIsVerifiableWithPublicKey() {
        var response = service.issueGuestToken("Анна");

        var jwt = TestJwtKeys.decoder().decode(response.token());

        assertThat(jwt.getSubject()).isEqualTo(response.user().id());
    }

    @Test
    @DisplayName("в токене: id гостя, имя, роль, издатель и срок")
    void tokenContainsClaims() {
        var response = service.issueGuestToken("Анна");

        var jwt = TestJwtKeys.decoder().decode(response.token());

        assertThat(jwt.getSubject()).startsWith("g-").hasSize(2 + 16);
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Анна");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("GUEST");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("elementary-test");
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(TTL));
    }

    @Test
    @DisplayName("ответ повторяет данные токена")
    void responseMatchesToken() {
        var response = service.issueGuestToken("Анна");

        assertThat(response.expiresAt()).isEqualTo(NOW.plus(TTL));
        assertThat(response.user().name()).isEqualTo("Анна");
        assertThat(response.user().role()).isEqualTo("GUEST");
    }

    @Test
    @DisplayName("каждый вход — новый гость, даже с тем же именем")
    void issuesNewGuestEachTime() {
        var first = service.issueGuestToken("Анна");
        var second = service.issueGuestToken("Анна");

        assertThat(first.user().id()).isNotEqualTo(second.user().id());
    }

    @Test
    @DisplayName("токен не проходит проверку чужим ключом — подделать его нельзя")
    void rejectsTokenWithForeignKey() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var foreignKey = (RSAPublicKey) generator.generateKeyPair().getPublic();
        var foreignDecoder = NimbusJwtDecoder.withPublicKey(foreignKey).build();

        var token = service.issueGuestToken("Анна").token();

        assertThatThrownBy(() -> foreignDecoder.decode(token)).isInstanceOf(BadJwtException.class);
    }
}
