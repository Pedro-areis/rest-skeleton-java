package dev.pedroreis.rest_skeleton_java.user.domain.exception;

/**
 * Base de todas as exceções de regra de negócio.
 * Java puro: não depende de Spring nem de HTTP.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
