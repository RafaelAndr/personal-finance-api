package com.personal_finance.repository;

import com.personal_finance.entity.Account;
import com.personal_finance.entity.Expense;
import com.personal_finance.entity.Users;
import com.personal_finance.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Users user1;
    private Users user2;

    private Account account1;
    private Account account2;

    @BeforeEach
    void setup(){

        expenseRepository.deleteAll();
        accountRepository.deleteAll();
        usersRepository.deleteAll();

        user1 = new Users();

        user1.setName("Rafael");
        user1.setUsername("rafael@gmail.com");
        user1.setPassword("123");
        user1.setRole(Role.ROLE_CLIENT);

        user2 = new Users();

        user2.setName("Maria");
        user2.setUsername("maria@gmail.com");
        user2.setPassword("123");
        user2.setRole(Role.ROLE_CLIENT);

        user1 = usersRepository.save(user1);
        user2 = usersRepository.save(user2);

        account1 = new Account();

        account1.setBankName("BB");
        account1.setBalance(BigDecimal.valueOf(1000));

        account1.setUser(user1);

        account2 = new Account();

        account2.setBankName("Caixa");
        account2.setBalance(BigDecimal.valueOf(500));

        account2.setUser(user2);

        account1 = accountRepository.save(account1);
        account2 = accountRepository.save(account2);
    }

    @Test
    void shouldFindExpensesByAccount(){

        Expense expense1 = new Expense();

        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);
        expense1.setUser(user1);
        expense1.setAccount(account1);

        Expense expense2 = new Expense();

        expense2.setValue(BigDecimal.valueOf(200));
        expense2.setPaid(true);
        expense2.setUser(user1);
        expense2.setAccount(account1);

        Expense expense3 = new Expense();

        expense3.setValue(BigDecimal.valueOf(300));
        expense3.setPaid(false);
        expense3.setUser(user2);
        expense3.setAccount(account2);

        expenseRepository.saveAll(List.of(expense1, expense2, expense3));

        List<Expense> result = expenseRepository.findByAccount(account1);

        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(
                                e -> e.getAccount()
                                        .getId()
                                        .equals(
                                                account1.getId()))
        );
    }

    @Test
    void shouldFindNotPaidExpenses(){

        Expense expense1 = new Expense();

        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);
        expense1.setUser(user1);
        expense1.setAccount(account1);

        Expense expense2 = new Expense();

        expense2.setValue(BigDecimal.valueOf(200));
        expense2.setPaid(true);
        expense2.setUser(user1);
        expense2.setAccount(account1);

        expenseRepository.saveAll(List.of(expense1, expense2));

        List<Expense> result = expenseRepository.findNotPaidExpenses(user1.getId());

        assertEquals(1, result.size());

        assertFalse(result.get(0).isPaid());
    }

    @Test
    void shouldFindPaidExpenses(){

        Expense expense1 = new Expense();

        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(true);
        expense1.setUser(user1);
        expense1.setAccount(account1);

        Expense expense2 = new Expense();

        expense2.setValue(BigDecimal.valueOf(100));
        expense2.setPaid(true);
        expense2.setUser(user1);
        expense2.setAccount(account1);

        Expense expense3 = new Expense();

        expense3.setValue(BigDecimal.valueOf(200));
        expense3.setPaid(false);
        expense3.setUser(user1);
        expense3.setAccount(account1);

        expenseRepository.saveAll(List.of(expense1, expense2, expense3));

        List<Expense> result = expenseRepository.findPaidExpenses(user1.getId());

        assertEquals(2, result.size());

        assertTrue(result.get(0).isPaid());
    }

    @Test
    void shouldFindNotPaidExpensesByAccount(){

        Expense expense1 = new Expense();

        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);
        expense1.setUser(user1);
        expense1.setAccount(account1);

        Expense expense2 = new Expense();

        expense2.setValue(BigDecimal.valueOf(200));
        expense2.setPaid(true);
        expense2.setUser(user1);
        expense2.setAccount(account1);

        expenseRepository.saveAll(List.of(expense1, expense2));

        List<Expense> result = expenseRepository.findNotPaidExpensesByAccount(account1.getId());

        assertEquals(1, result.size());

        assertFalse(result.get(0).isPaid());
    }

    @Test
    void shouldReturnTotalAccountExpenseValue(){

        Expense expense1 = new Expense();

        expense1.setValue(BigDecimal.valueOf(100));
        expense1.setPaid(false);
        expense1.setUser(user1);
        expense1.setAccount(account1);

        Expense expense2 = new Expense();

        expense2.setValue(BigDecimal.valueOf(250));
        expense2.setPaid(true);
        expense2.setUser(user1);
        expense2.setAccount(account1);

        expenseRepository.saveAll(List.of(expense1, expense2));

        BigDecimal total = expenseRepository.totalAccountExpenseValue(account1.getId());

        assertEquals(0, total.compareTo(BigDecimal.valueOf(350)));
    }
}