package com.abhi.Banking_SpringBootWeb_Project.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

//    private SecretKey secretKey;       //This creates new Secret key everytime the application restarts
//    public JWTService(){
//        try {
//            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
//            secretKey = keyGen.generateKey();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    private SecretKey getKey() {
//         return secretKey;
//    }

    @Value("${jwt.secret}")
    public String secretKey;         //This will remain same and will not block old users after application restarts
                                     //the Secret key is defined in application properties
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }



    public String generateToken(String userMobileNo){

        Map<String,Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userMobileNo)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String extractMobileNo(String token) {
        // extract the mobile no from username
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userMobileNo = extractMobileNo(token);
        return (userMobileNo.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }  //userDetails.getUsername actually returns Mobile no

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
