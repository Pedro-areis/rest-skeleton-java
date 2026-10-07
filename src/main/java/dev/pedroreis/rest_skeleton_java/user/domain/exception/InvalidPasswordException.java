package dev.pedroreis.rest_skeleton_java.user.domain.exception;

public class InvalidPasswordException extends DomainException {
    public InvalidPasswordException() {
        super("Senha inválida: o máximo é 72 bytes");
    }
}
