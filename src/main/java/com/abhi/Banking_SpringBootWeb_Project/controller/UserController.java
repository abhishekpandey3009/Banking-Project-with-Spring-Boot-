package com.abhi.Banking_SpringBootWeb_Project.controller;

import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;



    @PostMapping("/user")
    public ResponseEntity<String> addUser(@RequestBody User user){
        User user1 = userService.addUser(user);
        if(user1 != null){
            return new ResponseEntity<>("Account created successfully" , HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Account Creation failed",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> userLogin(@RequestBody User user){
        User user1 = userService.userLogin(user);
        if(user1 != null){
            return new ResponseEntity<>(user1 , HttpStatus.OK);
        }else{
            return new ResponseEntity<>("User Login Failed" , HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/id/{id}")
    public ResponseEntity<String> checkUserById(@PathVariable int id){
        User user = userService.getUserById(id);
        if(user != null){
            return new ResponseEntity<>("User exsists" , HttpStatus.FOUND);
        }else{
            return new ResponseEntity<>("User does not exsists" , HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/user/mobile_no/{mobile_no}")
    public ResponseEntity<String> checkUserByMobileNo(@PathVariable long mobile_no){
        User user = userService.checkuserByMobileNo(mobile_no);
        if(user != null){
            return new ResponseEntity<>("User Found" , HttpStatus.FOUND);
        }else{
            return new ResponseEntity<>("User Not Found" , HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/user")
    public ResponseEntity<String> updateUser(@RequestBody User user){
        User user1 = userService.updateUser(user);
        if(user1 != null){
            return new ResponseEntity<>("Updated Successfully" , HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Updation Failed" , HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestBody User user){
        int id = user.getUser_id();
        User user1 = userService.getUserById(id);
        if(user1 != null){
            userService.deleteUser(user);
            return new ResponseEntity<>("User deleted" , HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Deletion failed" , HttpStatus.BAD_REQUEST);
        }

    }
}
