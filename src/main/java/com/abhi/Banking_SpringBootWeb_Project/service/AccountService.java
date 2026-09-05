package com.abhi.Banking_SpringBootWeb_Project.service;

import com.abhi.Banking_SpringBootWeb_Project.dto.AmountRequest;
import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.repository.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    @Autowired
    private AccountRepo accountRepo;

    public Account addAccount(User user){
        Account account = new Account();
        account.setUser(user);
        account.setAccountNo(100000 + user.getUser_id());
        account.setBalance(BigDecimal.valueOf(0.00));

        return accountRepo.save(account);



    }

    public Account getAccountByUserId(int userId) {
        return accountRepo.findById(userId).orElse(null);
    }

    public Account getAccountByAccountNo(long accountNo){
        return accountRepo.findByAccountNo(accountNo).orElse(null);
    }


    public Account creditMoney(long accountNo , BigDecimal amount) {
        Account account = accountRepo.findByAccountNo(accountNo).orElse(null);

        if(account == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            return null;
        }
        account.setBalance(account.getBalance().add(amount));
        return accountRepo.save(account);
    }

    public Account debitMoney(long accountNo, BigDecimal amount) {
        Account account = accountRepo.findByAccountNo(accountNo).orElse(null);

        if(account == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            return null;
        }
        if(account.getBalance().compareTo(amount) < 0){
            return null;
        }
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepo.save(account);
    }

    public void deleteAccount(int id) {
        accountRepo.deleteById(id);
    }

    public Account transferMoney(long receiverAccountNo, long senderAccountNo, BigDecimal amount) {
        Account receiverAccount = accountRepo.findByAccountNo(receiverAccountNo).orElse(null);
        Account senderAccount = accountRepo.findByAccountNo(senderAccountNo).orElse(null);

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            return null;
        }
        if(receiverAccount == null || senderAccount == null){
            return null;
        }
        if(senderAccount.getBalance().compareTo(amount) < 0){
            return null;
        }
        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));

        accountRepo.save(receiverAccount);
        return accountRepo.save(senderAccount);

    }
}
