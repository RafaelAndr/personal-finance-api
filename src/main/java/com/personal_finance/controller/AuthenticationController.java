package com.personal_finance.controller;

import com.personal_finance.dto.user.LoginUserDto;
import com.personal_finance.dto.user.UserRequestDto;
import com.personal_finance.security.dtos.NewAccessToken;
import com.personal_finance.security.dtos.AuthenticationResponse;
import com.personal_finance.security.dtos.RefreshTokenRequest;
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
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody LoginUserDto dto) {
        AuthenticationResponse response = authenticationService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDto userRequestDto) {
        authenticationService.register(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public NewAccessToken refresh(@RequestBody RefreshTokenRequest request) {
        return authenticationService.refresh(request.refreshToken());
    }
}
