package dev.pedroreis.rest_skeleton_java.user.domain.exception;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
}
