package com.personal_finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_finance.dto.user.UserRequestDto;
import com.personal_finance.security.JwtService;
import com.personal_finance.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    void shouldAuthenticateUser() throws Exception {
//
//        LoginUserDto request = new LoginUserDto(
//                "rafael@gmail.com",
//                "123456"
//        );
//
//        AccessToken response = new AccessToken(
//                "jwt-token"
//        );
//
//        when(authenticationService.login(any(LoginUserDto.class)))
//                .thenReturn(response);
//
//        mockMvc.perform(
//                        post("/auth")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(request))
//                )
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.token").value("jwt-token"));
//
//        verify(authenticationService).login(any(LoginUserDto.class));
//    }

    @Test
    void shouldRegisterUser() throws Exception {

        UserRequestDto request = new UserRequestDto(
                "Rafael",
                "rafael@gmail.com",
                "123456",
                "123456"
        );

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        verify(authenticationService).register(any(UserRequestDto.class));
    }
}