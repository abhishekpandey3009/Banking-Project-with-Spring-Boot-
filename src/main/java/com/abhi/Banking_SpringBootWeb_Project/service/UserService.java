package com.abhi.Banking_SpringBootWeb_Project.service;

import com.abhi.Banking_SpringBootWeb_Project.config.PasswordConfig;
import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    public User addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);
        accountService.addAccount(savedUser);
        return savedUser;
    }

    public User getUserById(int id) {
        return userRepo.findById(id).orElse(null);
    }

    public User checkuserByMobileNo(String mobile_no) {
        return  userRepo.findByMobileNo(mobile_no);
    }
    public User updateUser(User user) {
        User existing = userRepo.findById(user.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found with id " + user.getUser_id()));

        existing.setName(user.getName());
        existing.setMobileNo(user.getMobileNo());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepo.save(existing);
    }

    public void deleteUser(User user) {
        int id = user.getUser_id();
        accountService.deleteAccount(id);
        userRepo.delete(user);
    }

    public String userLogin(User user) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getMobileNo() , user.getPassword()));

        if(authentication.isAuthenticated())
            return jwtService.generateToken(user.getMobileNo());


        return null;

    }
}
