package com.example.carsharingapp.dto.user;

import jakarta.validation.constraints.*;

public record UserLoginRequestDto(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
