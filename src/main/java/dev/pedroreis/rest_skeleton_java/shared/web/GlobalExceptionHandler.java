package dev.pedroreis.rest_skeleton_java.shared.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Trata o que não é de nenhuma funcionalidade: validação, corpo ilegível e o catch-all.
 * Não conhece exceções de {@code user} nem de {@code auth} (ver ADR 005).
 * <p>
 * Tem a menor precedência: os advices das funcionalidades (precedência maior) são consultados
 * primeiro, e o catch-all nunca vence um handler específico.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.merge(error.getField(), error.getDefaultMessage(), (a, b) -> a + "; " + b));

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Dados inválidos", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ErrorResponse.entity(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou malformado");
    }

    /**
     * Rede de segurança. Nunca devolve detalhes internos ao cliente.
     * <p>
     * Atenção na Task 7 (Spring Security): se este handler engolir
     * AccessDeniedException / AuthenticationException, as respostas 401/403 viram 500.
     * Adicionar handlers específicos (ou relançar) antes de ligar a segurança.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // Exceções do próprio Spring MVC (405, 415, 404 de rota etc.) já trazem o status certo.
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            return ErrorResponse.entity(springError.getStatusCode(), "Não foi possível processar a requisição");
        }

        log.error("Erro inesperado", ex);
        return ErrorResponse.entity(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde");
    }
}
