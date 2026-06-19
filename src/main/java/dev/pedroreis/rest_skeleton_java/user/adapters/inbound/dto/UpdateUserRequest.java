package dev.pedroreis.rest_skeleton_java.user.adapters.inbound.dto;

public record UpdateUserRequest(
        String name,
        String email,
        String password
) {
}
