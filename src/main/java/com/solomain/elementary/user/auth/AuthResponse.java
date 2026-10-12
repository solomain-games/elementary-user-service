package com.solomain.elementary.user.auth;

import java.time.Instant;

/**
 * Ответ на вход: токен и сведения о пользователе (протокол, 3.1).
 *
 * @param token     JWT для заголовка {@code Authorization: Bearer ...}
 * @param expiresAt когда токен перестанет действовать
 * @param user      кто вошёл
 */
public record AuthResponse(String token, Instant expiresAt, UserInfo user) {

    /**
     * Пользователь.
     *
     * @param id   идентификатор; для гостей начинается с {@code g-}
     * @param name отображаемое имя
     * @param role роль: {@code GUEST}, позже {@code PLAYER}, {@code ADMIN}
     */
    public record UserInfo(String id, String name, String role) {
    }
}
