package com.solomain.elementary.user.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("GET /.well-known/jwks.json")
class JwksControllerTest {

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(
                    new JwksController(TestJwtKeys.signingKey()),
                    new GuestAuthController(new TokenService(
                            TestJwtKeys.encoder(),
                            new JwtProperties("elementary-test", Duration.ofHours(24)),
                            Clock.systemUTC())))
            .build();

    @Test
    @DisplayName("отдаёт один открытый RSA-ключ с kid, назначением и алгоритмом")
    void publishesPublicKey() throws Exception {
        mvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=300, public"))
                .andExpect(jsonPath("$.keys.length()").value(1))
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].kid").isString())
                .andExpect(jsonPath("$.keys[0].n").isString())
                .andExpect(jsonPath("$.keys[0].e").isString());
    }

    @Test
    @DisplayName("не раскрывает закрытый ключ: в ответе нет ни одной закрытой части (d, p, q, dp, dq, qi)")
    void doesNotLeakPrivateKey() throws Exception {
        String body = mvc.perform(get("/.well-known/jwks.json"))
                .andReturn().getResponse().getContentAsString();

        RSAKey published = (RSAKey) JWKSet.parse(body).getKeys().getFirst();

        assertThat(published.isPrivate()).isFalse();
        for (String privatePart : new String[]{"d", "p", "q", "dp", "dq", "qi"}) {
            assertThat(published.toJSONObject()).doesNotContainKey(privatePart);
        }
    }

    @Test
    @DisplayName("токен из /api/auth/guest проверяется ключом из JWKS, kid совпадает — путь другого сервиса")
    void tokenVerifiesWithPublishedKey() throws Exception {
        String token = JsonPath.read(
                mvc.perform(post("/api/auth/guest")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"Анна\"}"))
                        .andReturn().getResponse().getContentAsString(),
                "$.token");
        String jwksBody = mvc.perform(get("/.well-known/jwks.json"))
                .andReturn().getResponse().getContentAsString();

        RSAKey published = (RSAKey) JWKSet.parse(jwksBody).getKeys().getFirst();
        var decoder = NimbusJwtDecoder.withPublicKey(published.toRSAPublicKey()).build();

        assertThat(decoder.decode(token).getClaimAsString("name")).isEqualTo("Анна");
        assertThat(SignedJWT.parse(token).getHeader().getKeyID()).isEqualTo(published.getKeyID());
    }
}
