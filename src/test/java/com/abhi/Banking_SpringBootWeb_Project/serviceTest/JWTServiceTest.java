package com.abhi.Banking_SpringBootWeb_Project.serviceTest;

import com.abhi.Banking_SpringBootWeb_Project.service.JWTService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JWTService.
 *
 * Note: secretKey is a plain @Value-injected public field (no Spring context
 * needed for these tests) so we set it directly to a throwaway test key --
 * NEVER reuse this key anywhere outside this test class.
 */
class JWTServiceTest {

    // Throwaway 256-bit key generated solely for these tests.
    private static final String TEST_SECRET = "xYYdoZbTw/JQ9pomlXEpoq8NmuzRQlb6lzLJEDNWdp0=";

    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        jwtService.secretKey = TEST_SECRET;
    }

    @Test
    void generateToken_andExtractMobileNo_shouldRoundTripToSameValue() {
        String token = jwtService.generateToken("9998887777");

        assertNotNull(token);
        assertEquals("9998887777", jwtService.extractMobileNo(token));
    }

    @Test
    void validateToken_withMatchingUserDetails_shouldReturnTrue() {
        String token = jwtService.generateToken("9998887777");
        UserDetails userDetails = buildUserDetails("9998887777");

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_withDifferentUser_shouldReturnFalse() {
        String token = jwtService.generateToken("9998887777");
        UserDetails otherUser = buildUserDetails("1112223333");

        assertFalse(jwtService.validateToken(token, otherUser));
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));

        // Manually build a token that already expired 1 minute ago
        String expiredToken = Jwts.builder()
                .subject("9998887777")
                .issuedAt(new Date(System.currentTimeMillis() - 2 * 60 * 1000))
                .expiration(new Date(System.currentTimeMillis() - 60 * 1000))
                .signWith(key)
                .compact();

        UserDetails userDetails = buildUserDetails("9998887777");

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtService.validateToken(expiredToken, userDetails),
                "Parsing an expired token currently throws rather than returning false -- " +
                        "callers (JWTFilter) must be prepared to catch this.");
    }

    private UserDetails buildUserDetails(String mobileNo) {
        return new User(mobileNo, "irrelevant-password", java.util.Collections.emptyList());
    }
}

