package com.personal_finance.service;

import com.personal_finance.dto.user.LoginUserDto;
import com.personal_finance.security.JwtService;
import com.personal_finance.security.CustomUserDetails;
import com.personal_finance.security.dtos.AuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetails customUserDetails;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldReturnJwtToken_WhenCredentialsAreValid(){
        LoginUserDto dto = new LoginUserDto("rafael", "123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(customUserDetails);

        when(jwtService.generateAccessToken(customUserDetails)).thenReturn("fake-token");

        AuthenticationResponse result = authenticationService.login(dto);

        assertThat(result.accessToken()).isEqualTo("fake-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(customUserDetails);
    }

    @Test
    void shouldThrowException_WhenCredentialsAreInvalid(){
        LoginUserDto dto = new LoginUserDto("rafael", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authenticationService.login(dto))
                .isInstanceOf(BadCredentialsException.class);
    }
}
