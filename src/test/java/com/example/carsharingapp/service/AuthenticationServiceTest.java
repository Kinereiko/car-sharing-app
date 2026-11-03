package com.example.carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.example.carsharingapp.dto.user.UserLoginRequestDto;
import com.example.carsharingapp.dto.user.UserLoginResponseDto;
import com.example.carsharingapp.security.AuthenticationService;
import com.example.carsharingapp.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD = "password123";
    private static final String TOKEN = "test.jwt.token";

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("""
            Verify that login returns a token when credentials are valid
            """)
    public void login_ValidCredentials_ReturnsToken() {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        when(jwtUtil.generateToken(EMAIL)).thenReturn(TOKEN);

        UserLoginResponseDto actual = authenticationService.login(requestDto);

        assertEquals(new UserLoginResponseDto(TOKEN), actual);
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtUtil).generateToken(EMAIL);
        verifyNoMoreInteractions(authenticationManager, jwtUtil);
    }

    @Test
    @DisplayName("""
            Verify that login passes email and password to the authentication manager
            """)
    public void login_ValidCredentials_PassesCredentialsToAuthenticationManager() {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        when(jwtUtil.generateToken(EMAIL)).thenReturn(TOKEN);

        authenticationService.login(requestDto);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        Authentication passed = captor.getValue();
        assertEquals(UsernamePasswordAuthenticationToken.class, passed.getClass());
        assertEquals(EMAIL, passed.getPrincipal());
        assertEquals(PASSWORD, passed.getCredentials());
    }

    @Test
    @DisplayName("""
            Verify that the token is generated for the authenticated user's name,
            not for the raw email from the request
            """)
    public void login_AuthenticatedNameDiffers_GeneratesTokenForAuthenticatedName() {
        String authenticatedName = "normalized@example.com";
        UserLoginRequestDto requestDto = new UserLoginRequestDto(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(authenticatedName);
        when(jwtUtil.generateToken(authenticatedName)).thenReturn(TOKEN);

        UserLoginResponseDto actual = authenticationService.login(requestDto);

        assertEquals(TOKEN, actual.token());
        verify(jwtUtil).generateToken(authenticatedName);
    }

    @Test
    @DisplayName("""
            Verify that login throws an exception and does not generate a token
            when credentials are invalid
            """)
    public void login_InvalidCredentials_ThrowsException() {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(EMAIL, "wrong-password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authenticationService.login(requestDto));

        assertEquals("Bad credentials", exception.getMessage());
        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(jwtUtil);
    }
}
