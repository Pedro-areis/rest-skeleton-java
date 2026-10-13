package dev.pedroreis.rest_skeleton_java.auth.domain;

/** Par devolvido no login. A renovação devolve só um novo access token (ADR 002). */
public record AuthTokens(String accessToken, String refreshToken) {}
