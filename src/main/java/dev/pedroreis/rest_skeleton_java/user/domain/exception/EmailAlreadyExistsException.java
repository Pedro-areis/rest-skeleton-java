package dev.pedroreis.rest_skeleton_java.user.domain.exception;

public class EmailAlreadyExistsException extends DomainException {
    public EmailAlreadyExistsException() {
        super("E-mail já cadastrado");
    }
}
