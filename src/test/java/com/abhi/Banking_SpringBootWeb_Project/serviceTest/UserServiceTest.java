package com.abhi.Banking_SpringBootWeb_Project.serviceTest;



import com.abhi.Banking_SpringBootWeb_Project.model.User;
import com.abhi.Banking_SpringBootWeb_Project.repository.UserRepo;
import com.abhi.Banking_SpringBootWeb_Project.service.AccountService;
import com.abhi.Banking_SpringBootWeb_Project.service.JWTService;
import com.abhi.Banking_SpringBootWeb_Project.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService covering:
 *  - registration (password hashing + linked account creation)
 *  - update (fetch-then-patch behaviour, id required, password left unchanged when blank)
 *  - login (JWT generation keyed on mobile number)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private AccountService accountService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUser_id(1);
        existingUser.setName("Jane Doe");
        existingUser.setMobileNo("9998887777");
        existingUser.setPassword("hashed-old-password");
    }

    @Test
    void addUser_shouldHashPasswordAndCreateLinkedAccount() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setMobileNo("1112223333");
        newUser.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUser_id(5);
            return u;
        });

        User saved = userService.addUser(newUser);

        assertEquals("hashedPassword", saved.getPassword(),
                "Password must be hashed before persisting, never stored in plaintext");
        verify(accountService, times(1)).addAccount(saved);
        verify(userRepo, times(1)).save(newUser);
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateNameAndMobileNo() {
        User incoming = new User();
        incoming.setUser_id(1);
        incoming.setName("Jane D.");
        incoming.setMobileNo("9998887777");
        incoming.setPassword(null); // client did not send a new password

        when(userRepo.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUser(incoming);

        assertEquals("Jane D.", updated.getName());
        assertEquals("9998887777", updated.getMobileNo());
        // Password must remain the original hash since none was supplied
        assertEquals("hashed-old-password", updated.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        // The entity actually saved must be the one fetched from the DB (has the real id),
        // never the raw client object -- this is what prevents update-creates-new-row bugs.
        verify(userRepo, times(1)).save(existingUser);
    }

    @Test
    void updateUser_whenPasswordProvided_shouldHashAndUpdatePassword() {
        User incoming = new User();
        incoming.setUser_id(1);
        incoming.setName("Jane Doe");
        incoming.setMobileNo("9998887777");
        incoming.setPassword("newPlainPassword");

        when(userRepo.findById(1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPlainPassword")).thenReturn("newHashedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUser(incoming);

        assertEquals("newHashedPassword", updated.getPassword());
        verify(passwordEncoder, times(1)).encode("newPlainPassword");
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldThrow() {
        User incoming = new User();
        incoming.setUser_id(999);

        when(userRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(incoming));
        verify(userRepo, never()).save(any());
    }

    @Test
    void getUserById_whenFound_shouldReturnUser() {
        when(userRepo.findById(1)).thenReturn(Optional.of(existingUser));

        User result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    void getUserById_whenNotFound_shouldReturnNull() {
        when(userRepo.findById(42)).thenReturn(Optional.empty());

        assertNull(userService.getUserById(42));
    }

    @Test
    void deleteUser_shouldDeleteAccountAndUserRecord() {
        userService.deleteUser(existingUser);

        verify(accountService, times(1)).deleteAccount(existingUser.getUser_id());
        verify(userRepo, times(1)).delete(existingUser);
    }

    @Test
    void userLogin_whenCredentialsValid_shouldReturnTokenKeyedOnMobileNo() {
        User loginRequest = new User();
        loginRequest.setMobileNo("9998887777");
        loginRequest.setPassword("plainPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("9998887777")).thenReturn("dummy.jwt.token");

        String token = userService.userLogin(loginRequest);

        assertEquals("dummy.jwt.token", token);
        // The token MUST be generated from the mobile number, not the name --
        // otherwise MyUserService.loadUserByUsername() cannot resolve it later.
        verify(jwtService, times(1)).generateToken("9998887777");
        verify(jwtService, never()).generateToken(loginRequest.getName());
    }

    @Test
    void userLogin_whenAuthenticationNotAuthenticated_shouldReturnNull() {
        User loginRequest = new User();
        loginRequest.setMobileNo("9998887777");
        loginRequest.setPassword("wrongPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String token = userService.userLogin(loginRequest);

        assertNull(token);
        verify(jwtService, never()).generateToken(anyString());
    }
}
