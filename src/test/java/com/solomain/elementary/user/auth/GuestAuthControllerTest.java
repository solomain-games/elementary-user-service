package com.solomain.elementary.user.auth;

import com.solomain.elementary.user.web.ApiExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Контроллер входа, проверяется через MockMvc: HTTP-запрос имитируется без запуска сервера.
 * {@code standaloneSetup} поднимает только этот контроллер и обработчик ошибок, без всего приложения.
 */
@DisplayName("POST /api/auth/guest")
class GuestAuthControllerTest {

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new GuestAuthController(new TokenService(
                    TestJwtKeys.encoder(),
                    new JwtProperties("elementary-test", Duration.ofHours(24)),
                    Clock.systemUTC())))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    @DisplayName("200: токен, срок и сведения о госте")
    void issuesToken() throws Exception {
        mvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Анна\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.user.name").value("Анна"))
                .andExpect(jsonPath("$.user.role").value("GUEST"));
    }

    @Test
    @DisplayName("пробелы по краям имени обрезаются")
    void stripsName() throws Exception {
        mvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"  Анна  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("Анна"));
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"", "   ", "Очень-очень-длинное-имя-25", "<script>", "Анна!"})
    @DisplayName("400 VALIDATION_FAILED: пустое, слишком длинное или с недопустимыми символами")
    void rejectsInvalidName(String name) throws Exception {
        mvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @DisplayName("принимает имя ровно из 24 символов — граничное значение")
    void acceptsMaxLengthName() throws Exception {
        mvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + "Я".repeat(24) + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("400 BAD_REQUEST: тело не JSON")
    void rejectsMalformedBody() throws Exception {
        mvc.perform(post("/api/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("не json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
