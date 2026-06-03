package com.example.carsharingapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    @Size(min = 8, max = 254)
    private String email;
    @NotBlank
    @Size(min = 8, max = 64)
    private String password;
    @NotBlank
    @Size(min = 8, max = 64)
    private String repeatPassword;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
