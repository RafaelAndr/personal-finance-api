package com.personal_finance.service;

import com.personal_finance.dto.expense.ExpenseRequestDto;
import com.personal_finance.dto.expense.ExpenseResponseDto;
import com.personal_finance.dto.payment.PaymentRequestDto;
import com.personal_finance.entity.Account;
import com.personal_finance.entity.Expense;
import com.personal_finance.entity.Payment;
import com.personal_finance.entity.Users;
import com.personal_finance.entity.enums.ExpenseCategory;
import com.personal_finance.entity.enums.PaymentMethod;
import com.personal_finance.entity.enums.Role;
import com.personal_finance.exception.AccessForbiddenException;
import com.personal_finance.exception.ExpenseAlreadyPaidException;
import com.personal_finance.exception.InsufficientBalanceException;
import com.personal_finance.exception.NegativeValueException;
import com.personal_finance.mapper.ExpenseMapper;
import com.personal_finance.repository.ExpenseRepository;
import com.personal_finance.security.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ExpenseMapper expenseMapper;
    @Mock
    private SecurityService securityService;
    @Mock
    private AccountService accountService;
    @Mock
    private PaymentService paymentService;
    @InjectMocks
    private ExpenseService expenseService;

    private Users userLoggedIn;

    private Account account;

    @BeforeEach
    void setUp() {
        userLoggedIn = new Users();
        userLoggedIn.setUsername("rafael");
        account = new Account();
        account.setUser(userLoggedIn);
    }

    @Test
    @DisplayName("Shoud throw ExpenseNegativeException when value is zero")
    void save_ShouldThrowException_WhenValueIsZero() {
        ExpenseRequestDto request = new ExpenseRequestDto(
                null, "Aluguel", BigDecimal.ZERO, ExpenseCategory.HOUSING
        );
        when(securityService.getUserLoggedIn()).thenReturn(userLoggedIn);
        assertThatThrownBy(() -> expenseService.save(request))
                .isInstanceOf(NegativeValueException.class)
                .hasMessageContaining("negative or zero");
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Shoud throw ExpenseNegativeException when value is negative")
    void save_ShouldThrowException_WhenValueIsNegative() {
        ExpenseRequestDto request = new ExpenseRequestDto(
                null, "Aluguel", new BigDecimal("-50.00"), ExpenseCategory.HOUSING
        );
        when(securityService.getUserLoggedIn()).thenReturn(userLoggedIn);
        assertThatThrownBy(() -> expenseService.save(request))
                .isInstanceOf(NegativeValueException.class);
    }

    @Test
    @DisplayName("Should throw AccessForbiddenException when account don't belong to user logged in")
    void save_ShouldThrowException_WhenAccountDoesNotBelongToUser() {
        Users otherUser = new Users();
        otherUser.setUsername("outro");
        Account accountOfOtherUser = new Account();
        accountOfOtherUser.setUser(otherUser);
        UUID accountId = UUID.randomUUID();
        ExpenseRequestDto request = new ExpenseRequestDto(
                accountId, "Netflix", new BigDecimal("50.00"), ExpenseCategory.LEISURE
        );
        UUID loggedUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        userLoggedIn.setId(loggedUserId);
        otherUser.setId(otherUserId);

        Expense expense = new Expense();

        when(securityService.getUserLoggedIn()).thenReturn(userLoggedIn);
        when(accountService.searchById(accountId)).thenReturn(accountOfOtherUser);
        when(expenseMapper.toEntity(request)).thenReturn(expense);
        assertThatThrownBy(() -> expenseService.save(request))
                .isInstanceOf(AccessForbiddenException.class);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save expense successfully when data are valid")
    void save_ShouldPersistExpense_WhenDataIsValid() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userLoggedIn.setId(userId);
        Users accountUser = new Users();
        accountUser.setId(userId);
        account.setUser(accountUser);

        ExpenseRequestDto request = new ExpenseRequestDto(
                accountId, "Supermercado", new BigDecimal("200.00"), ExpenseCategory.FOOD
        );
        Expense expense = new Expense();
        ExpenseResponseDto responseDto = new ExpenseResponseDto(
                UUID.randomUUID(), accountId, UUID.randomUUID(),
                "Supermercado", new BigDecimal("200.00"), ExpenseCategory.FOOD
        );
        when(securityService.getUserLoggedIn()).thenReturn(userLoggedIn);
        when(accountService.searchById(accountId)).thenReturn(account);
        when(expenseMapper.toEntity(request)).thenReturn(expense);
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.toDto(expense)).thenReturn(responseDto);
        ExpenseResponseDto result = expenseService.save(request);
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("Supermercado");
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    @DisplayName("Should get an expense when it exists")
    void getExpense_ShouldReturnExpense_WhenExpenseExists(){
        UUID id = UUID.randomUUID();

        Expense expense = new Expense();

        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        Expense result = expenseService.getExpense(id);

        assertThat(result).isEqualTo(expense);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when expense not found")
    void getExpense_ShouldThrowException_WhenExpenseNotFound() {
        UUID id = UUID.randomUUID();

        when(expenseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpense(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Expense not found");
    }

    @Test
    void getAllAccountExpenses_ShouldReturnExpenses_WhenAccountExists() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account();

        List<Expense> expenses = List.of(new Expense());

        when(accountService.searchById(accountId)).thenReturn(account);
        when(expenseRepository.findByAccount(account)).thenReturn(expenses);

        List<Expense> result = expenseService.getAllAccountExpenses(accountId);

        assertThat(result).isEqualTo(expenses);
    }

    @Test
    void shouldReturnEmptyList_WhenAccountHasNoExpenses() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account();

        when(accountService.searchById(accountId)).thenReturn(account);
        when(expenseRepository.findByAccount(account)).thenReturn(List.of());

        List<Expense> result = expenseService.getAllAccountExpenses(accountId);

        assertThat(result).isEmpty();
    }

    @Test
    void payExpense_ShouldPaySuccessfully_WhenExpenseHasNoAccount() {
        UUID expenseId = UUID.randomUUID();

        Users user = new Users();
        user.setId(UUID.randomUUID());

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(user);
        expense.setPaid(false);
        expense.setValue(new BigDecimal("50.00"));
        expense.setAccount(null);

        PaymentRequestDto request = new PaymentRequestDto(null, PaymentMethod.PIX);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        expenseService.payExpense(expenseId, request);

        assertThat(expense.isPaid()).isTrue();

        verify(paymentService).saveExpensePayment(any(Payment.class));
        verify(expenseRepository).save(expense);

        verify(accountService, never()).searchById(any());
    }

    @Test
    void payExpense_ShouldPaySuccessfully_WhenExpenseHasAccountAndSufficientBalance() {
        UUID expenseId = UUID.randomUUID();

        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setRole(Role.ROLE_CLIENT);

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBalance(new BigDecimal("50.00"));

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(user);
        expense.setPaid(false);
        expense.setValue(new BigDecimal("50.00"));
        expense.setAccount(account);

        PaymentRequestDto request = new PaymentRequestDto(accountId, PaymentMethod.PIX);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(accountService.searchById(accountId)).thenReturn(account);

        expenseService.payExpense(expenseId, request);

        assertThat(user.getRole()).isEqualTo(Role.ROLE_CLIENT);
        assertThat(expense.isPaid()).isTrue();
        assertThat(account.getBalance()).isEqualByComparingTo("0.00");

        verify(paymentService).saveExpensePayment(any(Payment.class));
        verify(expenseRepository).save(expense);

        verify(accountService).searchById(any());
    }

    @Test
    void payExpense_ShouldThrowException_WhenExpenseIsAlreadyPaid() {
        UUID expenseId = UUID.randomUUID();

        Users user = new Users();
        user.setId(UUID.randomUUID());

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(user);
        expense.setPaid(true);
        expense.setValue(new BigDecimal("50.00"));
        expense.setAccount(null);

        PaymentRequestDto request = new PaymentRequestDto(null, PaymentMethod.PIX);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.payExpense(expenseId, request))
                .isInstanceOf(ExpenseAlreadyPaidException.class)
                .hasMessage("Expense is already paid");
    }

    @Test
    void payExpense_ShouldThrowException_WhenExpenseDoesNotBelongToUser() {
        UUID expenseId = UUID.randomUUID();

        Users user = new Users();
        user.setId(UUID.randomUUID());

        Users otherUser = new Users();
        otherUser.setId(UUID.randomUUID());

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(otherUser);
        expense.setPaid(false);
        expense.setValue(new BigDecimal("50.00"));
        expense.setAccount(null);

        PaymentRequestDto request = new PaymentRequestDto(null, PaymentMethod.PIX);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.payExpense(expenseId, request))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessage("You can't access this expense");
    }

    @Test
    void payExpense_ShouldThrowException_WhenAccountHasInsufficientBalance() {
        UUID expenseId = UUID.randomUUID();

        Users user = new Users();
        user.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);
        account.setBalance(new BigDecimal("5.00"));

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(user);
        expense.setPaid(false);
        expense.setValue(new BigDecimal("50.00"));
        expense.setAccount(account);

        PaymentRequestDto request = new PaymentRequestDto(accountId, PaymentMethod.PIX);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(accountService.searchById(accountId)).thenReturn(account);

        assertThatThrownBy(() -> expenseService.payExpense(expenseId, request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Insufficient balance");
    }

    @Test
    void deleteExpense_ShouldDeleteExpense_WhenUserIsOwner() {
        Users user = new Users();
        user.setId(UUID.randomUUID());

        UUID expenseId = UUID.randomUUID();
        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(user);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        expenseService.delete(expenseId);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_ShouldThrowException_WhenUserIsNotOwner() {
        Users user = new Users();
        user.setId(UUID.randomUUID());

        Users otherUser = new Users();
        otherUser.setId(UUID.randomUUID());

        UUID expenseId = UUID.randomUUID();
        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(otherUser);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.delete(expenseId))
                .isInstanceOf(AccessForbiddenException.class);

        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void shouldListNotPaidExpenses() {
        Users user = new Users();
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        Expense expense = new Expense();
        ExpenseResponseDto dto = new ExpenseResponseDto(
                UUID.randomUUID(),
                null,
                userId,
                "Teste",
                new BigDecimal("50.00"),
                ExpenseCategory.LEISURE
        );

        List<Expense> expenses = List.of(expense);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findNotPaidExpenses(userId)).thenReturn(expenses);
        when(expenseMapper.toDto(expense)).thenReturn(dto);

        List<ExpenseResponseDto> result = expenseService.listNotPaidExpenses();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto);

        verify(expenseRepository).findNotPaidExpenses(userId);
        verify(expenseMapper).toDto(expense);
    }

    @Test
    void shouldListNotPaidExpensesByAccount() {
        Users user = new Users();
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(user);

        Expense expense = new Expense();
        expense.setAccount(account);
        ExpenseResponseDto dto = new ExpenseResponseDto(
                UUID.randomUUID(),
                accountId,
                userId,
                "Teste",
                new BigDecimal("50.00"),
                ExpenseCategory.LEISURE
        );

        List<Expense> expenses = List.of(expense);

        when(accountService.searchById(accountId)).thenReturn(account);
        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findNotPaidExpensesByAccount(accountId)).thenReturn(expenses);
        when(expenseMapper.toDto(expense)).thenReturn(dto);

        List<ExpenseResponseDto> result = expenseService.listNotPaidExpensesByAccount(accountId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto);

        verify(expenseRepository).findNotPaidExpensesByAccount(accountId);
        verify(expenseMapper).toDto(expense);
    }

    @Test
    void listNotPaidExpensesByAccount_ShouldThrowException_WhenUserIsNotAccountOwner() {
        Users user = new Users();
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        Users otherUser = new Users();
        otherUser.setId(UUID.randomUUID());

        Account account = new Account();
        UUID accountId = UUID.randomUUID();
        account.setId(accountId);
        account.setUser(otherUser);

        when(accountService.searchById(accountId)).thenReturn(account);
        when(securityService.getUserLoggedIn()).thenReturn(user);

        assertThatThrownBy(() -> expenseService.listNotPaidExpensesByAccount(accountId))
                .isInstanceOf(AccessForbiddenException.class);

        verify(accountService).searchById(accountId);
        verify(expenseRepository, never()).findNotPaidExpensesByAccount(any());
        verify(expenseMapper, never()).toDto(any());
    }

    @Test
    void shouldListPaidExpenses() {
        Users user = new Users();
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        Expense expense = new Expense();

        ExpenseResponseDto dto = new ExpenseResponseDto(
                UUID.randomUUID(),
                null,
                userId,
                "Teste",
                new BigDecimal("50.00"),
                ExpenseCategory.LEISURE
        );

        List<Expense> expenses = List.of(expense);

        when(securityService.getUserLoggedIn()).thenReturn(user);
        when(expenseRepository.findPaidExpenses(userId)).thenReturn(expenses);
        when(expenseMapper.toDto(expense)).thenReturn(dto);

        List<ExpenseResponseDto> result = expenseService.listPaidExpenses();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto);

        verify(expenseRepository).findPaidExpenses(userId);
        verify(expenseMapper).toDto(expense);
    }
}
