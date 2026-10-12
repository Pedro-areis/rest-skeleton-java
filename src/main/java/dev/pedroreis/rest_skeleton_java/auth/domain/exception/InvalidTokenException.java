package dev.pedroreis.rest_skeleton_java.auth.domain.exception;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.DomainException;

/** Token malformado, com assinatura inválida, expirado ou de tipo errado (access no lugar de refresh, e vice-versa). */
public class InvalidTokenException extends DomainException {
    public InvalidTokenException() {
        super("Token inválido ou expirado");
    }
}
