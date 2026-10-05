package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.UserNotFoundException;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.CreateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.DeleteUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.FindUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.UpdateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class UserService implements CreateUserUseCase, FindUserUseCase, UpdateUserUseCase, DeleteUserUseCase {
    private final UserRepositoryPort userRepositoryPort;

    public UserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(String name, String email, String password, LocalDate birthDate) {
        Optional<User> existingUser = userRepositoryPort.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User(name, email, password, birthDate);
        return userRepositoryPort.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public User update(UUID id, String name, String email, String password, LocalDate birthDate) {
        // OBS: busca por e-mail em vez de id. Bug conhecido, corrigido na Task 3.
        User existingUser = userRepositoryPort.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        existingUser.updateProfile(name, email, birthDate);
        existingUser.updatePassword(password);

        return userRepositoryPort.save(existingUser);
    }

    @Override
    public void deleteById(UUID id) {
        User user = userRepositoryPort.findById(id)
                        .orElseThrow(UserNotFoundException::new);
        userRepositoryPort.deleteById(id);
    }
}
