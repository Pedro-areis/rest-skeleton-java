package dev.pedroreis.rest_skeleton_java.user.ports.outbound;

/**
 * Porta de saída para proteger senhas. O domínio só conhece este contrato;
 * o algoritmo (BCrypt hoje) fica no adaptador e pode ser trocado sem mexer no domínio.
 */
public interface PasswordHasherPort {

    /** Gera o hash de uma senha em texto puro. */
    String hash(String rawPassword);

    /** Confere se a senha em texto puro corresponde ao hash guardado. */
    boolean matches(String rawPassword, String passwordHash);
}
