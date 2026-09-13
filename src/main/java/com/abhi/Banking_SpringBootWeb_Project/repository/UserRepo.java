package com.abhi.Banking_SpringBootWeb_Project.repository;

import com.abhi.Banking_SpringBootWeb_Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {

    User findByMobileNo(String mobile_no);

    Optional<User> findByNameAndMobileNo(String name, String mobileNo);


}
