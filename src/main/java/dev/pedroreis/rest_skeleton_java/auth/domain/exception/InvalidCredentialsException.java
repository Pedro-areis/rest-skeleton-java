package dev.pedroreis.rest_skeleton_java.auth.domain.exception;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.DomainException;

/**
 * Login recusado. A mensagem é a mesma para e-mail inexistente e senha errada,
 * para não revelar quais e-mails estão cadastrados (ADR 002).
 */
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos");
    }
}
