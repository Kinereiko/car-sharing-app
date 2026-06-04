package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.user.UserLoginRequestDto;
import com.example.carsharingapp.dto.user.UserLoginResponseDto;
import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.security.AuthenticationService;
import com.example.carsharingapp.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody @Valid
                                          UserLoginRequestDto requestDto) {
        return authenticationService.login(requestDto);
    }

    @PostMapping("/registration")
    public UserResponseDto register(@RequestBody @Valid
                                        UserRegistrationRequestDto requestDto) {
        return userService.register(requestDto);
    }
}
