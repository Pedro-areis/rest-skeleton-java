package dev.pedroreis.rest_skeleton_java.user.adapters.inbound.dto;

import dev.pedroreis.rest_skeleton_java.user.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        LocalDateTime createdAt
) {
    public static UserResponse fromDomain (User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
