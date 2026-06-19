package com.personal_finance.service;

import com.personal_finance.dto.user.LoginUserDto;
import com.personal_finance.dto.user.UserRequestDto;
import com.personal_finance.security.dtos.NewAccessToken;
import com.personal_finance.security.JwtService;
import com.personal_finance.security.CustomUserDetails;
import com.personal_finance.security.dtos.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersService usersService;

    public AuthenticationResponse login(LoginUserDto dto) {

        log.info("Login attempt for username '{}'", dto.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User {} logged in successfully", user.getId());

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Transactional
    public void register(UserRequestDto userRequestDto) {
        usersService.register(userRequestDto);
    }

    public NewAccessToken refresh(String refreshToken) {
        return jwtService.refresh(refreshToken);
    }
}
