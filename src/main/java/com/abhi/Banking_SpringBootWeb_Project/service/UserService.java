package com.abhi.Banking_SpringBootWeb_Project.service;

import com.abhi.Banking_SpringBootWeb_Project.config.PasswordConfig;
import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);
        accountService.addAccount(savedUser);
        return savedUser;
    }

    public User getUserById(int id) {
        return userRepo.findById(id).orElse(null);
    }

    public User checkuserByMobileNo(long mobile_no) {
        return  userRepo.findByMobileNo(mobile_no).orElse(null);
    }

    public User updateUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);

    }

    public void deleteUser(User user) {
        int id = user.getUser_id();
        accountService.deleteAccount(id);
        userRepo.delete(user);
    }

    public User userLogin(User user) {
        User existing = userRepo.findByNameAndMobileNo(user.getName() , user.getMobileNo() ).orElse(null);
        if (existing == null) return null;
        if (passwordEncoder.matches(user.getPassword(), existing.getPassword()))
            return existing;
        else
            return null;

    }
}
