package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.ports.inbound.CreateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.DeleteUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.FindUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.UpdateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;

import java.util.Optional;
import java.util.UUID;

public class UserService implements CreateUserUseCase, FindUserUseCase, UpdateUserUseCase, DeleteUserUseCase {
    private final UserRepositoryPort userRepositoryPort;

    public UserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(String name, String email, String password) {
        Optional<User> existingUser = userRepositoryPort.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("E-mail already exists");
        }

        User user = new User(name, email, password);
        return userRepositoryPort.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public User update(User user) {
        return userRepositoryPort.save(user);
    }

    @Override
    public void deleteById(UUID id) {
        Optional<User> user = userRepositoryPort.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        userRepositoryPort.deleteById(id);
    }
}
