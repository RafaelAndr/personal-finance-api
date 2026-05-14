package com.personal_finance.service;

import com.personal_finance.dto.account.*;
import com.personal_finance.entity.Account;
import com.personal_finance.entity.Users;
import com.personal_finance.exception.AccessForbiddenException;
import com.personal_finance.exception.AccountHasNoUserException;
import com.personal_finance.exception.EntityAlreadyExistsException;
import com.personal_finance.mapper.AccountMapper;
import com.personal_finance.repository.AccountRepository;
import com.personal_finance.security.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private AccountMapper accountMapper;
    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldSaveAccount_WhenDataAreValid(){
        Users user = new Users();

        when(securityService.getUserLoggedIn()).thenReturn(user);

        AccountRequestDto accountRequestDto = new AccountRequestDto(
                "Nubank"
        );

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBankName("Nubank");

        AccountResponseDto accountResponseDto = new AccountResponseDto(
                accountId, BigDecimal.ZERO, "Nubank"
        );

        when(accountMapper.toEntity(accountRequestDto)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountResponseDto);

        when(accountRepository.existsByBankNameAndUser(anyString(), any()))
                .thenReturn(false);

        AccountResponseDto result = accountService.save(accountRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.bankName()).isEqualTo("Nubank");
        assertThat(result.balance()).isEqualTo(BigDecimal.ZERO);

        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void SaveShouldThrowException_WhenBankNameAlreadyExists(){
        Users user = new Users();

        when(securityService.getUserLoggedIn()).thenReturn(user);

        AccountRequestDto dto = new AccountRequestDto("Nubank");

        Account account = new Account();
        account.setBankName("Nubank");

        when(accountMapper.toEntity(dto)).thenReturn(account);

        when(accountRepository.existsByBankNameAndUser(anyString(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> accountService.save(dto))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Bank name already exists");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void getExpense_ShouldReturnExpense_WhenExpenseExists(){
        UUID id = UUID.randomUUID();

        Account account = new Account();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        Account result = accountService.searchById(id);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void getExpense_ShouldThrowException_WhenAccountDoNotExists(){
        UUID id = UUID.randomUUID();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.searchById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Account not found");
    }

    @Test
    void shouldEditBankName_WhenNewNameIsValidAndNotDuplicated() {
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBankName("Banco do Brasil");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.existsByBankNameAndUser("Nubank", user))
                .thenReturn(false);

        AccountRequestDto dto = new AccountRequestDto("Nubank");

        accountService.editBankName(accountId, dto);

        assertThat(account.getBankName()).isEqualTo("Nubank");
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void editShouldThrowException_WhenBankNameAlreadyExists() {
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBankName("Banco do Brasil");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.existsByBankNameAndUser("Nubank", user))
                .thenReturn(true);

        AccountRequestDto dto = new AccountRequestDto("Nubank");

        assertThatThrownBy(() -> accountService.editBankName(accountId, dto))
                .isInstanceOf(EntityAlreadyExistsException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAccount_WhenUserIsOwner(){
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.delete(accountId);

        verify(accountRepository).deleteById(accountId);
    }

    @Test
    void deleteThrowException_WhenUserIsNotOwner(){
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Users otherUser = new Users();
        otherUser.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(otherUser);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.delete(accountId))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessage("You are not allowed to access this account");
    }

    @Test
    void deleteThrowException_WhenAccountHasNoUser(){
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.delete(accountId))
                .isInstanceOf(AccountHasNoUserException.class)
                .hasMessage("Account has no user associated");
    }

    @Test
    void shouldReturnUserAccounts_WhenUserIsLoggedIn(){
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setName("Rafael");

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setBankName("Nubank");

        List<Account> accounts = List.of(account);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.findByUser(user)).thenReturn(accounts);

        List<AccountBalanceDto> result = accountService.getUserAccounts();

        assertThat(result).hasSize(1);

        AccountBalanceDto dto = result.getFirst();

        assertThat(dto.id()).isEqualTo(accountId);
        assertThat(dto.owner()).isEqualTo("Rafael");
        assertThat(dto.balance()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(dto.bankName()).isEqualTo("Nubank");
    }

    @Test
    void shouldReturnUserTotalBalance_WhenUserIsLoggedIn(){
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setName("Rafael");

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(100));

        Account account2 = new Account();
        UUID account2Id = UUID.randomUUID();
        account2.setId(account2Id);
        account2.setUser(user);
        account2.setBalance(BigDecimal.valueOf(100));

        List<Account> accounts = List.of(account, account2);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.findByUser(user)).thenReturn(accounts);

        AccountTotalBalanceDto result = accountService.getTotalUserBalance();

        assertThat(result.owner()).isEqualTo("Rafael");
        assertThat(result.totalBalance()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    void shouldAddAmount_WhenUserIsOwner(){
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(0));

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        UpdateBalanceDto dto = new UpdateBalanceDto(BigDecimal.valueOf(500));

        accountService.addAmount(accountId, dto);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));

        verify(accountRepository).save(account);
    }

    @Test
    void shouldRemoveAmount_WhenUserIsOwner(){
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(500));

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        UpdateBalanceDto dto = new UpdateBalanceDto(BigDecimal.valueOf(100));

        accountService.removeAmount(accountId, dto);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(400));

        verify(accountRepository).save(account);
    }
}
