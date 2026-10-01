package dev.pedroreis.rest_skeleton_java.user.adapters.inbound.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Corpo padrão das respostas de erro.
 * {@code fieldErrors} só vem preenchido em erros de validação.
 */
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, LocalDateTime.now(), Map.of());
    }

    public static ErrorResponse of(int status, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(status, message, LocalDateTime.now(), fieldErrors);
    }
}
