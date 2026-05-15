package com.personal_finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_finance.dto.account.*;
import com.personal_finance.entity.Account;
import com.personal_finance.mapper.AccountMapper;
import com.personal_finance.security.JwtService;
import com.personal_finance.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountMapper accountMapper;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAccount() throws Exception {

        UUID id = UUID.randomUUID();

        AccountRequestDto request = new AccountRequestDto(
                "Nubank"
        );

        AccountResponseDto response = new AccountResponseDto(
                id,
                BigDecimal.valueOf(0),
                "Nubank"
        );

        when(accountService.save(any())).thenReturn(response);

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.bankName").value("Nubank"));

        verify(accountService).save(any());
    }

    @Test
    void shouldGetAccount() throws Exception{

        UUID id = UUID.randomUUID();

        AccountResponseDto response = new AccountResponseDto(
                id,
                BigDecimal.valueOf(0),
                "Nubank"
        );

        Account account = new Account();

        when(accountService.searchById(any())).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(response);

        mockMvc.perform(
                        get("/accounts/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.bankName").value("Nubank"));

        verify(accountService).searchById(any());
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() throws Exception {

        UUID id = UUID.randomUUID();

        when(accountService.searchById(any()))
                .thenThrow(new EntityNotFoundException("Expense not found"));

        mockMvc.perform(get("/accounts/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204WhenBankNameIsUpdated() throws Exception{
        UUID id = UUID.randomUUID();

        AccountRequestDto request = new AccountRequestDto("Nubank");

        mockMvc.perform(
                        patch("/accounts/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNoContent());

        verify(accountService).editBankName(eq(id), any(AccountRequestDto.class));
    }

    @Test
    void shouldDeleteAccount() throws Exception{
        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        delete("/accounts/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(accountService).delete(eq(id));
    }

    @Test
    void shouldReturnUserAccounts() throws Exception {
        UUID id = UUID.randomUUID();

        List<AccountBalanceDto> accounts = List.of(
                new AccountBalanceDto(id, "Rafael", BigDecimal.ZERO, "Nubank")
        );

        when(accountService.getUserAccounts()).thenReturn(accounts);

        mockMvc.perform(
                        get("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bankName").value("Nubank"))
                .andExpect(jsonPath("$[0].balance").value(0));

        verify(accountService).getUserAccounts();
    }

    @Test
    void shouldReturnUserTotalBalance() throws Exception {

        AccountTotalBalanceDto response = new AccountTotalBalanceDto("Rafael", BigDecimal.valueOf(100));

        when(accountService.getTotalUserBalance()).thenReturn(response);

        mockMvc.perform(
                        get("/accounts/total-balance")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Rafael"))
                .andExpect(jsonPath("$.totalBalance").value(100));

        verify(accountService).getTotalUserBalance();
    }

    @Test
    void shouldDepositAmountSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateBalanceDto request = new UpdateBalanceDto(BigDecimal.valueOf(100));

        mockMvc.perform(
                patch("/accounts/{id}/balance/deposit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNoContent());

        verify(accountService).addAmount(eq(id), any(UpdateBalanceDto.class));
    }

    @Test
    void shouldWithdrawAmountSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateBalanceDto request = new UpdateBalanceDto(BigDecimal.valueOf(100));

        mockMvc.perform(
                patch("/accounts/{id}/balance/withdraw", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNoContent());

        verify(accountService).removeAmount(eq(id), any(UpdateBalanceDto.class));
    }
}
