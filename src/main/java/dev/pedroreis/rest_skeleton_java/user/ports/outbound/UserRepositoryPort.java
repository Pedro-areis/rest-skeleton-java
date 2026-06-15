package dev.pedroreis.rest_skeleton_java.user.ports.outbound;

import dev.pedroreis.rest_skeleton_java.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    void deleteById(UUID id);
}
