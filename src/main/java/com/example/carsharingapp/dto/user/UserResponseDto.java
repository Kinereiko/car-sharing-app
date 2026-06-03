package com.example.carsharingapp.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
