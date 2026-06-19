package dev.pedroreis.rest_skeleton_java.user.ports.inbound;

import java.util.UUID;

public interface DeleteUserUseCase {
    void deleteById(UUID id);
}
