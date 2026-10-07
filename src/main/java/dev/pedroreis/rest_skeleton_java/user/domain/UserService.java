package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.UserNotFoundException;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.CreateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.DeleteUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.FindUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.UpdateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.PasswordHasherPort;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class UserService implements CreateUserUseCase, FindUserUseCase, UpdateUserUseCase, DeleteUserUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    public User execute(String name, String email, String password, LocalDate birthDate) {
        Optional<User> existingUser = userRepositoryPort.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        // O hash é caro (de propósito), então só é gerado depois das verificações baratas.
        String passwordHash = passwordHasherPort.hash(password);
        User user = new User(name, email, passwordHash, birthDate);
        return userRepositoryPort.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public User update(UUID id, String name, String email, String password, LocalDate birthDate) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(UserNotFoundException::new);

        // Primeiro aplica as mudanças (o domínio valida e-mail e data), depois confere a unicidade.
        user.updateProfile(name, email, birthDate);
        ensureEmailIsNotUsedByAnotherUser(user);
        user.updatePasswordHash(hashIfFilled(password));

        return userRepositoryPort.save(user);
    }

    @Override
    public void deleteById(UUID id) {
        // A consulta existe só para devolver "não encontrado" quando o id não existe.
        userRepositoryPort.findById(id)
                .orElseThrow(UserNotFoundException::new);
        userRepositoryPort.deleteById(id);
    }

    /**
     * Só gera hash quando há senha nova. Nulo ou em branco significa "não alterar":
     * devolvemos null e o User ignora (a regra de "não alterar" fica no domínio).
     */
    private String hashIfFilled(String password) {
        if (password == null || password.trim().isEmpty()) {
            return null;
        }
        return passwordHasherPort.hash(password);
    }

    /**
     * O e-mail pode continuar sendo o do próprio usuário (ele não mudou),
     * mas não pode ser o de outra pessoa. A constraint UNIQUE do banco continua
     * como segunda barreira para o caso de duas requisições simultâneas.
     */
    private void ensureEmailIsNotUsedByAnotherUser(User user) {
        userRepositoryPort.findByEmail(user.getEmail())
                .filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(other -> {
                    throw new EmailAlreadyExistsException();
                });
    }
}
