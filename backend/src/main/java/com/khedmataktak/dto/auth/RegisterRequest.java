package com.khedmataktak.dto.auth;

import com.khedmataktak.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        UserType userType
) {
    public UserType resolvedUserType() {
        return userType != null ? userType : UserType.CANDIDATE;
    }
}
