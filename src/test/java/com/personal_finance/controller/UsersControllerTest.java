package com.personal_finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_finance.dto.user.ChangePasswordDto;
import com.personal_finance.dto.user.UserResponseDto;
import com.personal_finance.entity.Users;
import com.personal_finance.entity.enums.Role;
import com.personal_finance.mapper.UserMapper;
import com.personal_finance.security.JwtService;
import com.personal_finance.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAuthenticatedUser() throws Exception {

        UserResponseDto response = new UserResponseDto(
                "Rafael",
                "rafael@gmail.com",
                Role.ROLE_CLIENT
        );

        when(usersService.getMe()).thenReturn(response);

        mockMvc.perform(
                        get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rafael"))
                .andExpect(jsonPath("$.username").value("rafael@gmail.com"));

        verify(usersService).getMe();
    }

    @Test
    void shouldReturnUserById() throws Exception {

        UUID id = UUID.randomUUID();

        Users user = new Users();

        UserResponseDto response = new UserResponseDto(
                "Rafael",
                "rafael@gmail.com",
                Role.ROLE_CLIENT
        );

        when(usersService.searchById(any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(response);

        mockMvc.perform(
                        get("/users/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rafael"))
                .andExpect(jsonPath("$.username").value("rafael@gmail.com"));

        verify(usersService).searchById(any());
        verify(userMapper).toDto(user);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        UUID id = UUID.randomUUID();

        when(usersService.searchById(any()))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldChangePassword() throws Exception {

        ChangePasswordDto dto = new ChangePasswordDto(
                "oldPassword",
                "newPassword"
        );

        mockMvc.perform(
                        patch("/users/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNoContent());

        verify(usersService).changePassword(any(ChangePasswordDto.class));
    }

    @Test
    void shouldReturnAllUsers() throws Exception {

        UUID id = UUID.randomUUID();

        Users user = new Users();

        UserResponseDto response = new UserResponseDto(
                "Rafael",
                "rafael@gmail.com",
                Role.ROLE_CLIENT
        );

        when(usersService.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(response);

        mockMvc.perform(
                        get("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rafael"))
                .andExpect(jsonPath("$[0].username").value("rafael@gmail.com"));

        verify(usersService).findAll();
        verify(userMapper).toDto(user);
    }

    @Test
    void shouldDeleteUser() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        delete("/users/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(usersService).deleteUser(eq(id));
    }

    @Test
    void shouldPromoteUserToAdmin() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        put("/users/{id}/promote", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        verify(usersService).promoteToAdmin(eq(id));
    }
}