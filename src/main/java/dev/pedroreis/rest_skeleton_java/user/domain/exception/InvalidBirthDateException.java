package dev.pedroreis.rest_skeleton_java.user.domain.exception;

public class InvalidBirthDateException extends DomainException {
    public InvalidBirthDateException() {
        super("Data de nascimento inválida");
    }
}
