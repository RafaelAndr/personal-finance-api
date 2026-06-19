package com.personal_finance.security.dtos;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}
