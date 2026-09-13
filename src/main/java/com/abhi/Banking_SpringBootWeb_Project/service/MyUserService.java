package com.abhi.Banking_SpringBootWeb_Project.service;

import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.model.UserPrincipal;
import com.abhi.Banking_SpringBootWeb_Project.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserService implements UserDetailsService {

    @Autowired
    private UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String userMobileNo) throws UsernameNotFoundException {
        User user = repo.findByMobileNo(userMobileNo);

        if(user == null){
            System.out.println("User not found");
            throw new UsernameNotFoundException("User not found");
        }

        return new UserPrincipal(user);
    }

}
