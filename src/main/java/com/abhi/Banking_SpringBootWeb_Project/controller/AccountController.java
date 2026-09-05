package com.abhi.Banking_SpringBootWeb_Project.controller;

import com.abhi.Banking_SpringBootWeb_Project.dto.AmountRequest;
import com.abhi.Banking_SpringBootWeb_Project.dto.TransactionRequest;
import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import com.abhi.Banking_SpringBootWeb_Project.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/account/user/{userId}")
    public ResponseEntity<?> getAccountByUserId(@PathVariable int userId){
        Account account = accountService.getAccountByUserId(userId);
        if(account != null){
            return new ResponseEntity<>(account, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Account not Found" , HttpStatus.NOT_FOUND);
        }
    }



    @PutMapping("/account/credit")
    public ResponseEntity<String> creditMoney(@RequestBody AmountRequest amountRequest){
        Account account = accountService.creditMoney(amountRequest.getAccountNo(),amountRequest.getAmount());
        if(account != null){
            return new ResponseEntity<>("Amount credited Successfully. New Balance:- "+account.getBalance(),HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Failed to credit Amount ",HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/account/withdraw")
    public ResponseEntity<String> debitMoney(@RequestBody AmountRequest amountRequest){
        Account account = accountService.debitMoney(amountRequest.getAccountNo(),amountRequest.getAmount());
        if(account != null){
            return new ResponseEntity<>("Amount debited Successfully. New Balance:- "+account.getBalance(),HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Failed to debit Amount ",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/account/transaction")
    public ResponseEntity<String> transferMoney(@RequestBody TransactionRequest request){
        Account account = accountService.transferMoney(request.getReceiverAccountNo(),request.getSenderAccountNo(),request.getAmount());
        if(account != null){
            return new ResponseEntity<>("Amount Transfered Successfully. New Balance:- "+account.getBalance(),HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Failed to Transfer Amount ",HttpStatus.BAD_REQUEST);
        }
    }



}
