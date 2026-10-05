package dev.pedroreis.rest_skeleton_java.user.ports.inbound;

import dev.pedroreis.rest_skeleton_java.user.domain.User;

import java.time.LocalDate;

public interface CreateUserUseCase {
    User execute(String name, String email, String password, LocalDate birthDate);
}
