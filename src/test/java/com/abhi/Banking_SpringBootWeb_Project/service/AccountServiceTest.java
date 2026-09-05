package com.abhi.Banking_SpringBootWeb_Project.service;

import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.repository.AccountRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * These are unit tests -- AccountRepo is mocked, so no real database or
 * Spring context is needed. They only verify AccountService's own logic
 * (the credit/debit/transfer rules), not JPA or HTTP behavior.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUser(new User());
        account.setAccountNo(100001L);
        account.setBalance(BigDecimal.valueOf(1000));
    }

    // ---------- creditMoney ----------

    @Test
    void creditMoney_addsToExistingBalance() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.creditMoney(100001L, BigDecimal.valueOf(500));

        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(result.getBalance()));
        verify(accountRepo).save(account);
    }

    @Test
    void creditMoney_rejectsZeroAmount() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(account));

        Account result = accountService.creditMoney(100001L, BigDecimal.ZERO);

        assertNull(result);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void creditMoney_rejectsNegativeAmount() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(account));

        Account result = accountService.creditMoney(100001L, BigDecimal.valueOf(-50));

        assertNull(result);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void creditMoney_returnsNullForUnknownAccount() {
        when(accountRepo.findByAccountNo(999999L)).thenReturn(Optional.empty());

        Account result = accountService.creditMoney(999999L, BigDecimal.valueOf(500));

        assertNull(result);
        verify(accountRepo, never()).save(any());
    }

    // ---------- debitMoney ----------

    @Test
    void debitMoney_subtractsFromBalance() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.debitMoney(100001L, BigDecimal.valueOf(400));

        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(600).compareTo(result.getBalance()));
    }

    @Test
    void debitMoney_rejectsAmountGreaterThanBalance() {
        // The one bug class that actually matters in a banking app: never
        // let a withdrawal push the balance negative.
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(account));

        Account result = accountService.debitMoney(100001L, BigDecimal.valueOf(5000));

        assertNull(result);
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(account.getBalance()), "balance must be untouched");
        verify(accountRepo, never()).save(any());
    }

    @Test
    void debitMoney_allowsWithdrawingExactBalance() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.debitMoney(100001L, BigDecimal.valueOf(1000));

        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getBalance()));
    }

    @Test
    void debitMoney_rejectsZeroOrNegativeAmount() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(account));

        assertNull(accountService.debitMoney(100001L, BigDecimal.ZERO));
        assertNull(accountService.debitMoney(100001L, BigDecimal.valueOf(-10)));
        verify(accountRepo, never()).save(any());
    }

    @Test
    void debitMoney_returnsNullForUnknownAccount() {
        when(accountRepo.findByAccountNo(999999L)).thenReturn(Optional.empty());

        assertNull(accountService.debitMoney(999999L, BigDecimal.valueOf(100)));
    }

    // ---------- addAccount ----------

    @Test
    void addAccount_startsAtZeroBalance() {
        User user = new User();
        user.setUser_id(7);
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.addAccount(user);

        assertEquals(0, BigDecimal.ZERO.compareTo(result.getBalance()));
        assertEquals(user, result.getUser());
    }

    // ---------- lookups ----------

    @Test
    void getAccountByAccountNo_returnsNullWhenMissing() {
        when(accountRepo.findByAccountNo(42L)).thenReturn(Optional.empty());

        assertNull(accountService.getAccountByAccountNo(42L));
    }

    @Test
    void getAccountByUserId_delegatesToFindById() {
        when(accountRepo.findById(7)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountByUserId(7);

        assertEquals(account, result);
    }
}
