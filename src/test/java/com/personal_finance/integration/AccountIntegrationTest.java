package com.personal_finance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_finance.dto.account.AccountRequestDto;
import com.personal_finance.dto.account.UpdateBalanceDto;
import com.personal_finance.entity.Account;
import com.personal_finance.entity.Users;
import com.personal_finance.entity.enums.Role;
import com.personal_finance.repository.AccountRepository;
import com.personal_finance.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Users user;

    @BeforeEach
    void setup(){

        accountRepository.deleteAll();

        user = new Users();

        user.setName("Rafael");
        user.setUsername("rafael@gmail.com");
        user.setPassword("123");
        user.setRole(Role.ROLE_CLIENT);

        user = usersRepository.save(user);
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldCreateAccountSuccessfully() throws Exception {

        AccountRequestDto request =
                new AccountRequestDto(
                        "Banco do Brasil"
                );

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bankName").value("Banco do Brasil"));

        List<Account> accounts = accountRepository.findAll();

        assertEquals(1, accounts.size());

        assertEquals("Banco do Brasil", accounts.get(0).getBankName());

        assertEquals(BigDecimal.ZERO, accounts.get(0).getBalance());
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetAccountByIdSuccessfully() throws Exception {

        Account account = new Account();

        account.setBankName("Banco do Brasil");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);

        account = accountRepository.save(account);

        mockMvc.perform(
                        get("/accounts/{id}", account.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankName")
                        .value("Banco do Brasil"));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldUpdateBankNameSuccessfully() throws Exception {

        Account account = new Account();

        account.setBankName("Banco do Brasil");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);

        account = accountRepository.save(account);

        AccountRequestDto request = new AccountRequestDto("Caixa");

        mockMvc.perform(
                        patch("/accounts/{id}", account.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNoContent());

        Account updated = accountRepository.findById(account.getId()).orElseThrow();

        assertEquals("Caixa", updated.getBankName());
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldDeleteAccountSuccessfully() throws Exception {

        Account account = new Account();

        account.setBankName("Banco do Brasil");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);

        account = accountRepository.save(account);

        mockMvc.perform(
                        delete("/accounts/{id}",
                                account.getId())
                )
                .andExpect(status().isNoContent());

        assertFalse(accountRepository.existsById(account.getId())
        );
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetUserAccountsSuccessfully() throws Exception {

        Account account1 = new Account();

        account1.setBankName("BB");
        account1.setBalance(BigDecimal.valueOf(100));
        account1.setUser(user);

        Account account2 = new Account();

        account2.setBankName("Caixa");
        account2.setBalance(BigDecimal.valueOf(200));

        account2.setUser(user);

        accountRepository.save(account1);
        accountRepository.save(account2);

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldReturnTotalBalanceSuccessfully() throws Exception {

        Account account1 = new Account();

        account1.setBankName("BB");
        account1.setBalance(BigDecimal.valueOf(100));
        account1.setUser(user);

        Account account2 = new Account();

        account2.setBankName("Caixa");
        account2.setBalance(BigDecimal.valueOf(250));
        account2.setUser(user);

        accountRepository.save(account1);
        accountRepository.save(account2);

        mockMvc.perform(
                        get("/accounts/total-balance")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance")
                        .value(350));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldDepositSuccessfully() throws Exception {

        Account account = new Account();

        account.setBankName("BB");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);

        account = accountRepository.save(account);

        UpdateBalanceDto dto = new UpdateBalanceDto(BigDecimal.valueOf(100));

        mockMvc.perform(
                        patch("/accounts/{id}/balance/deposit", account.getId()
                        ).
                                contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        Account updated = accountRepository.findById(account.getId()).orElseThrow();

        assertEquals(BigDecimal.valueOf(100), updated.getBalance());
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldWithdrawSuccessfully() throws Exception {

        Account account = new Account();

        account.setBankName("BB");
        account.setBalance(BigDecimal.valueOf(200));
        account.setUser(user);

        account = accountRepository.save(account);

        UpdateBalanceDto dto = new UpdateBalanceDto(BigDecimal.valueOf(50));

        mockMvc.perform(
                        patch(
                                "/accounts/{id}/balance/withdraw",
                                account.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        Account updated = accountRepository.findById(account.getId()).orElseThrow();

        assertEquals(BigDecimal.valueOf(150), updated.getBalance());
    }
}