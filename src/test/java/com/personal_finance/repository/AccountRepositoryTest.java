package com.personal_finance.repository;

import com.personal_finance.entity.Account;
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
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UsersRepository usersRepository;

    private Users user1;
    private Users user2;

    @BeforeEach
    void setup(){

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
    }

    @Test
    void shouldFindAccountsByUser(){

        Account account1 = new Account();

        account1.setBankName("Banco do Brasil");
        account1.setBalance(BigDecimal.valueOf(100));
        account1.setUser(user1);

        Account account2 = new Account();

        account2.setBankName("Caixa");
        account2.setBalance(BigDecimal.valueOf(200));
        account2.setUser(user1);

        Account account3 = new Account();

        account3.setBankName("Santander");
        account3.setBalance(BigDecimal.valueOf(300));
        account3.setUser(user2);

        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);

        List<Account> result = accountRepository.findByUser(user1);

        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(
                                a -> a.getUser()
                                        .getId()
                                        .equals(user1.getId())
                        )
        );
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoAccounts(){

        List<Account> accounts = accountRepository.findByUser(user1);

        assertTrue(accounts.isEmpty());
    }

    @Test
    void shouldCalculateTotalBalanceByUserId(){

        Account account1 = new Account();

        account1.setBankName("BB");
        account1.setBalance(BigDecimal.valueOf(100));
        account1.setUser(user1);

        Account account2 = new Account();

        account2.setBankName("Caixa");
        account2.setBalance(BigDecimal.valueOf(250));
        account2.setUser(user1);

        Account account3 = new Account();

        account3.setBankName("Santander");
        account3.setBalance(BigDecimal.valueOf(500));
        account3.setUser(user2);

        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);

        BigDecimal total = accountRepository.getTotalBalanceByUserId(user1.getId());

        assertEquals(
                new BigDecimal("350.00"),
                total
        );
    }

    @Test
    void shouldReturnZeroWhenUserHasNoAccounts(){

        BigDecimal total = accountRepository.getTotalBalanceByUserId(user1.getId());

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void shouldReturnTrueWhenBankExistsForUser(){

        Account account = new Account();

        account.setBankName("Banco do Brasil");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user1);

        accountRepository.save(account);

        boolean exists = accountRepository.existsByBankNameAndUser("Banco do Brasil", user1);

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenBankDoesNotExist(){

        boolean exists =
                accountRepository
                        .existsByBankNameAndUser(
                                "Caixa",
                                user1
                        );

        assertFalse(exists);
    }

    @Test
    void shouldReturnFalseWhenBankExistsForAnotherUser(){

        Account account = new Account();

        account.setBankName("Banco do Brasil");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user2);

        accountRepository.save(account);

        boolean exists =
                accountRepository
                        .existsByBankNameAndUser(
                                "Banco do Brasil",
                                user1
                        );

        assertFalse(exists);
    }
}