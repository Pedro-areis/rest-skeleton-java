package dev.pedroreis.rest_skeleton_java.user.domain.exception;

public class InvalidEmailException extends DomainException {
    public InvalidEmailException() {
        super("E-mail inválido");
    }
}
