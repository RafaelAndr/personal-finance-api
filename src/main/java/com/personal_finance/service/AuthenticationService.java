package com.personal_finance.service;

import com.personal_finance.dto.user.LoginUserDto;
import com.personal_finance.dto.user.UserRequestDto;
import com.personal_finance.security.JwtToken;
import com.personal_finance.security.JwtService;
import com.personal_finance.security.CustomUserDetails;
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

    public JwtToken login(LoginUserDto dto) {

        log.info("Login attempt for username '{}'", dto.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        log.info("User {} logged in successfully", user.getId());

        return new JwtToken(token);
    }

    @Transactional
    public void register(UserRequestDto userRequestDto) {
        usersService.register(userRequestDto);
    }
}
