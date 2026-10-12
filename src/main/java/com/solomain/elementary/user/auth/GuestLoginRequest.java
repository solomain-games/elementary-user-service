package com.solomain.elementary.user.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Запрос гостевого входа (протокол, 3.1).
 *
 * @param name отображаемое имя: 1–24 символа, буквы, цифры, пробел, дефис, подчёркивание.
 *             Пробелы по краям обрезаются до проверки.
 */
public record GuestLoginRequest(
        @NotBlank
        @Size(max = 24)
        @Pattern(regexp = "^[\\p{L}\\p{N} _-]+$", message = "may contain only letters, digits, space, '-' and '_'")
        String name) {

    public GuestLoginRequest {
        // Компактный конструктор выполняется до проверки аннотаций: валидируется уже обрезанное имя
        if (name != null) {
            name = name.strip();
        }
    }
}
