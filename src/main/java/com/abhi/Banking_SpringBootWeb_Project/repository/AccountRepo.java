package com.abhi.Banking_SpringBootWeb_Project.repository;

import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account,Integer> {

    Optional<Account> findByAccountNo(long accountNo);
}
