package com.abhi.Banking_SpringBootWeb_Project.service;

import com.abhi.Banking_SpringBootWeb_Project.model.Account;
import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService -- UserRepo, AccountService, and
 * PasswordEncoder are all mocked, so these run without a database or
 * Spring context.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private AccountService accountService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUser_id(1);
        existingUser.setName("Abhishek");
        existingUser.setMobileNo(7000865275L);
        existingUser.setPassword("$2a$10$hashedvalueplaceholder"); // stored hash
    }

    // ---------- addUser ----------

    @Test
    void addUser_hashesRawPasswordBeforeSaving() {
        User newUser = new User();
        newUser.setName("Priya");
        newUser.setMobileNo(9998887776L);
        newUser.setPassword("mySecret123");

        when(passwordEncoder.encode("mySecret123")).thenReturn("HASHED_VALUE");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountService.addAccount(any(User.class))).thenReturn(new Account());

        User saved = userService.addUser(newUser);

        // The raw password must never reach the database -- only the hash.
        assertEquals("HASHED_VALUE", saved.getPassword());
        assertNotEquals("mySecret123", saved.getPassword());
        verify(passwordEncoder).encode("mySecret123");
    }

    @Test
    void addUser_alsoCreatesAnAccountForTheNewUser() {
        User newUser = new User();
        newUser.setName("Priya");
        newUser.setMobileNo(9998887776L);
        newUser.setPassword("mySecret123");

        when(passwordEncoder.encode(anyString())).thenReturn("HASHED_VALUE");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountService.addAccount(any(User.class))).thenReturn(new Account());

        userService.addUser(newUser);

        verify(accountService).addAccount(newUser);
    }

    // ---------- userLogin ----------

    @Test
    void userLogin_succeedsWithCorrectPassword() {
        User loginAttempt = new User();
        loginAttempt.setName("Abhishek");
        loginAttempt.setMobileNo(7000865275L);
        loginAttempt.setPassword("correctPassword");

        when(userRepo.findByNameAndMobileNo("Abhishek", 7000865275L))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctPassword", existingUser.getPassword()))
                .thenReturn(true);

        User result = userService.userLogin(loginAttempt);

        assertNotNull(result);
        assertEquals(existingUser, result);
    }

    @Test
    void userLogin_failsWithWrongPassword() {
        // The regression this guards against: someone "simplifying" login
        // back to a plain == or .equals() check on the password, which
        // would silently defeat the hashing.
        User loginAttempt = new User();
        loginAttempt.setName("Abhishek");
        loginAttempt.setMobileNo(7000865275L);
        loginAttempt.setPassword("wrongPassword");

        when(userRepo.findByNameAndMobileNo("Abhishek", 7000865275L))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", existingUser.getPassword()))
                .thenReturn(false);

        User result = userService.userLogin(loginAttempt);

        assertNull(result);
    }

    @Test
    void userLogin_failsForUnknownUser() {
        User loginAttempt = new User();
        loginAttempt.setName("Nobody");
        loginAttempt.setMobileNo(1234567890L);
        loginAttempt.setPassword("whatever");

        when(userRepo.findByNameAndMobileNo("Nobody", 1234567890L))
                .thenReturn(Optional.empty());

        User result = userService.userLogin(loginAttempt);

        assertNull(result);
        // No point even hashing/comparing a password for a user that
        // doesn't exist.
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ---------- updateUser ----------

    @Test
    void updateUser_hashesNewPasswordToo() {
        User update = new User();
        update.setUser_id(1);
        update.setName("Abhishek Pandey");
        update.setMobileNo(7000865275L);
        update.setPassword("newRawPassword");

        when(passwordEncoder.encode("newRawPassword")).thenReturn("NEW_HASHED_VALUE");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(update);

        assertEquals("NEW_HASHED_VALUE", result.getPassword());
    }
}
