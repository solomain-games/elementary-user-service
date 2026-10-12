package com.solomain.elementary.user.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Превращает исключения в ответы {@link ApiError}. Действует для всех контроллеров сервиса.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final MediaType PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON;

    /** Не прошла проверка {@code @Valid}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        List<ApiError.FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return badRequest("Request validation failed", "VALIDATION_FAILED", errors);
    }

    /** Тело запроса не JSON или не той структуры. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException exception) {
        return badRequest("Request body is missing or malformed", "BAD_REQUEST", List.of());
    }

    private static ResponseEntity<ApiError> badRequest(String detail, String code, List<ApiError.FieldError> errors) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        var body = new ApiError("about:blank", status.getReasonPhrase(), status.value(), detail, code, errors);
        return ResponseEntity.status(status).contentType(PROBLEM_JSON).body(body);
    }
}
