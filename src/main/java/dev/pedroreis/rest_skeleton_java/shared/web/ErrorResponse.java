package dev.pedroreis.rest_skeleton_java.shared.web;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Corpo padrão das respostas de erro, compartilhado por todas as funcionalidades (ver ADR 005).
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

    /** Atalho usado pelos advices: resposta com o status e a mensagem dados. */
    public static ResponseEntity<ErrorResponse> entity(HttpStatusCode status, String message) {
        return ResponseEntity.status(status).body(of(status.value(), message));
    }
}
