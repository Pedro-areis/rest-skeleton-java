package dev.pedroreis.rest_skeleton_java.user.ports.inbound;

import dev.pedroreis.rest_skeleton_java.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface FindUserUseCase {
    Optional<User> findById(UUID id);
}
