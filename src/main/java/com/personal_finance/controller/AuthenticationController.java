package com.personal_finance.controller;

import com.personal_finance.dto.user.LoginUserDto;
import com.personal_finance.dto.user.UserRequestDto;
import com.personal_finance.security.JwtService;
import com.personal_finance.security.dtos.AccessToken;
import com.personal_finance.security.dtos.RefreshToken;
import com.personal_finance.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<AccessToken> authenticate(@RequestBody LoginUserDto dto) {
        AccessToken token = authenticationService.login(dto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDto userRequestDto) {
        authenticationService.register(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public AccessToken refresh(@RequestBody RefreshToken request) {
        return authenticationService.refresh(request.refreshToken());
    }
}
