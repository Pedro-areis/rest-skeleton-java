package dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.dto;

import dev.pedroreis.rest_skeleton_java.auth.domain.AuthTokens;

public record LoginResponse(String accessToken, String refreshToken) {
    public static LoginResponse fromDomain(AuthTokens tokens) {
        return new LoginResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
