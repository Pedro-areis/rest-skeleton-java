package dev.pedroreis.rest_skeleton_java.user.domain.exception;

public class InvalidPasswordException extends DomainException {
    public InvalidPasswordException() {
        super("Senha inválida: deve ter no mínimo 8 caracteres e no máximo 72 bytes");
    }
}
