package com.personal_finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_finance.dto.account.AccountTotalExpenseDto;
import com.personal_finance.dto.expense.ExpenseRequestDto;
import com.personal_finance.dto.expense.ExpenseResponseDto;
import com.personal_finance.dto.payment.PaymentRequestDto;
import com.personal_finance.entity.Account;
import com.personal_finance.entity.Expense;
import com.personal_finance.entity.enums.ExpenseCategory;
import com.personal_finance.entity.enums.PaymentMethod;
import com.personal_finance.mapper.ExpenseMapper;
import com.personal_finance.security.JwtService;
import com.personal_finance.service.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseMapper expenseMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateExpense() throws Exception {

        ExpenseRequestDto request = new ExpenseRequestDto(
                UUID.randomUUID(),
                "Bom",
                BigDecimal.valueOf(50),
                ExpenseCategory.LEISURE
        );

        ExpenseResponseDto response = new ExpenseResponseDto(
                UUID.randomUUID(),
                null,
                null,
                "BOM",
                BigDecimal.valueOf(50),
                ExpenseCategory.LEISURE
        );

        when(expenseService.save(any())).thenReturn(response);

        ResultActions result = mockMvc.perform(
                post("/expenses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
        );

        result
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value").value(BigDecimal.valueOf(50)))
                .andExpect(jsonPath("$.description").value("BOM"))
                .andExpect(jsonPath("$.expenseCategory").value("LEISURE"));

        verify(expenseService).save(any());
    }

    @Test
    void shouldGetExpense() throws Exception {

        UUID id = UUID.randomUUID();

        Expense expense = new Expense();
        expense.setId(id);

        ExpenseResponseDto response = new ExpenseResponseDto(
                id,
                null,
                null,
                "BOM",
                BigDecimal.valueOf(50),
                ExpenseCategory.LEISURE
        );

        when(expenseService.getExpense(any())).thenReturn(expense);
        when(expenseMapper.toDto(expense)).thenReturn(response);

        mockMvc.perform(
                        get("/expenses/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.description").value("BOM"))
                .andExpect(jsonPath("$.value").value(50))
                .andExpect(jsonPath("$.expenseCategory").value("LEISURE"));
    }

    @Test
    void shouldReturnNotFoundWhenExpenseDoesNotExist() throws Exception {

        UUID id = UUID.randomUUID();

        when(expenseService.getExpense(any()))
                .thenThrow(new EntityNotFoundException("Expense not found"));

        mockMvc.perform(get("/expenses/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllAccountsExpenses() throws Exception {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = new Account();
        account.setId(accountId);

        Expense expense = new Expense();
        expense.setId(id);
        expense.setAccount(account);

        ExpenseResponseDto response = new ExpenseResponseDto(
                id,
                null,
                null,
                "BOM",
                BigDecimal.valueOf(50),
                ExpenseCategory.LEISURE
        );

        List<Expense> expenses = List.of(expense);

        when(expenseService.getAllAccountExpenses(accountId)).thenReturn(expenses);
        when(expenseMapper.toDto(expense)).thenReturn(response);

        mockMvc.perform(get("/expenses/account/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].description").value("BOM"))
                .andExpect(jsonPath("$[0].value").value(50))
                .andExpect(jsonPath("$[0].expenseCategory").value("LEISURE"));

        verify(expenseService).getAllAccountExpenses(accountId);
        verify(expenseMapper).toDto(expense);
    }

    @Test
    void shouldDeleteExpense() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(expenseService).delete(id);

        mockMvc.perform(delete("/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(expenseService).delete(id);
    }

    @Test
    void shouldPayExpense() throws Exception {
        UUID id = UUID.randomUUID();

        PaymentRequestDto request = new PaymentRequestDto(
                null,
                PaymentMethod.BOLETO
        );

        doNothing().when(expenseService).payExpense(id, request);

        mockMvc.perform(post("/expenses/pay/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(expenseService).payExpense(id, request);
    }

    @Test
    void shouldReturnNotPaidExpenses() throws Exception {
        UUID id = UUID.randomUUID();

        ExpenseResponseDto response = new ExpenseResponseDto(
                id,
                null,
                null,
                "BOM",
                BigDecimal.valueOf(50),
                ExpenseCategory.LEISURE
        );

        List<ExpenseResponseDto> expenses = List.of(response);

        when(expenseService.listNotPaidExpenses()).thenReturn(expenses);

        mockMvc.perform(get("/expenses/not-paid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].description").value("BOM"))
                .andExpect(jsonPath("$[0].value").value(50))
                .andExpect(jsonPath("$[0].expenseCategory").value("LEISURE"));

        verify(expenseService).listNotPaidExpenses();
    }

    @Test
    void shouldReturnNotPaidExpensesByAccount() throws Exception {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        ExpenseResponseDto response = new ExpenseResponseDto(
                id,
                null,
                null,
                "BOM",
                BigDecimal.valueOf(50),
                ExpenseCategory.LEISURE
        );

        List<ExpenseResponseDto> expenses = List.of(response);

        when(expenseService.listNotPaidExpensesByAccount(accountId)).thenReturn(expenses);

        mockMvc.perform(get("/expenses/not-paid/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].description").value("BOM"))
                .andExpect(jsonPath("$[0].value").value(50))
                .andExpect(jsonPath("$[0].expenseCategory").value("LEISURE"));

        verify(expenseService).listNotPaidExpensesByAccount(accountId);
    }

    @Test
    void shouldReturnPaidExpenses() throws Exception {
        UUID id = UUID.randomUUID();

        ExpenseResponseDto response = new ExpenseResponseDto(
                id,
                null,
                null,
                "BOM",
                BigDecimal.valueOf(50),
                ExpenseCategory.LEISURE
        );

        List<ExpenseResponseDto> expenses = List.of(response);

        when(expenseService.listPaidExpenses()).thenReturn(expenses);

        mockMvc.perform(get("/expenses/paid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].description").value("BOM"))
                .andExpect(jsonPath("$[0].value").value(50))
                .andExpect(jsonPath("$[0].expenseCategory").value("LEISURE"));

        verify(expenseService).listPaidExpenses();
    }

    @Test
    void shouldReturnTotalAccountExpenseValue() throws Exception {
        UUID id = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(50);

        AccountTotalExpenseDto response = new AccountTotalExpenseDto(id, amount);

        when(expenseService.getTotalExpenseAccountValue(id)).thenReturn(response);

        mockMvc.perform(get("/expenses/account/total-expense/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(id.toString()))
                .andExpect(jsonPath("$.totalExpenseAmount").value(50));

        verify(expenseService).getTotalExpenseAccountValue(id);
    }
}