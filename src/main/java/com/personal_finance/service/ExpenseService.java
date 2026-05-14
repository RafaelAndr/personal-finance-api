package com.personal_finance.service;

import com.personal_finance.dto.account.AccountTotalExpenseDto;
import com.personal_finance.dto.expense.ExpenseRequestDto;
import com.personal_finance.dto.expense.ExpenseResponseDto;
import com.personal_finance.dto.payment.PaymentRequestDto;
import com.personal_finance.entity.Account;
import com.personal_finance.entity.Expense;
import com.personal_finance.entity.Payment;
import com.personal_finance.entity.Users;
import com.personal_finance.exception.*;
import com.personal_finance.mapper.ExpenseMapper;
import com.personal_finance.repository.ExpenseRepository;
import com.personal_finance.security.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final SecurityService securityService;
    private final AccountService accountService;
    private final PaymentService paymentService;

    private Users getLoggedUser(){
        return securityService.getUserLoggedIn();
    }

    public ExpenseResponseDto save(ExpenseRequestDto expenseRequestDto) {

        Users userLoggedIn = getLoggedUser();
        UUID userId = userLoggedIn.getId();

        log.info("event=expense_create_start userId={}", userId);

        if (expenseRequestDto.value() != null &&
                expenseRequestDto.value().compareTo(BigDecimal.ZERO) <= 0) {

            log.warn("event=expense_invalid_value value={} userId={}", expenseRequestDto.value(), userId);

            throw new NegativeValueException("You can't register a negative or zero expense value");
        }

        Expense expense = expenseMapper.toEntity(expenseRequestDto);
        expense.setUser(userLoggedIn);

        if (expenseRequestDto.accountId() != null) {
            UUID accountId = expenseRequestDto.accountId();

            Account account = accountService.searchById(accountId);

            account.validateOwnership(userId);

            expense.setAccount(account);
        }

        Expense savedExpense = expenseRepository.save(expense);

        log.info("event=expense_created expenseId={} userId={}", savedExpense.getId(), userId);

        return expenseMapper.toDto(savedExpense);
    }

    public Expense getExpense(UUID id){

        return expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Expense not found for id={}", id);
                    return new EntityNotFoundException("Expense not found");
                });
    }

    public List<Expense> getAllAccountExpenses(UUID accountId){

        Account account = accountService.searchById(accountId);

        return expenseRepository.findByAccount(account);
    }

    @Transactional
    public void payExpense(UUID id, PaymentRequestDto paymentRequestDto){

        log.info("Initiating payment for expense id={}", id);

        Users userLoggedIn = securityService.getUserLoggedIn();
        log.debug("Authenticated user id={}", userLoggedIn.getId());

        Expense expense = getExpense(id);

        expense.validateOwnership(userLoggedIn.getId());

        if (expense.isPaid()) {
            log.warn("Attempt to pay already paid expense id={} by user id={}", id, userLoggedIn.getId());
            throw new ExpenseAlreadyPaidException("Expense is already paid");
        }

        if (!expense.getUser().getId().equals(userLoggedIn.getId())){
            log.warn("Unauthorized payment attempt: user id={} trying to pay expense id={} owned by user id={}",
                    userLoggedIn.getId(), id, expense.getUser().getId());
            throw new AccessForbiddenException("You can't pay this expense because it is not yours");
        }

        Payment payment = new Payment();
        payment.setExpense(expense);
        payment.setUser(userLoggedIn);
        payment.setPaymentMethod(paymentRequestDto.paymentMethod());
        payment.setExpenseValue(expense.getValue());
        payment.setUserName(expense.getUser().getName());

        if (expense.getAccount() != null){

            Account account = accountService.searchById(expense.getAccount().getId());

            log.debug("Account id={} balance before payment={}", account.getId(), account.getBalance());

            account.debit(expense.getValue());

            log.info("Debiting account id={} new balance={}", account.getId(), account.getBalance());

            payment.setAccount(account);
        }

        expense.markAsPaid();

        paymentService.saveExpensePayment(payment);
        expenseRepository.save(expense);

        log.info("Expense id={} paid successfully by user id={}", id, userLoggedIn.getId());
    }

    public void delete(UUID id){
        Users userLogged = getLoggedUser();

        log.info("User {} attempting to delete expense {}", userLogged.getId(), id);

        Expense expense = getExpense(id);

        expense.validateOwnership(userLogged.getId());

        expenseRepository.delete(expense);

        log.info("Expense {} deleted by user {}", id, userLogged.getId());
    }

    public List<ExpenseResponseDto> listNotPaidExpenses(){
        Users userLogged = getLoggedUser();

        List<Expense> expenses = expenseRepository.findNotPaidExpenses(userLogged.getId());

        return expenses.stream().map(expenseMapper::toDto).toList();
    }

    public List<ExpenseResponseDto> listNotPaidExpensesByAccount(UUID accountId){
        Users userLogged = getLoggedUser();

        Account account = accountService.searchById(accountId);

        account.validateOwnership(userLogged.getId());

        List<Expense> expenses = expenseRepository.findNotPaidExpensesByAccount(accountId);

        return expenses.stream().map(expenseMapper::toDto).toList();
    }

    public List<ExpenseResponseDto> listPaidExpenses(){
        Users userLogged = getLoggedUser();

        List<Expense> expenses = expenseRepository.findPaidExpenses(userLogged.getId());

        return expenses.stream().map(expenseMapper::toDto).toList();
    }

    public AccountTotalExpenseDto getTotalExpenseAccountValue(UUID accountId){
        BigDecimal amount = expenseRepository.totalAccountExpenseValue(accountId);

        return new AccountTotalExpenseDto(accountId, amount);
    }
}