package com.solomain.elementary.user.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Вход в игру (протокол, раздел 3).
 */
@RestController
@RequestMapping("/api/auth")
public class GuestAuthController {

    private final TokenService tokenService;

    public GuestAuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Гостевой вход по имени: без регистрации, без сохранения в базе.
     * {@code @Valid} запускает проверки аннотаций {@link GuestLoginRequest}; при ошибке ответ — 400.
     */
    @PostMapping("/guest")
    public AuthResponse guest(@Valid @RequestBody GuestLoginRequest request) {
        return tokenService.issueGuestToken(request.name());
    }
}
