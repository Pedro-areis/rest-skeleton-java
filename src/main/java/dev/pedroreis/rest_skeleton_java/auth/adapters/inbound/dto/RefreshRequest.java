package dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}
