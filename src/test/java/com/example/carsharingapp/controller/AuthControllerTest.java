package com.example.carsharingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.carsharingapp.config.SecurityConfig;
import com.example.carsharingapp.dto.user.UserLoginRequestDto;
import com.example.carsharingapp.dto.user.UserLoginResponseDto;
import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.exception.RegistrationException;
import com.example.carsharingapp.security.AuthenticationService;
import com.example.carsharingapp.security.JwtAuthenticationFilter;
import com.example.carsharingapp.security.JwtUtil;
import com.example.carsharingapp.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    private UserRegistrationRequestDto registrationRequest() {
        UserRegistrationRequestDto dto = new UserRegistrationRequestDto();
        dto.setEmail("john.doe@example.com");
        dto.setPassword("password123");
        dto.setRepeatPassword("password123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        return dto;
    }

    @Test
    @DisplayName("POST /auth/login with valid credentials returns a token (public endpoint)")
    void login_ValidRequest_ReturnsToken() throws Exception {
        when(authenticationService.login(any(UserLoginRequestDto.class)))
                .thenReturn(new UserLoginResponseDto("jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserLoginRequestDto("john.doe@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/login with blank email and password returns 400")
    void login_BlankFields_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserLoginRequestDto("", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2));

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("POST /auth/login with an empty body returns 400")
    void login_EmptyBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("POST /auth/registration with valid data returns the registered user")
    void register_ValidRequest_ReturnsUser() throws Exception {
        UserResponseDto response = new UserResponseDto();
        response.setEmail("john.doe@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        when(userService.register(any(UserRegistrationRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("POST /auth/registration with invalid email returns 400")
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto dto = registrationRequest();
        dto.setEmail("not-an-email");

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /auth/registration with too short passwords and blank names returns 400")
    void register_InvalidFields_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto dto = registrationRequest();
        dto.setPassword("short");
        dto.setRepeatPassword("short");
        dto.setFirstName("");
        dto.setLastName(" ");

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(4));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /auth/registration with an existing email returns 409")
    void register_ExistingEmail_ReturnsConflict() throws Exception {
        when(userService.register(any(UserRegistrationRequestDto.class)))
                .thenThrow(new RegistrationException("User already exists"));

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }
}
