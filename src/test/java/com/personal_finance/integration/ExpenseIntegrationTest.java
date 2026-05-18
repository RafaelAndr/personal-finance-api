package com.personal_finance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_finance.dto.expense.ExpenseRequestDto;
import com.personal_finance.dto.payment.PaymentRequestDto;
import com.personal_finance.entity.Account;
import com.personal_finance.entity.Expense;
import com.personal_finance.entity.Users;
import com.personal_finance.entity.enums.ExpenseCategory;
import com.personal_finance.entity.enums.PaymentMethod;
import com.personal_finance.entity.enums.Role;
import com.personal_finance.repository.AccountRepository;
import com.personal_finance.repository.ExpenseRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Users user;

    private Account account;

    @BeforeEach
    void setup(){

        expenseRepository.deleteAll();
        accountRepository.deleteAll();
        usersRepository.deleteAll();

        user = new Users();

        user.setName("Rafael");
        user.setUsername("rafael@gmail.com");
        user.setPassword("123");
        user.setRole(Role.ROLE_CLIENT);

        user = usersRepository.save(user);

        account = new Account();

        account.setBankName("Banco do Brasil");
        account.setBalance(BigDecimal.valueOf(1000));
        account.setUser(user);
        account = accountRepository.save(account);
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldCreateExpenseSuccessfully() throws Exception {

        ExpenseRequestDto request = new ExpenseRequestDto(
                account.getId(),
                "Medical expenses",
                BigDecimal.valueOf(100),
                ExpenseCategory.HEALTH
        );

        mockMvc.perform(
                        post("/expenses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description")
                                .value("Medical expenses")
                );

        List<Expense> expenses = expenseRepository.findAll();

        assertEquals(1, expenses.size());

        Expense saved = expenses.get(0);

        assertEquals("Medical expenses", saved.getDescription());

        assertEquals(ExpenseCategory.HEALTH, saved.getExpenseCategory());

        assertEquals(false, saved.isPaid());

        assertEquals(0, BigDecimal.valueOf(100).compareTo(saved.getValue()));

        assertEquals(account.getId(), saved.getAccount().getId());

        assertEquals(user.getId(), saved.getUser().getId());
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetExpenseDetailSuccessfully() throws Exception {

        Expense expense = new Expense();

        expense.setDescription("Mercado");
        expense.setExpenseCategory(ExpenseCategory.FOOD);
        expense.setValue(BigDecimal.valueOf(100));
        expense.setUser(user);
        expense.setAccount(account);

        expense = expenseRepository.save(expense);

        mockMvc.perform(
                        get("/expenses/{id}", expense.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Mercado"))
                .andExpect(jsonPath("$.expenseCategory").value("FOOD"))
                .andExpect(jsonPath("$.value").value(100));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetAllAccountExpensesSuccessfully() throws Exception {

        Expense expense1 = new Expense();

        expense1.setDescription("Mercado");
        expense1.setExpenseCategory(ExpenseCategory.FOOD);
        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);
        expense1.setUser(user);
        expense1.setAccount(account);

        Expense expense2 = new Expense();

        expense2.setDescription("Uber");
        expense2.setExpenseCategory(ExpenseCategory.TRANSPORT);
        expense2.setValue(BigDecimal.valueOf(50));
        expense2.setPaid(true);
        expense2.setUser(user);
        expense2.setAccount(account);

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(
                        get("/expenses/account/{accountId}", account.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].description").value("Mercado"))
                .andExpect(jsonPath("$[1].description").value("Uber"));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldDeleteExpenseSuccessfully() throws Exception {

        Expense expense = new Expense();

        expense.setDescription("Mercado");
        expense.setExpenseCategory(ExpenseCategory.FOOD);
        expense.setValue(BigDecimal.valueOf(100));
        expense.setPaid(false);

        expense.setUser(user);
        expense.setAccount(account);

        expense = expenseRepository.save(expense);

        mockMvc.perform(
                        delete("/expenses/{id}", expense.getId()))
                .andExpect(status().isNoContent());

        assertFalse(expenseRepository.existsById(expense.getId()));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldPayExpenseSuccessfully() throws Exception {

        Expense expense = new Expense();

        expense.setDescription("Mercado");
        expense.setExpenseCategory(ExpenseCategory.FOOD);
        expense.setValue(BigDecimal.valueOf(100));
        expense.setPaid(false);

        expense.setUser(user);
        expense.setAccount(account);

        expense = expenseRepository.save(expense);

        PaymentRequestDto request = new PaymentRequestDto(
                        account.getId(),
                        PaymentMethod.PIX
                );

        mockMvc.perform(
                        post("/expenses/pay/{id}", expense.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Expense updated = expenseRepository.findById(expense.getId()).orElseThrow();

        assertTrue(updated.isPaid());
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetNotPaidExpensesSuccessfully() throws Exception {

        Expense expense1 = new Expense();

        expense1.setDescription("Mercado");
        expense1.setExpenseCategory(ExpenseCategory.FOOD);
        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);

        expense1.setUser(user);
        expense1.setAccount(account);

        Expense expense2 = new Expense();

        expense2.setDescription("Uber");
        expense2.setExpenseCategory(ExpenseCategory.TRANSPORT);
        expense2.setValue(BigDecimal.valueOf(50));
        expense2.setPaid(true);

        expense2.setUser(user);
        expense2.setAccount(account);

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(
                        get("/expenses/not-paid")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Mercado"));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetNotPaidExpensesByAccountSuccessfully() throws Exception {

        Expense expense1 = new Expense();

        expense1.setDescription("Mercado");
        expense1.setExpenseCategory(ExpenseCategory.FOOD);
        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);

        expense1.setUser(user);
        expense1.setAccount(account);

        Expense expense2 = new Expense();

        expense2.setDescription("Uber");
        expense2.setExpenseCategory(ExpenseCategory.TRANSPORT);
        expense2.setValue(BigDecimal.valueOf(50));
        expense2.setPaid(true);

        expense2.setUser(user);
        expense2.setAccount(account);

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(
                        get("/expenses/not-paid/{accountId}", account.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Mercado"));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetPaidExpensesSuccessfully() throws Exception {

        Expense expense1 = new Expense();

        expense1.setDescription("Mercado");
        expense1.setExpenseCategory(ExpenseCategory.FOOD);
        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);

        expense1.setUser(user);
        expense1.setAccount(account);

        Expense expense2 = new Expense();

        expense2.setDescription("Uber");
        expense2.setExpenseCategory(ExpenseCategory.TRANSPORT);
        expense2.setValue(BigDecimal.valueOf(50));
        expense2.setPaid(true);

        expense2.setUser(user);
        expense2.setAccount(account);

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(
                        get("/expenses/paid")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Uber"));
    }

    @Test
    @WithMockUser(username = "rafael@gmail.com")
    void shouldGetTotalExpenseAccountValueSuccessfully() throws Exception {

        Expense expense1 = new Expense();

        expense1.setDescription("Mercado");
        expense1.setExpenseCategory(ExpenseCategory.FOOD);
        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);
        expense1.setUser(user);
        expense1.setAccount(account);

        Expense expense2 = new Expense();

        expense2.setDescription("Uber");
        expense2.setExpenseCategory(ExpenseCategory.TRANSPORT);
        expense2.setValue(BigDecimal.valueOf(250));
        expense2.setPaid(true);
        expense2.setUser(user);
        expense2.setAccount(account);

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(
                        get("/expenses/account/total-expense/{id}",
                                account.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenseAmount").value(350));
    }
}
