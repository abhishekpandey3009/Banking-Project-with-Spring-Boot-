package com.abhi.Banking_SpringBootWeb_Project.serviceTest;

import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.repository.AccountRepo;
import com.abhi.Banking_SpringBootWeb_Project.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService covering account creation and the
 * credit / debit / transfer money-movement logic, including the
 * validation guards (negative amounts, insufficient balance, missing accounts).
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private AccountService accountService;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        senderAccount = new Account();
        senderAccount.setUser_id(1);
        senderAccount.setAccountNo(100001L);
        senderAccount.setBalance(BigDecimal.valueOf(1000));

        receiverAccount = new Account();
        receiverAccount.setUser_id(2);
        receiverAccount.setAccountNo(100002L);
        receiverAccount.setBalance(BigDecimal.valueOf(500));
    }

    @Test
    void addAccount_shouldCreateAccountLinkedToUserWithZeroBalance() {
        User user = new User();
        user.setUser_id(7);

        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account account = accountService.addAccount(user);

        assertEquals(user, account.getUser());
        assertEquals(100007L, account.getAccountNo());
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
    }

    @Test
    void creditMoney_withValidAmount_shouldIncreaseBalance() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.creditMoney(100001L, BigDecimal.valueOf(250));

        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(1250).compareTo(result.getBalance()));
    }

    @Test
    void creditMoney_withZeroOrNegativeAmount_shouldReturnNull() {
        assertNull(accountService.creditMoney(100001L, BigDecimal.ZERO));
        assertNull(accountService.creditMoney(100001L, BigDecimal.valueOf(-50)));
        verify(accountRepo, never()).save(any());
    }

    @Test
    void creditMoney_whenAccountNotFound_shouldReturnNull() {
        when(accountRepo.findByAccountNo(999999L)).thenReturn(Optional.empty());

        assertNull(accountService.creditMoney(999999L, BigDecimal.valueOf(100)));
        verify(accountRepo, never()).save(any());
    }

    @Test
    void debitMoney_withSufficientBalance_shouldDecreaseBalance() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.debitMoney(100001L, BigDecimal.valueOf(400));

        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(600).compareTo(result.getBalance()));
    }

    @Test
    void debitMoney_withInsufficientBalance_shouldReturnNullAndNotSave() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(senderAccount));

        Account result = accountService.debitMoney(100001L, BigDecimal.valueOf(5000));

        assertNull(result);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void debitMoney_withZeroOrNegativeAmount_shouldReturnNull() {
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(senderAccount));

        assertNull(accountService.debitMoney(100001L, BigDecimal.ZERO));
        assertNull(accountService.debitMoney(100001L, BigDecimal.valueOf(-50)));
        verify(accountRepo, never()).save(any());
    }

    @Test
    void transferMoney_withSufficientBalance_shouldMoveFundsBetweenAccounts() {
        when(accountRepo.findByAccountNo(100002L)).thenReturn(Optional.of(receiverAccount));
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.transferMoney(100002L, 100001L, BigDecimal.valueOf(300));

        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(700).compareTo(senderAccount.getBalance()));
        assertEquals(0, BigDecimal.valueOf(800).compareTo(receiverAccount.getBalance()));
        verify(accountRepo, times(1)).save(senderAccount);
        verify(accountRepo, times(1)).save(receiverAccount);
    }

    @Test
    void transferMoney_whenSenderHasInsufficientBalance_shouldReturnNull() {
        when(accountRepo.findByAccountNo(100002L)).thenReturn(Optional.of(receiverAccount));
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(senderAccount));

        Account result = accountService.transferMoney(100002L, 100001L, BigDecimal.valueOf(5000));

        assertNull(result);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void transferMoney_whenEitherAccountMissing_shouldReturnNull() {
        when(accountRepo.findByAccountNo(100002L)).thenReturn(Optional.empty());
        when(accountRepo.findByAccountNo(100001L)).thenReturn(Optional.of(senderAccount));

        Account result = accountService.transferMoney(100002L, 100001L, BigDecimal.valueOf(100));

        assertNull(result);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void transferMoney_withZeroOrNegativeAmount_shouldReturnNull() {
        Account result = accountService.transferMoney(100002L, 100001L, BigDecimal.ZERO);

        assertNull(result);
        verify(accountRepo, never()).save(any());
    }
}
