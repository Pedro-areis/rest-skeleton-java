package dev.pedroreis.rest_skeleton_java.user.adapters.inbound.dto;

import java.time.LocalDate;

public record UpdateUserRequest(
        String name,
        String email,
        String password,
        LocalDate birthDate
) {
}
