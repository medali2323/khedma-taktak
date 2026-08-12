package com.khedmataktak.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        String userId,
        String email,
        String firstName,
        String lastName,
        String userType
) {
}
